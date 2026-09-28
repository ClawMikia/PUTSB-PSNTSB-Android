package com.cyberpunk.debttracker.ui.game

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.data.model.game.AchievementState
import com.cyberpunk.debttracker.data.model.game.GameLogEntry
import com.cyberpunk.debttracker.data.model.game.LogKind
import com.cyberpunk.debttracker.data.model.game.Npc
import com.cyberpunk.debttracker.data.model.game.NpcMood
import com.cyberpunk.debttracker.data.model.game.QuestState
import com.cyberpunk.debttracker.databinding.ItemAchievementBinding
import com.cyberpunk.debttracker.databinding.ItemLogBinding
import com.cyberpunk.debttracker.databinding.ItemNpcBinding
import com.cyberpunk.debttracker.databinding.ItemQuestBinding
import com.cyberpunk.debttracker.game.AchievementCatalog
import com.cyberpunk.debttracker.game.AchievementDef
import com.cyberpunk.debttracker.game.Cycles
import com.cyberpunk.debttracker.game.GameEngine
import com.cyberpunk.debttracker.game.IconForge
import com.cyberpunk.debttracker.game.NpcArchetype
import com.cyberpunk.debttracker.game.QuestCatalog
import com.cyberpunk.debttracker.game.QuestCycle
import com.cyberpunk.debttracker.game.QuestDef
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Shared number formatting so every screen reads the same. */
internal object GameFormat {
    fun peso(value: Int): String = "₱" + String.format(Locale.US, "%,d", value)
    fun short(value: Int): String = when {
        value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000.0)
        value >= 10_000 -> String.format(Locale.US, "%.0fK", value / 1_000.0)
        value >= 1_000 -> String.format(Locale.US, "%,d", value)
        else -> value.toString()
    }
    private val time = SimpleDateFormat("HH:mm", Locale.US)
    fun time(millis: Long): String = time.format(Date(millis))
}

private fun Int.progressPercent(target: Int): Int =
    if (target <= 0) 100 else ((this * 100) / target).coerceIn(0, 100)

// ═══════════════════════════════════════════════════════════════════════════
//  QUESTS
// ═══════════════════════════════════════════════════════════════════════════

data class QuestRow(val def: QuestDef, val state: QuestState?)

class QuestAdapter : ListAdapter<QuestRow, QuestAdapter.Holder>(DIFF) {

    class Holder(val binding: ItemQuestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemQuestBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val row = getItem(position)
        val binding = holder.binding
        val context = binding.root.context
        val def = row.def
        val done = row.state?.completed == true
        val progress = row.state?.progress ?: 0

        binding.tvQuestTitle.text = if (done) def.title else def.title
        binding.tvQuestTitle.alpha = if (done) 1f else 0.92f
        binding.tvQuestDesc.text = def.description
        binding.ivQuestBear.setImageDrawable(IconForge.bear(def.id))
        binding.ivQuestBear.alpha = if (done) 1f else 0.55f

        binding.progressQuest.progress = progress.progressPercent(def.target)
        binding.progressQuest.setIndicatorColor(
            context.getColor(if (done) R.color.neon_green_ok else R.color.cyber_gold),
        )
        binding.tvQuestProgress.text = if (done) {
            context.getString(R.string.game_quest_progress, def.target, def.target)
        } else {
            context.getString(R.string.game_quest_progress, progress, def.target)
        }
        binding.tvQuestReward.text = buildString {
            append("+").append(def.xp).append(" XP")
            if (def.coins > 0) append("  +").append(GameFormat.short(def.coins)).append("₱")
            if (def.bolts > 0) append("  +").append(def.bolts).append("⚡")
        }
        binding.root.alpha = if (done) 0.95f else 1f
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<QuestRow>() {
            override fun areItemsTheSame(a: QuestRow, b: QuestRow) = a.def.id == b.def.id
            override fun areContentsTheSame(a: QuestRow, b: QuestRow) = a.def == b.def && a.state == b.state
        }
    }
}

