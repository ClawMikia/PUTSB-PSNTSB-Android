package com.cyberpunk.debttracker.game

import com.cyberpunk.debttracker.data.db.DebtDao
import com.cyberpunk.debttracker.data.db.GameDao
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtType
import com.cyberpunk.debttracker.data.model.game.AchievementState
import com.cyberpunk.debttracker.data.model.game.CycleBaseline
import com.cyberpunk.debttracker.data.model.game.GameLogEntry
import com.cyberpunk.debttracker.data.model.game.LogKind
import com.cyberpunk.debttracker.data.model.game.Npc
import com.cyberpunk.debttracker.data.model.game.NpcMood
import com.cyberpunk.debttracker.data.model.game.PlayerProfile
import com.cyberpunk.debttracker.data.model.game.QuestState
import com.cyberpunk.debttracker.data.model.game.RolloverState
import com.cyberpunk.debttracker.data.model.game.StatCounter
import com.cyberpunk.debttracker.data.model.game.StatGauge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

// ═══════════════════════════════════════════════════════════════════════════
//  GAME ENGINE
//
//  Every debt mutation funnels through here, which turns plain bookkeeping
//  into a game: teddy-bear rewards for progress, white skull penalties for
//  slip-ups, NPC relationships that level up, and 7 daily / 30 monthly /
//  100 annual quests that rotate on their own clocks.
//
//  All state lives in gamification_db, which is excluded from cloud backup,
//  so uninstalling the app wipes the campaign back to zero — exactly like a
//  fresh install. Debt records stay in debt_tracker_db and keep backing up.
// ═══════════════════════════════════════════════════════════════════════════

sealed interface GameOverlay {
    val title: String
    val blurb: String

    data class Reward(
        val code: String, override val title: String, override val blurb: String,
        val xp: Int, val coins: Int, val nerve: Int, val bolts: Int,
    ) : GameOverlay

    data class Penalty(
        val code: String, override val title: String, override val blurb: String,
        val coins: Int, val nerve: Int,
    ) : GameOverlay

    data class Achievement(
        val code: String, override val title: String, override val blurb: String,
        val tier: AchievementTier,
    ) : GameOverlay

    data class QuestDone(
        val id: String, override val title: String, override val blurb: String,
        val cycle: QuestCycle,
    ) : GameOverlay

    data class LevelUp(
        val from: Int, val to: Int, override val title: String, override val blurb: String,
    ) : GameOverlay

    data class NpcLevelUp(
        val name: String, val npcTitle: String, val level: Int,
        override val title: String, override val blurb: String,
    ) : GameOverlay

    data class NpcMoodShift(
        val name: String, val npcTitle: String, val mood: NpcMood,
        override val title: String, override val blurb: String,
    ) : GameOverlay
}

/** A gauge that moved since the last evaluation, used for zero-crossing rewards. */
private data class Transition(val key: String, val from: Int, val to: Int)

