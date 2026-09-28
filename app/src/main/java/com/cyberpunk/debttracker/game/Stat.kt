package com.cyberpunk.debttracker.game

/**
 * Every gameplay stat the engine tracks.
 *
 * COUNTER keys are lifetime monotonic totals — quest progress inside a cycle
 * is (current value − value recorded when the cycle opened).
 *
 * GAUGE keys are instantaneous snapshots recomputed from the live debt table
 * on every evaluation. Quests with [QuestGoalType.REDUCE] watch gauges.
 */
object Stat {

    // ─── COUNTERS: debt lifecycle ─────────────────────────────────────────────
    const val DEBTS_ADDED = "debts_added"
    const val DEBTS_DELETED = "debts_deleted"
    const val DEBTS_SETTLED = "debts_settled"
    const val PAYMENTS_RECORDED = "payments_recorded"
    const val VALUE_SETTLED = "value_settled"
    const val VALUE_ADDED = "value_added"
    const val VALUE_OWED_CLEARED = "value_owed_cleared"
    const val VALUE_LENT_COLLECTED = "value_lent_collected"
    const val LARGEST_SETTLEMENT = "largest_settlement"
    const val LARGEST_NODE = "largest_node"
    const val OVERDUE_PAID = "overdue_paid"
    const val ONTIME_PAID = "ontime_paid"
    const val PERFECT_NODE = "perfect_node"
    const val PARTIAL_TO_FULL = "partial_to_full"
    const val OWE_SIDE_SETTLED = "owe_side_settled"
    const val LENT_SIDE_SETTLED = "lent_side_settled"
    const val BIG_DAY = "big_day"
    const val TRIPLE_DAY = "triple_day"
    const val ALL_SEVEN_DONE = "all_seven_daily"
    const val CLEAN_DAYS = "clean_days"
    const val STREAK_BEST = "streak_best"

    // ─── COUNTERS: ledger actions ─────────────────────────────────────────────
    const val IMPORTS = "imports_performed"
    const val IMPORTED_NODES = "nodes_imported"
    const val EXPORTS = "exports_performed"
    const val ARCHIVES = "archives_performed"
    const val DETAIL_VISITS = "detail_visits"
    const val ANALYTICS_VISITS = "analytics_visits"
    const val DAYS_PLAYED = "days_played"

    // ─── COUNTERS: NPCs ───────────────────────────────────────────────────────
    const val NPCS_RECRUITED = "npcs_recruited"
    const val NPCS_PACIFIED = "npcs_pacified"
    const val NPCS_WIPED = "npcs_wiped"
    const val NPCS_REACHED_MAX = "npcs_reached_max"
    const val NPC_LEVEL_UPS = "npc_level_ups"
    const val NPC_TOP_LEVEL = "npc_top_level"
    const val NPC_RAGES = "npc_rages"
    const val NPC_RAGE_CLEARED = "npc_rage_cleared"
    const val NPC_NUDGES = "npc_nudges"
    const val NPC_PACIFIES = "npc_pacifies"
    const val NPC_CONFRONTATIONS = "npc_confrontations"
    const val NPC_DEBT_CLEARED = "npc_debt_cleared"
    const val NPC_SETTLE = "npc_settle"

    // ─── COUNTERS: progression ────────────────────────────────────────────────
    const val REWARDS_RECEIVED = "rewards_received"
    const val PENALTIES_RECEIVED = "penalties_received"
    const val COINS_EARNED = "coins_earned"
    const val COINS_SPENT = "coins_spent"
    const val BOLTS_EARNED = "bolts_earned"
    const val BOLTS_SPENT = "bolts_spent"
    const val LEVEL_UPS = "level_ups"
    const val TOP_LEVEL = "top_level"

    // ─── COUNTERS: quests & achievements ──────────────────────────────────────
    const val QUESTS_DAILY_DONE = "quests_daily_done"
    const val QUESTS_MONTHLY_DONE = "quests_monthly_done"
    const val QUESTS_ANNUAL_DONE = "quests_annual_done"
    const val QUESTS_TOTAL_DONE = "quests_total_done"
    const val QUESTS_DAILY_ALL = "quests_daily_all"
    const val MONTHS_CLEARED = "months_cleared"
    const val YEARS_CLEARED = "years_cleared"
    const val ACHIEVEMENTS_UNLOCKED = "achievements_unlocked"
    const val ACH_SAME_TIER_10 = "ach_tier_10"
    const val ACH_SAME_TIER_25 = "ach_tier_25"
    const val ACH_SAME_TIER_50 = "ach_tier_50"
    const val ACH_SAME_TIER_100 = "ach_tier_100"
    const val NERVE_FULL = "nerve_full"
    const val NERVE_EMPTY = "nerve_empty"