fun questRows(cycle: QuestCycle, states: List<QuestState>): List<QuestRow> {
    val byId = states.associateBy { it.questId }
    return QuestCatalog.forCycle(cycle).map { QuestRow(it, byId[it.id]) }
}

// ═══════════════════════════════════════════════════════════════════════════
//  ACHIEVEMENTS
// ═══════════════════════════════════════════════════════════════════════════

data class AchievementRow(val def: AchievementDef, val state: AchievementState?)

class AchievementAdapter : ListAdapter<AchievementRow, AchievementAdapter.Holder>(DIFF) {

    class Holder(val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val row = getItem(position)
        val binding = holder.binding
        val context = binding.root.context
        val def = row.def
        val unlocked = row.state?.unlocked == true

        binding.tvAchievementTitle.text = def.title
        binding.tvAchievementDesc.text = def.description
        binding.tvAchievementTier.text = def.tier.label
        binding.ivAchievementBear.setImageDrawable(IconForge.bear(def.code))
        binding.ivAchievementBear.alpha = if (unlocked) 1f else 0.3f

        if (unlocked) {
            binding.progressAchievement.progress = 100
            binding.tvAchievementProgress.setText(R.string.game_locked_placeholder)
            binding.tvAchievementProgress.setTextColor(context.getColor(R.color.neon_green_ok))
        } else {
            val progress = row.state?.progress ?: 0
            binding.progressAchievement.progress = progress.progressPercent(def.target)
            binding.tvAchievementProgress.text =
                context.getString(R.string.game_achievement_progress, progress, def.target)
            binding.tvAchievementProgress.setTextColor(context.getColor(R.color.text_secondary))
        }
        binding.progressAchievement.setIndicatorColor(
            context.getColor(if (unlocked) R.color.neon_green_ok else R.color.cyber_gold),
        )
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<AchievementRow>() {
            override fun areItemsTheSame(a: AchievementRow, b: AchievementRow) = a.def.code == b.def.code
            override fun areContentsTheSame(a: AchievementRow, b: AchievementRow) =
                a.def == b.def && a.state == b.state
        }
    }
}

fun achievementRows(states: List<AchievementState>): List<AchievementRow> {
    val byCode = states.associateBy { it.code }
    return AchievementCatalog.all.map { AchievementRow(it, byCode[it.code]) }
}

// ═══════════════════════════════════════════════════════════════════════════
//  NPCs
// ═══════════════════════════════════════════════════════════════════════════

