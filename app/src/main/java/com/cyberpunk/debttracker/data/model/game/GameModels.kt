package com.cyberpunk.debttracker.data.model.game

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ═══════════════════════════════════════════════════════════════════════════
//  GAMIFICATION SCHEMA
//
//  This entire schema lives in its own Room database (gamification_db) which
//  is explicitly EXCLUDED from cloud backup / device transfer. Uninstalling
//  the app therefore wipes the whole campaign: every achievement, quest,
//  NPC, coin, XP point and level returns to zero / locked.
//
//  Debt records themselves remain in debt_tracker_db and keep backing up.
// ═══════════════════════════════════════════════════════════════════════════

// ─── Player ──────────────────────────────────────────────────────────────────

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey
    @ColumnInfo(name = "slot")
    val slot: Int = SINGLETON,

    val handle: String = DEFAULT_HANDLE,

    val xp: Int = 0,
    val level: Int = 1,

    val coins: Int = 0,
    val bolts: Int = 0,

    /** "Nerve" — the player's HP-equivalent meter, 0..100. Penalties drain, rewards restore. */
    val nerve: Int = MAX_NERVE,

    val streakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val playDays: Int = 0,

    /** Epoch day (LocalDate.toEpochDay) of the last time the player was seen. */
    @ColumnInfo(name = "last_seen_day")
    val lastSeenDay: Long = 0L,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    val rankTitle: String = "",
) {
    companion object {
        const val SINGLETON = 1
        const val DEFAULT_HANDLE = "OPERATIVE"
        const val MAX_NERVE = 100
    }
}

// ─── Stats ───────────────────────────────────────────────────────────────────
//
//  COUNTERS are monotonically increasing lifetime totals. Quest progress
//  inside a cycle is measured as (current - baseline recorded at cycle start).
//
//  GAUGES are instantaneous values (how many debts are overdue right now,
//  the player's nerve, …). Quests of goal type REDUCE watch gauges.

@Entity(tableName = "stat_counters")
data class StatCounter(
    @PrimaryKey
    @ColumnInfo(name = "stat_key")
    val statKey: String,
    val value: Int = 0,
)

@Entity(tableName = "stat_gauges")
data class StatGauge(
    @PrimaryKey
    @ColumnInfo(name = "stat_key")
    val statKey: String,
    val value: Int = 0,
)

/** Snapshot of a counter at the moment a quest cycle opened. */
@Entity(tableName = "cycle_baseline", primaryKeys = ["cycle_key", "stat_key"])
data class CycleBaseline(
    @ColumnInfo(name = "cycle_key") val cycleKey: String,
    @ColumnInfo(name = "stat_key") val statKey: String,
    val value: Int = 0,
)

// ─── Achievements ────────────────────────────────────────────────────────────

@Entity(tableName = "achievement_state")
data class AchievementState(
    @PrimaryKey
    @ColumnInfo(name = "code")
    val code: String,
    val progress: Int = 0,
    val target: Int = 1,
    val unlocked: Boolean = false,
    @ColumnInfo(name = "unlocked_at") val unlockedAt: Long = 0L,
)

// ─── Quests ──────────────────────────────────────────────────────────────────

@Entity(tableName = "quest_state", primaryKeys = ["quest_id", "cycle_key"])
data class QuestState(
    @ColumnInfo(name = "quest_id") val questId: String,
    @ColumnInfo(name = "cycle_key") val cycleKey: String,
    val progress: Int = 0,
    val target: Int = 1,
    val completed: Boolean = false,
    @ColumnInfo(name = "completed_at") val completedAt: Long = 0L,
)

// ─── NPCs ────────────────────────────────────────────────────────────────────

@Entity(
    tableName = "npcs",
    indices = [Index(value = ["name_key"], unique = true)],
)
data class Npc(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "name_key") val nameKey: String,
    val name: String,
    val title: String = "",

    /** Archetype key, see NpcArchetypes. */
    val archetype: String,
    val traitCsv: String = "",

    val level: Int = 1,
    val xp: Int = 0,

    /** Patience / HP. Decreases while they are owed money and overdue. */
    val patience: Int = 100,
    val maxPatience: Int = 100,

    /** -100 (nemesis) .. +100 (sworn ally). */
    val relation: Int = 0,

    val mood: NpcMood = NpcMood.NEUTRAL,

    val trackedDebts: Int = 0,
    val openDebts: Int = 0,
    val settledDebts: Int = 0,
    val overdueDebts: Int = 0,

    val owedToThem: Int = 0,
    val paidToThem: Int = 0,
    val theyOweYou: Int = 0,
    val collectedFromThem: Int = 0,

    val levelUps: Int = 0,
    val rages: Int = 0,
    val nudgesToday: Int = 0,

    @ColumnInfo(name = "nudged_day") val nudgedDay: Long = 0L,
    @ColumnInfo(name = "spawned_at") val spawnedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_seen_at") val lastSeenAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_level_at") val lastLevelAt: Long = 0L,
) {
    val isPacified: Boolean get() = settledDebts > 0 && openDebts == 0
    val isWiped: Boolean get() = trackedDebts > 0 && openDebts == 0
    val patiencePercent: Int get() = if (maxPatience <= 0) 0 else (patience * 100) / maxPatience
    val isEnraged: Boolean get() = patience <= 25
}

enum class NpcMood { NEUTRAL, ANGRY, DESPERATE, PATIENT, GRATEFUL, AFRAID, CHEERFUL, BROKE }

// ─── Battle log ──────────────────────────────────────────────────────────────

@Entity(tableName = "game_log", indices = [Index(value = ["at"])])
data class GameLogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val kind: LogKind = LogKind.REWARD,
    val title: String,
    val detail: String = "",
    val iconSeed: String = "",
    val penalty: Boolean = false,
    val at: Long = System.currentTimeMillis(),
    val read: Boolean = false,
)

enum class LogKind { REWARD, PENALTY, ACHIEVEMENT, QUEST, LEVEL, NPC, SYSTEM }

// ─── Rollover bookkeeping ────────────────────────────────────────────────────

/**
 * Single-row table that records which daily / monthly / annual quest cycle is
 * currently open, plus the last rollover day, so cycles rotate exactly once
 * per boundary no matter how often the app is opened.
 */
@Entity(tableName = "rollover_state")
data class RolloverState(
    @PrimaryKey
    val id: Int = SINGLETON,
    @ColumnInfo(name = "last_rollover_day") val lastRolloverDay: Long = 0L,
    @ColumnInfo(name = "daily_cycle") val dailyCycle: String = "",
    @ColumnInfo(name = "monthly_cycle") val monthlyCycle: String = "",
    @ColumnInfo(name = "annual_cycle") val annualCycle: String = "",
) {
    companion object {
        const val SINGLETON = 1
    }
}