@Singleton
class GameEngine @Inject constructor(
    private val gameDao: GameDao,
    private val debtDao: DebtDao,
) {

    // ─── Public streams ───────────────────────────────────────────────────────

    val profile: Flow<PlayerProfile?> = gameDao.observeProfile()
    val achievements: Flow<List<AchievementState>> = gameDao.observeAchievementStates()
    val unlockedAchievements: Flow<Int> = gameDao.observeUnlockedCount()
    val npcs: Flow<List<Npc>> = gameDao.observeAllNpcs()
    val logEntries: Flow<List<GameLogEntry>> = gameDao.observeRecentLog(LOG_LIMIT)
    val unreadLogCount: Flow<Int> = gameDao.observeUnreadLogCount()
    val counters: Flow<List<StatCounter>> = gameDao.observeAllCounters()
    val gauges: Flow<List<StatGauge>> = gameDao.observeAllGauges()

    fun questStates(cycle: QuestCycle): Flow<List<QuestState>> =
        gameDao.observeQuestStates(Cycles.keyFor(cycle))

    fun questCompletedCount(cycle: QuestCycle): Flow<Int> =
        gameDao.observeQuestCompletedCount(Cycles.keyFor(cycle))

    private val _overlays = MutableSharedFlow<GameOverlay>(extraBufferCapacity = 512)
    val overlays: SharedFlow<GameOverlay> = _overlays.asSharedFlow()

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    suspend fun bootstrap() {
        val today = Cycles.epochDay()
        val state = gameDao.getRollover()
        val stored = gameDao.getProfile()

        if (stored == null || state == null || state.dailyCycle.isEmpty()) {
            firstRun(today)
        } else {
            rollover(stored, state, today)
        }
        settle()
    }

    private suspend fun firstRun(today: Long) {
        gameDao.insertProfileIfAbsent(PlayerProfile(lastSeenDay = today))
        gameDao.upsertProfile(
            PlayerProfile(
                lastSeenDay = today,
                rankTitle = LevelCurve.rankTitle(1),
            ),
        )
        gameDao.upsertRollover(
            RolloverState(
                lastRolloverDay = today,
                dailyCycle = Cycles.dailyKey(),
                monthlyCycle = Cycles.monthlyKey(),
                annualCycle = Cycles.annualKey(),
            ),
        )
        QuestCycle.entries.forEach { seedBaselines(Cycles.keyFor(it)) }
        gameDao.addToCounter(Stat.DAYS_PLAYED, 1)
        grant(RewardCatalog.FIRST_LAUNCH)
        log(LogKind.SYSTEM, "CAMPAIGN INITIALISED", "Level 1, 0 XP, 0 coins. Nothing unlocked.", "SYS_BOOT")
    }

    private suspend fun rollover(stored: PlayerProfile, state: RolloverState, today: Long) {
        var profile = stored
        val gap = today - state.lastRolloverDay

        if (gap >= 1) {
            if (profile.streakDays > 0 && gap > 1) {
                profile = profile.copy(streakDays = 0)
                penalize(PenaltyCatalog.STREAK_LOST)
            }
            when {
                gap >= 7 -> penalize(PenaltyCatalog.ABANDONED_7D)
                gap >= 3 -> penalize(PenaltyCatalog.ABANDONED_3D)
            }

            profile = profile.copy(
                streakDays = profile.streakDays + 1,
                playDays = profile.playDays + 1,
                lastSeenDay = today,
                bestStreakDays = maxOf(profile.bestStreakDays, profile.streakDays + 1),
            )
            gameDao.upsertProfile(profile)
            gameDao.addToCounter(Stat.DAYS_PLAYED, 1)
            gameDao.maxCounter(Stat.STREAK_BEST, profile.bestStreakDays)
            gameDao.upsertGauge(StatGauge(Stat.G_ACTIVE_TODAY, 0))

            grant(RewardCatalog.STREAK_DAY)
            when {
                profile.streakDays >= 365 -> grant(RewardCatalog.STREAK_365)
                profile.streakDays >= 100 -> grant(RewardCatalog.STREAK_100)
                profile.streakDays >= 30 -> grant(RewardCatalog.STREAK_30)
                profile.streakDays >= 7 -> grant(RewardCatalog.STREAK_7)
                profile.streakDays == 6 -> penalize(PenaltyCatalog.STREAK_SHORT)
            }
            if (profile.playDays == 365) grant(RewardCatalog.YEAR_OF_DEBTS)

            overduePenalties(gap)
        } else if (profile.lastSeenDay != today) {
            gameDao.upsertProfile(profile.copy(lastSeenDay = today))
        }

        rotateCycles(state, today)
    }

    private suspend fun rotateCycles(state: RolloverState, today: Long) {
        var next = state.copy(lastRolloverDay = today)

        for (cycle in QuestCycle.entries) {
            val key = Cycles.keyFor(cycle)
            if (next.cycleKeyOf(cycle).isNotEmpty() && next.cycleKeyOf(cycle) != key) {
                closeCycle(cycle, next.cycleKeyOf(cycle))
                seedBaselines(key)
            }
            next = next.withCycleKey(cycle, key)
        }
        gameDao.upsertRollover(next)
    }

    private fun RolloverState.cycleKeyOf(cycle: QuestCycle): String = when (cycle) {
        QuestCycle.DAILY -> dailyCycle
        QuestCycle.MONTHLY -> monthlyCycle
        QuestCycle.ANNUAL -> annualCycle
    }

    private fun RolloverState.withCycleKey(cycle: QuestCycle, key: String): RolloverState = when (cycle) {
        QuestCycle.DAILY -> copy(dailyCycle = key)
        QuestCycle.MONTHLY -> copy(monthlyCycle = key)
        QuestCycle.ANNUAL -> copy(annualCycle = key)
    }

    private suspend fun closeCycle(cycle: QuestCycle, closedKey: String) {
        val defs = QuestCatalog.forCycle(cycle)
        if (defs.isEmpty()) return
        val states = gameDao.getQuestStates(closedKey).associateBy { it.questId }
        val done = defs.count { states[it.id]?.completed == true }

        when (cycle) {
            QuestCycle.DAILY -> when {
                done == defs.size -> {
                    gameDao.addToCounter(Stat.ALL_SEVEN_DONE, 1)
                    gameDao.addToCounter(Stat.QUESTS_DAILY_ALL, 1)
                    grant(RewardCatalog.QUEST_ALL_DAILY)
                }
                done == 0 -> penalize(PenaltyCatalog.IGNORED_QUEST)
            }
            QuestCycle.MONTHLY -> if (done == defs.size) {
                gameDao.addToCounter(Stat.MONTHS_CLEARED, 1)
                grant(RewardCatalog.MONTH_CLEARED)
            }
            QuestCycle.ANNUAL -> if (done == defs.size) {
                gameDao.addToCounter(Stat.YEARS_CLEARED, 1)
                grant(RewardCatalog.YEAR_CLEARED)
            }
        }
        log(
            LogKind.SYSTEM,
            "${cycle.label} CLOSED",
            "$done of ${defs.size} completed.",
            "SYS_CYCLE_${cycle.name}_$closedKey",
        )
    }

    private suspend fun overduePenalties(daysMissed: Long) {
        val overdue = liveDebts().filter { !it.isArchived && it.isOverdue }
        if (overdue.isEmpty()) {
            gameDao.addToCounter(Stat.CLEAN_DAYS, 1)
            grant(RewardCatalog.CLEAN_DAY)
            return
        }
        if (overdue.any { it.debtType == DebtType.I_OWE }) penalize(PenaltyCatalog.OVERDUE_OWE)
        if (overdue.any { it.debtType == DebtType.OWES_ME }) penalize(PenaltyCatalog.OVERDUE_LENT)

        val worstDays = overdue
            .mapNotNull { it.dueDate?.let { due -> (now() - due) / MILLIS_PER_DAY } }
            .maxOrNull() ?: 0L
        penalize(PenaltyCatalog.forDaysLate(worstDays))
        if (overdue.size >= 4) penalize(PenaltyCatalog.BAD_DEBT_CLUSTER)
        if (daysMissed >= 7) penalize(PenaltyCatalog.INTEREST_COMPOUND)
    }

    // ─── Debt events ──────────────────────────────────────────────────────────

    suspend fun onDebtAdded(debt: Debt) {
        bump(Stat.DEBTS_ADDED)
        bump(Stat.VALUE_ADDED, peso(debt.amount))
        raise(Stat.LARGEST_NODE, peso(debt.amount))

        when {
            debt.isOverdue -> penalize(PenaltyCatalog.NEW_DEBT_OVERDUE)
            peso(debt.amount) >= 50_000L -> penalize(PenaltyCatalog.NEW_DEBT_BIG)
            else -> penalize(PenaltyCatalog.NEW_DEBT)
        }

        val npc = ensureNpc(debt.personName)
        bump(Stat.NPCS_RECRUITED)
        grant(RewardCatalog.NPC_RECRUIT)
        log(LogKind.NPC, "${npc.name.uppercase()} ENTERED THE MATRIX", npc.title, seedOf(npc))

        settle()
    }

    suspend fun onPaymentRecorded(debt: Debt, amount: Double) {
        bump(Stat.PAYMENTS_RECORDED)
        val pesoAmount = peso(amount)
        val fullySettled = (debt.paidAmount + amount) >= debt.amount - 0.01

        if (!fullySettled) {
            grant(
                if (pesoAmount >= 10_000L) RewardCatalog.SETTLE_MID else RewardCatalog.PARTIAL_STEP,
            )
            val npc = ensureNpc(debt.personName)
            log(LogKind.NPC, "PARTIAL FROM ${npc.name.uppercase()}", "₱$pesoAmount received", seedOf(npc))
        } else if (debt.debtType == DebtType.OWES_ME) {
            bump(Stat.VALUE_LENT_COLLECTED, pesoAmount)
            grant(if (pesoAmount >= 100_000L) RewardCatalog.COLLECT_BIG else RewardCatalog.COLLECT_LENT)
        }

        settle()
    }

    suspend fun onDebtSettled(debt: Debt) {
        val value = peso(debt.amount)
        val wasOverdue = debt.isOverdue
        val isOwe = debt.debtType == DebtType.I_OWE

        bump(Stat.DEBTS_SETTLED)
        bump(Stat.VALUE_SETTLED, value)
        if (isOwe) bump(Stat.VALUE_OWED_CLEARED, value) else bump(Stat.VALUE_LENT_COLLECTED, value)
        bump(Stat.NPC_DEBT_CLEARED)
        bump(Stat.NPC_SETTLE)

        // Settlements today, stored in the G_ACTIVE_TODAY gauge.
        val today = Cycles.epochDay()
        val settlementsToday = (gameDao.getGauge(Stat.G_ACTIVE_TODAY) ?: 0) + 1
        gameDao.upsertGauge(StatGauge(Stat.G_ACTIVE_TODAY, settlementsToday))
        when (settlementsToday) {
            1 -> grant(RewardCatalog.FIRST_SETTLE)
            2 -> {
                bump(Stat.BIG_DAY)
                grant(RewardCatalog.BIG_DAY)
            }
            3 -> {
                bump(Stat.TRIPLE_DAY)
                grant(RewardCatalog.TRIPLE_DAY)
            }
        }

        val previousRecord = gameDao.getCounter(Stat.LARGEST_SETTLEMENT) ?: 0
        if (value > previousRecord) {
            raise(Stat.LARGEST_SETTLEMENT, value)
            if (previousRecord > 0) grant(RewardCatalog.NEW_RECORD)
        }

        when {
            wasOverdue -> {
                bump(Stat.OVERDUE_PAID)
                grant(RewardCatalog.OVERDUE_SLAYER)
            }
            debt.dueDate != null -> {
                bump(Stat.PERFECT_NODE)
                grant(RewardCatalog.PERFECT_NODE)
            }
            else -> bump(Stat.ONTIME_PAID)
        }
        if (debt.paidAmount > 0.0) {
            bump(Stat.PARTIAL_TO_FULL)
            grant(RewardCatalog.PARTIAL_TO_FULL)
        }

        grant(RewardCatalog.rewardForSettledValue(value.toLong()))
        if (!isOwe) {
            grant(if (value >= 100_000) RewardCatalog.COLLECT_BIG else RewardCatalog.COLLECT_LENT)
        }

        val npc = levelUpNpc(debt.personName)
        log(
            LogKind.NPC,
            "${npc.name.uppercase()} SETTLED",
            if (wasOverdue) "Overdue cleared. They exhaled." else "Node closed. ₱$value gone.",
            seedOf(npc),
        )

        settle()
    }

    suspend fun onDebtDeleted(debt: Debt) {
        bump(Stat.DEBTS_DELETED)
        penalize(PenaltyCatalog.EDIT_DELETE)
        settle()
    }

    suspend fun onDebtArchived(debt: Debt) {
        bump(Stat.ARCHIVES)
        grant(RewardCatalog.DAILY_SWEEP)
        settle()
    }

    suspend fun onImported(count: Int) {
        bump(Stat.IMPORTS)
        bump(Stat.IMPORTED_NODES, count)
        settle()
    }

    suspend fun onExported(count: Int) {
        bump(Stat.EXPORTS)
        settle()
    }

    suspend fun onDetailViewed() {
        bump(Stat.DETAIL_VISITS)
    }

    suspend fun onAnalyticsViewed() {
        bump(Stat.ANALYTICS_VISITS)
    }

    // ─── NPC roster ───────────────────────────────────────────────────────────

    suspend fun ensureNpc(personName: String): Npc {
        val key = nameKey(personName)
        gameDao.getNpc(key)?.let { return it }

        val archetype = NpcArchetype.forPerson(personName)
        val candidate = Npc(
            nameKey = key,
            name = personName.trim().ifEmpty { "UNKNOWN" },
            title = archetype.name,
            archetype = archetype.key,
            traitCsv = archetype.traits.joinToString("|"),
            patience = archetype.basePatience,
            maxPatience = archetype.basePatience,
        )
        val id = gameDao.insertNpcIfAbsent(candidate)
        return if (id > 0L) candidate.copy(id = id) else gameDao.getNpc(key) ?: candidate
    }

    /**
     * Rebuilds every NPC's aggregates straight from the live debt table, so the
     * roster can never drift away from the ledger. Call after any mutation.
     */
    suspend fun syncNpcs() = syncNpcs(liveDebts())

    suspend fun syncNpcs(debts: List<Debt>) {
        val grouped = debts.filter { it.personName.isNotBlank() }
            .groupBy { nameKey(it.personName) }
        val existing = gameDao.getAllNpcs().associateBy { it.nameKey }
        val stamp = now()

        for ((key, nodes) in grouped) {
            val before = existing[key] ?: ensureNpc(nodes.first().personName)
            val archetype = NpcArchetype.byKey(before.archetype)

            val open = nodes.filter { !it.isArchived && !it.isSettled }
            val settled = nodes.count { it.isSettled }
            val overdue = open.count { it.isOverdue }

            val owedToThem = open.filter { it.debtType == DebtType.I_OWE }.sumOf { peso(it.remaining) }
            val paidToThem = nodes.filter { it.debtType == DebtType.I_OWE }.sumOf { peso(it.paidAmount) }
            val theyOweYou = open.filter { it.debtType == DebtType.OWES_ME }.sumOf { peso(it.remaining) }
            val collected = nodes.filter { it.debtType == DebtType.OWES_ME }.sumOf { peso(it.paidAmount) }

            val wasEnraged = before.isEnraged
            val hadOpen = before.openDebts > 0
            val wasSettled = before.settledDebts

            val patience = when {
                open.isEmpty() -> before.maxPatience
                else -> {
                    val drain = overdue * (3 * archetype.retaliation).coerceAtLeast(1)
                    val recover = if (overdue == 0) 2 else 0
                    (before.patience - drain + recover).coerceIn(0, before.maxPatience)
                }
            }
            val nowEnraged = patience <= 25

            val relation = (before.relation
                + if (open.isEmpty() && hadOpen) 6 else 0
                - overdue * 2 * archetype.retaliation
                + if (archetype.key == "GHOST" && overdue > 0) -3 else 0
                ).coerceIn(-100, 100)

            val updated = before.copy(
                title = archetype.name,
                traitCsv = archetype.traits.joinToString("|"),
                trackedDebts = nodes.size,
                openDebts = open.size,
                settledDebts = settled,
                overdueDebts = overdue,
                owedToThem = owedToThem.toInt(),
                paidToThem = paidToThem.toInt(),
                theyOweYou = theyOweYou.toInt(),
                collectedFromThem = collected.toInt(),
                patience = patience,
                relation = relation,
                mood = moodFor(open.isEmpty(), settled, overdue, patience, before.maxPatience),
                lastSeenAt = stamp,
                rages = before.rages + if (!wasEnraged && nowEnraged) 1 else 0,
            )
            gameDao.upsertNpc(updated)

            when {
                wasEnraged && !updated.isEnraged -> {
                    bump(Stat.NPC_RAGE_CLEARED)
                    grant(RewardCatalog.NPC_RAGE_CLEARED)
                    log(LogKind.NPC, "${updated.name.uppercase()} CALMED", archetype.happy, seedOf(updated))
                }
                !wasEnraged && updated.isEnraged -> {
                    bump(Stat.NPC_RAGES)
                    penalize(if (overdue >= 3) PenaltyCatalog.NPC_ENRAGE else PenaltyCatalog.NPC_RAGE)
                    log(LogKind.NPC, "${updated.name.uppercase()} IS FURIOUS", archetype.angry, seedOf(updated))
                    emit(
                        GameOverlay.NpcMoodShift(
                            name = updated.name,
                            npcTitle = updated.title,
                            mood = NpcMood.ANGRY,
                            title = "${updated.name.uppercase()} IS FURIOUS",
                            blurb = archetype.angry,
                        ),
                    )
                }
            }

            if (open.isEmpty() && hadOpen) {
                if (settled > wasSettled) {
                    bump(Stat.NPCS_PACIFIED)
                    grant(RewardCatalog.NPC_PACIFIED)
                    log(LogKind.NPC, "${updated.name.uppercase()} IS PACIFIED", archetype.happy, seedOf(updated))
                }
                if (before.trackedDebts > 0) {
                    bump(Stat.NPCS_WIPED)
                    grant(RewardCatalog.NPC_PURGED)
                }
            }

            // Long-silence flavour penalties.
            if (updated.openDebts > 0 && stamp - before.lastSeenAt > MILLIS_PER_DAY * 14) {
                penalize(PenaltyCatalog.SILENT_CREDITOR)
            }
            if (updated.openDebts > 0 && updated.theyOweYou == 0 && updated.owedToThem >= 100_000) {
                penalize(PenaltyCatalog.NPC_BROKE)
            }
        }

        // Contacts whose debts are all gone.
        for (key in existing.keys - grouped.keys) {
            val npc = existing.getValue(key)
            if (npc.openDebts > 0) penalize(PenaltyCatalog.NPC_ABANDONED)
            if (npc.isEnraged) penalize(PenaltyCatalog.PATIENCE_GONE)
            gameDao.upsertNpc(npc.copy(openDebts = 0, lastSeenAt = stamp))
        }

        val angryCount = gameDao.getAllNpcs().count { it.isEnraged }
        if (angryCount >= 3) penalize(PenaltyCatalog.NPC_MULTIPLIER)
    }

    private fun moodFor(
        noOpen: Boolean, settled: Int, overdue: Int, patience: Int, maxPatience: Int,
    ): NpcMood = when {
        noOpen && settled > 0 -> NpcMood.GRATEFUL
        noOpen -> NpcMood.BROKE
        overdue > 0 && patience <= maxPatience * 25 / 100 -> NpcMood.ANGRY
        overdue > 0 && patience <= maxPatience * 50 / 100 -> NpcMood.DESPERATE
        overdue > 0 -> NpcMood.AFRAID
        patience >= maxPatience * 85 / 100 -> NpcMood.PATIENT
        else -> NpcMood.CHEERFUL
    }

    private suspend fun levelUpNpc(personName: String): Npc {
        var npc = ensureNpc(personName)
        val archetype = NpcArchetype.byKey(npc.archetype)
        val fromLevel = npc.level
        var xp = npc.xp + archetype.xpPerSettle

        while (npc.level < NPC_MAX_LEVEL) {
            val need = npcXpToNext(npc.level)
            if (xp < need) break
            xp -= need
            npc = npc.copy(level = npc.level + 1, levelUps = npc.levelUps + 1)
        }
        if (npc.level >= NPC_MAX_LEVEL) xp = 0

        val previousRelation = npc.relation
        npc = npc.copy(
            xp = xp,
            relation = (npc.relation + archetype.relationPerSettle).coerceIn(-100, 100),
            lastLevelAt = now(),
        )
        gameDao.upsertNpc(npc)
        raise(Stat.NPC_TOP_LEVEL, npc.level)

        if (npc.level > fromLevel) {
            bump(Stat.NPC_LEVEL_UPS, npc.level - fromLevel)
            grant(RewardCatalog.NPC_LEVEL_UP)
            val blurb = if (npc.level >= NPC_MAX_LEVEL) {
                "Max bond reached. They would take a bullet."
            } else {
                "Level $fromLevel → ${npc.level}. Closer than before."
            }
            emit(
                GameOverlay.NpcLevelUp(
                    name = npc.name,
                    npcTitle = npc.title,
                    level = npc.level,
                    title = "${npc.name.uppercase()} LEVEL ${npc.level}",
                    blurb = blurb,
                ),
            )
        }
        if (npc.level >= NPC_MAX_LEVEL) grant(RewardCatalog.NPC_MAX_LEVEL)
        if (previousRelation < 70 && npc.relation >= 70) grant(RewardCatalog.NPC_BONDED)
        return npc
    }

    // ─── NPC actions ──────────────────────────────────────────────────────────

    suspend fun nudgeNpc(npc: Npc): Boolean {
        val today = Cycles.epochDay()
        val fresh = gameDao.getNpc(npc.nameKey) ?: return false
        if (fresh.nudgedDay == today) return false
        gameDao.upsertNpc(
            fresh.copy(
                nudgedDay = today,
                nudgesToday = fresh.nudgesToday + 1,
                relation = (fresh.relation + 1).coerceIn(-100, 100),
            ),
        )
        bump(Stat.NPC_NUDGES)
        grant(RewardCatalog.NPC_NUDGED)
        log(LogKind.NPC, "NUDGED ${fresh.name.uppercase()}", "A friendly reminder was delivered.", seedOf(fresh))
        settle()
        return true
    }

    suspend fun pacifyNpc(npc: Npc): Boolean {
        val profile = profile() ?: return false
        if (profile.coins < PACIFY_COST) return false
        val fresh = gameDao.getNpc(npc.nameKey) ?: return false
        gameDao.upsertNpc(
            fresh.copy(
                patience = (fresh.patience + 25).coerceAtMost(fresh.maxPatience),
                relation = (fresh.relation + 10).coerceIn(-100, 100),
            ),
        )
        spendCoins(PACIFY_COST)
        bump(Stat.NPC_PACIFIES)
        grant(RewardCatalog.NPC_PACIFIED_ACTION)
        log(LogKind.NPC, "PACIFIED ${fresh.name.uppercase()}", "Spent $PACIFY_COST coins on goodwill.", seedOf(fresh))
        settle()
        return true
    }

    suspend fun confrontNpc(npc: Npc): Boolean {
        val fresh = gameDao.getNpc(npc.nameKey) ?: return false
        gameDao.upsertNpc(
            fresh.copy(
                patience = (fresh.patience - 12).coerceAtLeast(0),
                relation = (fresh.relation - 6).coerceIn(-100, 100),
            ),
        )
        bump(Stat.NPC_CONFRONTATIONS)
        penalize(PenaltyCatalog.NPC_CONFRONT)
        log(LogKind.NPC, "CONFRONTED ${fresh.name.uppercase()}", "You escalated. It did not help.", seedOf(fresh))
        settle()
        return true
    }

    // ─── In-app campaign restart ──────────────────────────────────────────────

    suspend fun purgeCampaign() {
        val previousLevel = profile()?.level ?: 1
        gameDao.purgeCampaign()
        grant(RewardCatalog.REINCARNATE)
        log(
            LogKind.SYSTEM, "CAMPAIGN PURGED",
            "Level $previousLevel erased. Back to zero, exactly like a fresh install.",
            "SYS_PURGE",
        )
        bootstrap()
    }

    suspend fun markLogRead() = gameDao.markLogRead()

    // ─── Stat plumbing ────────────────────────────────────────────────────────

    private suspend fun bump(key: String, delta: Int = 1) {
        if (delta != 0) gameDao.addToCounter(key, delta)
    }

    private suspend fun raise(key: String, value: Int) = gameDao.maxCounter(key, value)

    private suspend fun liveDebts(): List<Debt> = debtDao.getAllDebtsForExport()

    private suspend fun profile(): PlayerProfile? = gameDao.getProfile()

    /**
     * Recomputes every gauge from the live debt table and reports what moved,
     * so zero-crossings can fire one-shot rewards.
     */
    private suspend fun refreshGauges(): List<Transition> {
        val debts = liveDebts()
        val open = debts.filter { !it.isArchived && !it.isSettled }
        val overdue = open.filter { it.isOverdue }
        val owe = open.filter { it.debtType == DebtType.I_OWE }
        val lent = open.filter { it.debtType == DebtType.OWES_ME }
        val alive = gameDao.getAllNpcs()

        val desired = listOf(
            StatGauge(Stat.G_ACTIVE, open.size),
            StatGauge(Stat.G_OVERDUE, overdue.size),
            StatGauge(Stat.G_OPEN_OWE, owe.sumOf { peso(it.remaining) }.toInt()),
            StatGauge(Stat.G_OPEN_LENT, lent.sumOf { peso(it.remaining) }.toInt()),
            StatGauge(Stat.G_OVERDUE_OWE, overdue.count { it.debtType == DebtType.I_OWE }),
            StatGauge(Stat.G_OVERDUE_LENT, overdue.count { it.debtType == DebtType.OWES_ME }),
            StatGauge(Stat.G_NET_OWED, (lent.sumOf { peso(it.remaining) } - owe.sumOf { peso(it.remaining) }).toInt()),
            StatGauge(Stat.G_NERVE, profile()?.nerve ?: PlayerProfile.MAX_NERVE),
            StatGauge(Stat.G_NPCS, alive.size),
            StatGauge(Stat.G_NPCS_ANGRY, alive.count { it.isEnraged }),
            StatGauge(Stat.G_NPC_DEBTS, alive.sumOf { it.openDebts }),
        )

        val moves = ArrayList<Transition>(desired.size)
        for (gauge in desired) {
            val previous = gameDao.getGauge(gauge.statKey) ?: 0
            if (previous != gauge.value) {
                moves += Transition(gauge.statKey, previous, gauge.value)
                gameDao.upsertGauge(gauge)
            }
        }
        return moves
    }

    // ─── Evaluation ───────────────────────────────────────────────────────────

    /**
     * Runs a few passes so rewards unlocked by rewards are picked up too.
     * Bounded to [EVALUATION_PASSES] so a chain reaction can never loop.
     */
    private suspend fun settle() {
        repeat(EVALUATION_PASSES) {
            if (!evaluateOnce()) return
        }
    }

    private suspend fun evaluateOnce(): Boolean {
        var changed: Boolean

        lastTransitions = refreshGauges()
        changed = lastTransitions.isNotEmpty()

        val stats = readStats()
        if (evaluateAchievements(stats)) changed = true
        for (cycle in QuestCycle.entries) {
            if (evaluateCycle(cycle, stats)) changed = true
        }
        syncDerived(stats)
        if (fireGaugeRewards(stats)) changed = true

        return changed
    }

    private suspend fun readStats(): Map<String, Int> {
        val map = HashMap<String, Int>(160)
        gameDao.getAllCounters().forEach { map[it.statKey] = it.value }
        gameDao.getAllGauges().forEach { map[it.statKey] = it.value }
        return map
    }

    /** Counters the engine owns outright — never incremented by events. */
    private suspend fun syncDerived(stats: Map<String, Int>) {
        val stored = gameDao.getAchievementStates()
        gameDao.setCounterValue(Stat.ACHIEVEMENTS_UNLOCKED, stored.count { it.unlocked })

        val defs = AchievementCatalog.all
        val completedTiers = AchievementTier.entries.count { tier ->
            val inTier = defs.filter { it.tier == tier }
            inTier.isNotEmpty() && inTier.count { (stats[it.statKey] ?: 0) >= it.target } == inTier.size
        }
        for (key in listOf(
            Stat.ACH_SAME_TIER_10, Stat.ACH_SAME_TIER_25,
            Stat.ACH_SAME_TIER_50, Stat.ACH_SAME_TIER_100,
        )) {
            gameDao.setCounterValue(key, completedTiers)
        }

        profile()?.let { current ->
            gameDao.setCounterValue(Stat.TOP_LEVEL, current.level)
            gameDao.setCounterValue(Stat.STREAK_BEST, current.bestStreakDays)
        }
    }

    private suspend fun evaluateAchievements(stats: Map<String, Int>): Boolean {
        val stored = gameDao.getAchievementStates().associateBy { it.code }
        var changed = false

        for (def in AchievementCatalog.all) {
            val value = (stats[def.statKey] ?: 0).coerceAtLeast(0)
            val progress = value.coerceAtMost(def.target)
            val existing = stored[def.code]

            if (existing == null) {
                gameDao.upsertAchievementState(
                    AchievementState(
                        code = def.code,
                        progress = progress,
                        target = def.target,
                        unlocked = value >= def.target,
                        unlockedAt = if (value >= def.target) now() else 0L,
                    ),
                )
                changed = true
                continue
            }
            if (existing.unlocked) continue

            if (value >= def.target) {
                gameDao.upsertAchievementState(
                    existing.copy(
                        progress = def.target,
                        target = def.target,
                        unlocked = true,
                        unlockedAt = now(),
                    ),
                )
                onAchievementUnlocked(def)
                changed = true
            } else if (existing.progress != progress || existing.target != def.target) {
                gameDao.upsertAchievementState(
                    existing.copy(progress = progress, target = def.target),
                )
                changed = true
            }
        }
        return changed
    }

    private suspend fun evaluateCycle(cycle: QuestCycle, stats: Map<String, Int>): Boolean {
        val key = Cycles.keyFor(cycle)
        val defs = QuestCatalog.forCycle(cycle)
        if (defs.isEmpty()) return false

        val stored = gameDao.getQuestStates(key).associateBy { it.questId }
        var changed = false

        for (def in defs) {
            val raw = (stats[def.statKey] ?: 0).coerceAtLeast(0)
            val baseline = gameDao.getBaseline(key, def.statKey) ?: 0
            val progress = progressOf(def, raw, baseline)
            val reached = isReached(def, raw, baseline)
            val existing = stored[def.id]

            if (existing == null) {
                gameDao.upsertQuestState(
                    QuestState(
                        questId = def.id,
                        cycleKey = key,
                        progress = progress,
                        target = def.target,
                        completed = reached,
                        completedAt = if (reached) now() else 0L,
                    ),
                )
                if (reached) onQuestCompleted(def)
                changed = true
                continue
            }
            if (existing.completed) continue

            if (reached) {
                gameDao.upsertQuestState(
                    existing.copy(
                        progress = def.target,
                        target = def.target,
                        completed = true,
                        completedAt = now(),
                    ),
                )
                onQuestCompleted(def)
                changed = true
            } else if (existing.progress != progress || existing.target != def.target) {
                gameDao.upsertQuestState(existing.copy(progress = progress, target = def.target))
                changed = true
            }
        }
        return changed
    }

    private fun isReached(def: QuestDef, raw: Int, baseline: Int): Boolean = when (def.goalType) {
        QuestGoalType.REACH -> (raw - baseline).coerceAtLeast(0) >= def.target
        QuestGoalType.ABSOLUTE -> raw >= def.target
        QuestGoalType.REDUCE -> raw <= def.target
    }

    private fun progressOf(def: QuestDef, raw: Int, baseline: Int): Int = when (def.goalType) {
        QuestGoalType.REACH -> (raw - baseline).coerceAtLeast(0).coerceAtMost(def.target)
        QuestGoalType.ABSOLUTE -> raw.coerceAtMost(def.target)
        QuestGoalType.REDUCE -> if (raw <= def.target) def.target else 0
    }

    private suspend fun seedBaselines(cycleKey: String) {
        for (key in QuestCatalog.byStat().keys) {
            gameDao.insertBaselineIfAbsent(CycleBaseline(cycleKey, key, gameDao.getCounter(key) ?: 0))
        }
    }

    // ─── Gauge-driven, one-shot rewards ───────────────────────────────────────

    private suspend fun fireGaugeRewards(stats: Map<String, Int>): Boolean {
        val transitions = lastTransitions
        if (transitions.isEmpty()) return false
        var fired = false

        for (move in transitions) {
            when (move.key) {
                Stat.G_ACTIVE -> if (move.from > 0 && move.to == 0) {
                    grant(RewardCatalog.LEDGER_CLEAN)
                    fired = true
                }
                Stat.G_OVERDUE -> if (move.from > 0 && move.to == 0) {
                    grant(RewardCatalog.OVERDUE_SWEEP)
                    fired = true
                }
                Stat.G_OPEN_OWE -> if (move.from > 0 && move.to == 0) {
                    bump(Stat.OWE_SIDE_SETTLED)
                    grant(RewardCatalog.SWEEP_ALL)
                    fired = true
                }
                Stat.G_OPEN_LENT -> if (move.from > 0 && move.to == 0) {
                    bump(Stat.LENT_SIDE_SETTLED)
                    grant(RewardCatalog.SWEEP_ALL)
                    fired = true
                }
                Stat.G_NET_OWED -> {
                    if (move.from < 0 && move.to == 0) {
                        grant(RewardCatalog.NET_ZERO)
                        fired = true
                    } else if (move.from < 0 && move.to >= 0) {
                        grant(RewardCatalog.NET_POSITIVE)
                        fired = true
                    }
                }
                Stat.G_NERVE -> if (move.from < PlayerProfile.MAX_NERVE && move.to == PlayerProfile.MAX_NERVE) {
                    bump(Stat.NERVE_FULL)
                    grant(RewardCatalog.NERVE_FULL)
                    fired = true
                }
                Stat.G_NPCS -> if (move.from < 25 && move.to >= 25) {
                    grant(RewardCatalog.NPC_SWARM)
                    fired = true
                }
            }
        }
        lastTransitions = emptyList()

        // Threshold penalties recomputed from the current snapshot.
        val owe = stats[Stat.G_OPEN_OWE] ?: 0
        val lent = stats[Stat.G_OPEN_LENT] ?: 0
        val net = stats[Stat.G_NET_OWED] ?: 0
        val open = stats[Stat.G_ACTIVE] ?: 0
        val collected = stats[Stat.VALUE_LENT_COLLECTED] ?: 0
        val nerve = stats[Stat.G_NERVE] ?: PlayerProfile.MAX_NERVE

        if (owe >= 500_000) penalize(PenaltyCatalog.DEBT_AVALANCHE)
        else if (owe >= 100_000) penalize(PenaltyCatalog.DEBT_MOUNTAIN)
        if (owe >= 1_000_000 && net < 0) penalize(PenaltyCatalog.BORROW_BROKE)
        if (net < 0 && open > 0) penalize(PenaltyCatalog.NEGATIVE_NET)
        if (lent > 0 && collected == 0) penalize(PenaltyCatalog.UNPAID_COLLECTOR)
        if (nerve <= 40) penalize(PenaltyCatalog.NERVE_DRAIN)

        return fired
    }

    private var lastTransitions: List<Transition> = emptyList()

    // ─── Rewards & penalties ──────────────────────────────────────────────────

    private suspend fun onAchievementUnlocked(def: AchievementDef) {
        grant(RewardCatalog.ACH_UNLOCKED)
        if (def.tier == AchievementTier.LEGENDARY || def.tier == AchievementTier.PLATINUM) {
            grant(RewardCatalog.ACH_TIER)
        }
        log(LogKind.ACHIEVEMENT, def.title, def.description, def.code)
        emit(GameOverlay.Achievement(def.code, def.title, def.description, def.tier))
    }

    private suspend fun onQuestCompleted(def: QuestDef) {
        when (def.cycle) {
            QuestCycle.DAILY -> bump(Stat.QUESTS_DAILY_DONE)
            QuestCycle.MONTHLY -> bump(Stat.QUESTS_MONTHLY_DONE)
            QuestCycle.ANNUAL -> bump(Stat.QUESTS_ANNUAL_DONE)
        }
        bump(Stat.QUESTS_TOTAL_DONE)
        grant(
            when (def.cycle) {
                QuestCycle.DAILY -> RewardCatalog.QUEST_DAILY
                QuestCycle.MONTHLY -> RewardCatalog.QUEST_MONTHLY
                QuestCycle.ANNUAL -> RewardCatalog.QUEST_ANNUAL
            },
        )
        log(LogKind.QUEST, def.title, def.description, def.id)
        emit(GameOverlay.QuestDone(def.id, def.title, def.description, def.cycle))
    }

    /**
     * Applies a teddy-bear reward. Never re-enters [settle], so callers stay
     * in control of evaluation ordering.
     */
    suspend fun grant(code: String) {
        val def = RewardCatalog[code]
        var profile = profile() ?: return
        val coinsBefore = gameDao.getCounter(Stat.COINS_EARNED) ?: 0

        val fromLevel = profile.level
        val newXp = (profile.xp.toLong() + def.xp).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val curve = LevelCurve.evaluate(newXp, fromLevel)

        profile = profile.copy(
            xp = newXp,
            level = curve.level,
            coins = (profile.coins + def.coins).coerceAtLeast(0),
            bolts = profile.bolts + def.bolts,
            nerve = (profile.nerve + def.nerve).coerceIn(0, PlayerProfile.MAX_NERVE),
            rankTitle = LevelCurve.rankTitle(curve.level),
        )
        gameDao.upsertProfile(profile)
        gameDao.upsertGauge(StatGauge(Stat.G_NERVE, profile.nerve))

        bump(Stat.REWARDS_RECEIVED)
        bump(Stat.COINS_EARNED, def.coins)
        if (def.bolts > 0) {
            bump(Stat.BOLTS_EARNED, def.bolts)
            announce(RewardCatalog.BOLT_CACHE)
        }
        val coinsAfter = gameDao.getCounter(Stat.COINS_EARNED) ?: 0
        if (coinsAfter / COIN_STASH_STEP > coinsBefore / COIN_STASH_STEP) {
            announce(RewardCatalog.COIN_STASH)
        }

        if (curve.level > fromLevel) {
            bump(Stat.LEVEL_UPS, curve.level - fromLevel)
            grantMilestone(fromLevel, curve.level)
            val title = LevelCurve.rankTitle(curve.level)
            log(LogKind.LEVEL, "LEVEL $curve.level", title, "LVL_${curve.level}_$title")
            emit(GameOverlay.LevelUp(fromLevel, curve.level, "LEVEL $curve.level", title))
        }

        log(LogKind.REWARD, def.title, def.blurb, def.code)
        emit(GameOverlay.Reward(def.code, def.title, def.blurb, def.xp, def.coins, def.nerve, def.bolts))
    }

    /** Applies a white-skull penalty. Coins and nerve are floored at zero. */
    suspend fun penalize(code: String) {
        val def = PenaltyCatalog[code]
        val profile = profile() ?: return
        val nerveBefore = profile.nerve
        val newNerve = (nerveBefore + def.nerve).coerceIn(0, PlayerProfile.MAX_NERVE)

        gameDao.upsertProfile(
            profile.copy(
                coins = (profile.coins + def.coins).coerceAtLeast(0),
                nerve = newNerve,
            ),
        )
        gameDao.upsertGauge(StatGauge(Stat.G_NERVE, newNerve))

        bump(Stat.PENALTIES_RECEIVED)
        if (def.coins < 0) bump(Stat.COINS_SPENT, -def.coins)
        if (nerveBefore > 0 && newNerve == 0) bump(Stat.NERVE_EMPTY)

        log(LogKind.PENALTY, def.title, def.blurb, def.code)
        emit(GameOverlay.Penalty(def.code, def.title, def.blurb, def.coins, def.nerve))
    }

    /** Log + overlay only, no effect. Used for follow-up flavour rewards. */
    private suspend fun announce(code: String) {
        val def = RewardCatalog[code]
        log(LogKind.REWARD, def.title, def.blurb, def.code)
        emit(GameOverlay.Reward(def.code, def.title, def.blurb, def.xp, def.coins, def.nerve, def.bolts))
    }

    private suspend fun grantMilestone(fromLevel: Int, toLevel: Int) {
        grant(RewardCatalog.LEVEL_UP)
        val milestones = listOf(
            10 to RewardCatalog.LEVEL_10,
            25 to RewardCatalog.LEVEL_25,
            50 to RewardCatalog.LEVEL_50,
            100 to RewardCatalog.LEVEL_100,
            250 to RewardCatalog.LEVEL_250,
        )
        for ((level, code) in milestones) {
            if (fromLevel < level && toLevel >= level) grant(code)
        }
    }

    private suspend fun spendCoins(amount: Int) {
        val profile = profile() ?: return
        gameDao.upsertProfile(profile.copy(coins = (profile.coins - amount).coerceAtLeast(0)))
        bump(Stat.COINS_SPENT, amount)
    }

    private suspend fun log(kind: LogKind, title: String, detail: String, seed: String) {
        gameDao.insertLog(
            GameLogEntry(
                kind = kind,
                title = title,
                detail = detail,
                iconSeed = seed,
                penalty = kind == LogKind.PENALTY,
            ),
        )
        gameDao.trimLog(LOG_LIMIT)
    }

    private fun emit(overlay: GameOverlay) {
        _overlays.tryEmit(overlay)
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun nameKey(personName: String): String = personName.trim().lowercase()

    private fun seedOf(npc: Npc): String = "NPC_${npc.nameKey}_${npc.archetype}"

    private fun peso(value: Double): Int = value.roundToLong().coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()

    private fun now(): Long = System.currentTimeMillis()

    companion object {
        const val PACIFY_COST = 50
        const val NPC_MAX_LEVEL = 50
        const val LOG_LIMIT = 400
        private const val EVALUATION_PASSES = 3
        private const val COIN_STASH_STEP = 10_000
        private const val MILLIS_PER_DAY = 86_400_000L

        fun npcXpToNext(level: Int): Int = 60 + 30 * level + 2 * level * level
    }
}