class NpcAdapter(
    private val onNudge: (Npc) -> Unit,
    private val onPacify: (Npc) -> Unit,
    private val onConfront: (Npc) -> Unit,
) : ListAdapter<Npc, NpcAdapter.Holder>(DIFF) {

    /**
     * Live wallet balance, mirrored from the profile so the buttons can show
     * whether PACIFY is actually affordable instead of failing on tap.
     */
    var coins: Int = 0
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    class Holder(val binding: ItemNpcBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemNpcBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val npc = getItem(position)
        val binding = holder.binding
        val context = binding.root.context
        val archetype = NpcArchetype.byKey(npc.archetype)

        binding.tvNpcAvatar.text = npc.name.take(1).uppercase()
        binding.tvNpcName.text = npc.name
        binding.tvNpcTitle.text = "${archetype.name.uppercase()} · ${archetype.role}"
        binding.tvNpcSpeech.text = when (npc.mood) {
            NpcMood.GRATEFUL -> archetype.happy
            NpcMood.BROKE -> archetype.broken
            NpcMood.ANGRY -> archetype.angry
            else -> archetype.idle
        }
        binding.tvNpcMood.text = npc.mood.name
        binding.tvNpcMood.setTextColor(context.getColor(moodColor(npc.mood)))

        binding.tvNpcLevel.text = context.getString(R.string.game_npc_npc_level, npc.level)
        binding.tvNpcPatience.text = context.getString(R.string.game_patience_format, npc.patiencePercent)
        binding.tvNpcPatience.setTextColor(
            context.getColor(
                when {
                    npc.patiencePercent <= 25 -> R.color.neon_red_alert
                    npc.patiencePercent <= 50 -> R.color.debt_partial
                    else -> R.color.text_secondary
                },
            ),
        )

        val need = GameEngine.npcXpToNext(npc.level)
        binding.progressNpc.progress = (npc.xp * 100 / need.coerceAtLeast(1)).coerceIn(0, 100)

        binding.tvNpcDebt.text = buildString {
            if (npc.isPacified) append(context.getString(R.string.game_npc_pacified)).append(" · ")
            append(context.getString(R.string.game_npc_open_debts, npc.openDebts))
            if (npc.overdueDebts > 0) {
                append("  ")
                append(context.getString(R.string.game_npc_overdue, npc.overdueDebts))
            }
        }

        // NUDGE is a once-per-day courtesy. Once used, show that plainly instead
        // of leaving a dead button that silently rejects the tap.
        val nudgedToday = npc.nudgedDay == Cycles.epochDay()
        binding.btnNudge.setOnClickListener { onNudge(npc) }
        binding.btnNudge.isEnabled = !nudgedToday
        binding.btnNudge.alpha = if (nudgedToday) DISABLED_ALPHA else 1f
        binding.btnNudge.setText(
            if (nudgedToday) R.string.game_action_nudged_today else R.string.game_action_nudge,
        )

        // PACIFY spends coins, so it is only enabled when the wallet can cover it.
        val canAfford = coins >= GameEngine.PACIFY_COST
        binding.btnPacify.setOnClickListener { onPacify(npc) }
        binding.btnPacify.isEnabled = canAfford
        binding.btnPacify.alpha = if (canAfford) 1f else DISABLED_ALPHA
        binding.btnPacify.text = if (canAfford) {
            context.getString(R.string.game_pacify_cost, GameEngine.PACIFY_COST)
        } else {
            context.getString(R.string.game_pacify_cost_short, GameEngine.PACIFY_COST)
        }

        // CONFRONT has no cost, it just bleeds the relationship.
        binding.btnConfront.setOnClickListener { onConfront(npc) }
    }

    private fun moodColor(mood: NpcMood): Int = when (mood) {
        NpcMood.GRATEFUL -> R.color.neon_green_ok
        NpcMood.CHEERFUL -> R.color.neon_green_ok
        NpcMood.PATIENT -> R.color.text_secondary
        NpcMood.NEUTRAL -> R.color.text_secondary
        NpcMood.AFRAID -> R.color.debt_partial
        NpcMood.DESPERATE -> R.color.neon_amber
        NpcMood.ANGRY -> R.color.neon_red_alert
        NpcMood.BROKE -> R.color.text_tertiary
    }

    private companion object {
        const val DISABLED_ALPHA = 0.4f

        val DIFF = object : DiffUtil.ItemCallback<Npc>() {
            override fun areItemsTheSame(a: Npc, b: Npc) = a.id == b.id
            override fun areContentsTheSame(a: Npc, b: Npc) = a == b
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  BATTLE LOG
// ═══════════════════════════════════════════════════════════════════════════

class LogAdapter : ListAdapter<GameLogEntry, LogAdapter.Holder>(DIFF) {

    class Holder(val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val entry = getItem(position)
        val binding = holder.binding
        val context = binding.root.context
        val isPenalty = entry.kind == LogKind.PENALTY || entry.penalty

        binding.tvLogTitle.text = entry.title
        binding.tvLogTitle.setTextColor(
            context.getColor(if (isPenalty) R.color.neon_red_alert else R.color.text_primary),
        )
        binding.tvLogDetail.text = entry.detail
        binding.tvLogTime.text = GameFormat.time(entry.at)
        binding.ivLogIcon.setImageDrawable(
            IconForge.skull(entry.iconSeed).also { drawable ->
                if (isPenalty) drawable.setAlpha(255) else drawable.alpha = 170
            },
        )
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<GameLogEntry>() {
            override fun areItemsTheSame(a: GameLogEntry, b: GameLogEntry) = a.id == b.id
            override fun areContentsTheSame(a: GameLogEntry, b: GameLogEntry) = a == b
        }
    }
}