    // ─── GAUGES ───────────────────────────────────────────────────────────────
    const val G_ACTIVE = "g_active"
    const val G_OVERDUE = "g_overdue"
    const val G_OPEN_OWE = "g_open_owe"
    const val G_OPEN_LENT = "g_open_lent"
    const val G_OVERDUE_OWE = "g_overdue_owe"
    const val G_OVERDUE_LENT = "g_overdue_lent"
    const val G_NET_OWED = "g_net_owed"
    const val G_NERVE = "g_nerve"
    const val G_NPCS = "g_npcs"
    const val G_NPCS_ANGRY = "g_npcs_angry"
    const val G_NPC_DEBTS = "g_npc_debts"
    const val G_ACTIVE_TODAY = "g_active_today"

    /** Everything the engine bumps through [GameEngine.addCounter]. */
    val ALL_COUNTERS: List<String> = listOf(
        DEBTS_ADDED, DEBTS_DELETED, DEBTS_SETTLED, PAYMENTS_RECORDED,
        VALUE_SETTLED, VALUE_ADDED, VALUE_OWED_CLEARED, VALUE_LENT_COLLECTED,
        LARGEST_SETTLEMENT, LARGEST_NODE, OVERDUE_PAID, ONTIME_PAID,
        PERFECT_NODE, PARTIAL_TO_FULL, OWE_SIDE_SETTLED, LENT_SIDE_SETTLED,
        BIG_DAY, TRIPLE_DAY, ALL_SEVEN_DONE, CLEAN_DAYS, STREAK_BEST,
        IMPORTS, IMPORTED_NODES, EXPORTS, ARCHIVES, DETAIL_VISITS,
        ANALYTICS_VISITS, DAYS_PLAYED,
        NPCS_RECRUITED, NPCS_PACIFIED, NPCS_WIPED, NPCS_REACHED_MAX,
        NPC_LEVEL_UPS, NPC_TOP_LEVEL, NPC_RAGES, NPC_RAGE_CLEARED,
        NPC_NUDGES, NPC_PACIFIES, NPC_CONFRONTATIONS, NPC_DEBT_CLEARED, NPC_SETTLE,
        REWARDS_RECEIVED, PENALTIES_RECEIVED, COINS_EARNED, COINS_SPENT,
        BOLTS_EARNED, BOLTS_SPENT, LEVEL_UPS, TOP_LEVEL,
        QUESTS_DAILY_DONE, QUESTS_MONTHLY_DONE, QUESTS_ANNUAL_DONE,
        QUESTS_TOTAL_DONE, QUESTS_DAILY_ALL, MONTHS_CLEARED, YEARS_CLEARED,
        ACHIEVEMENTS_UNLOCKED, ACH_SAME_TIER_10, ACH_SAME_TIER_25,
        ACH_SAME_TIER_50, ACH_SAME_TIER_100, NERVE_FULL, NERVE_EMPTY,
    )

    val ALL_GAUGES: List<String> = listOf(
        G_ACTIVE, G_OVERDUE, G_OPEN_OWE, G_OPEN_LENT, G_OVERDUE_OWE,
        G_OVERDUE_LENT, G_NET_OWED, G_NERVE, G_NPCS, G_NPCS_ANGRY,
        G_NPC_DEBTS, G_ACTIVE_TODAY,
    )

    fun label(key: String): String = when (key) {
        G_ACTIVE -> "active nodes"
        G_OVERDUE -> "overdue nodes"
        G_OPEN_OWE -> "you owe"
        G_OPEN_LENT -> "owed to you"
        G_OVERDUE_OWE -> "overdue you-owe"
        G_OVERDUE_LENT -> "overdue owed-to-you"
        G_NET_OWED -> "net owed to you"
        G_NERVE -> "nerve"
        G_NPCS -> "known NPCs"
        G_NPCS_ANGRY -> "enraged NPCs"
        G_NPC_DEBTS -> "NPC debt nodes"
        G_ACTIVE_TODAY -> "settlements today"
        else -> key.replace('_', ' ')
    }
}
