package com.cyberpunk.debttracker.game

// ═══════════════════════════════════════════════════════════════════════════
//  QUEST CATALOG
//
//  DAILY   — 7 contracts, rotate every local midnight
//  MONTHLY — 30 operations, rotate on the 1st of each month
//  ANNUAL  — 100 campaign objectives, rotate on January 1st
//
//  Each quest has its own unique teddy-bear head (IconForge.bear(id)).
// ═══════════════════════════════════════════════════════════════════════════

enum class QuestGoalType {
    /** Accumulate `target` units of a counter inside this cycle. */
    REACH,

    /** Counter/level that must merely be at least `target` (baseline ignored). */
    ABSOLUTE,

    /** A gauge that must fall to `target` or lower. */
    REDUCE,
}

data class QuestDef(
    val id: String,
    val cycle: QuestCycle,
    val title: String,
    val description: String,
    val statKey: String,
    val target: Int,
    val goalType: QuestGoalType,
    val xp: Int,
    val coins: Int,
    val bolts: Int = 0,
)

object QuestCatalog {

    // ─── DAILY (7) ───────────────────────────────────────────────────────────

    val daily: List<QuestDef> = listOf(
        QuestDef("D01", QuestCycle.DAILY, "Daybreak Ledger",
            "Settle 1 debt node before the day ends.",
            Stat.DEBTS_SETTLED, 1, QuestGoalType.REACH, 90, 25),
        QuestDef("D02", QuestCycle.DAILY, "Tapped Installment",
            "Record 2 payments against any nodes.",
            Stat.PAYMENTS_RECORDED, 2, QuestGoalType.REACH, 80, 22),
        QuestDef("D03", QuestCycle.DAILY, "New Contacts",
            "Add 2 brand new debt nodes today.",
            Stat.DEBTS_ADDED, 2, QuestGoalType.REACH, 70, 20),
        QuestDef("D04", QuestCycle.DAILY, "Rush Hour",
            "Clear 1 node that is already past due.",
            Stat.OVERDUE_PAID, 1, QuestGoalType.REACH, 120, 32),
        QuestDef("D05", QuestCycle.DAILY, "Heavy Hitter",
            "Settle \u20B15,000 or more in a single day.",
            Stat.VALUE_SETTLED, 5_000, QuestGoalType.REACH, 130, 35),
        QuestDef("D06", QuestCycle.DAILY, "Double Kill",
            "Settle 2 debt nodes today.",
            Stat.DEBTS_SETTLED, 2, QuestGoalType.REACH, 150, 40),
        QuestDef("D07", QuestCycle.DAILY, "Sweep the Floor",
            "Bring your overdue node count down to zero.",
            Stat.G_OVERDUE, 0, QuestGoalType.REDUCE, 160, 45),
    )

    // ─── MONTHLY (30) ────────────────────────────────────────────────────────

    val monthly: List<QuestDef> = listOf(
        QuestDef("M01", QuestCycle.MONTHLY, "Operation Clean Sweep",
            "Settle 10 debt nodes this month.",
            Stat.DEBTS_SETTLED, 10, QuestGoalType.REACH, 420, 110),
        QuestDef("M02", QuestCycle.MONTHLY, "Cash Extraction",
            "Settle \u20B150,000 worth of debt this month.",
            Stat.VALUE_SETTLED, 50_000, QuestGoalType.REACH, 460, 120),
        QuestDef("M03", QuestCycle.MONTHLY, "Installment Grind",
            "Record 20 payments this month.",
            Stat.PAYMENTS_RECORDED, 20, QuestGoalType.REACH, 380, 100),
        QuestDef("M04", QuestCycle.MONTHLY, "Contact Expansion",
            "Add 10 new debt nodes this month.",
            Stat.DEBTS_ADDED, 10, QuestGoalType.REACH, 300, 80),
        QuestDef("M05", QuestCycle.MONTHLY, "Clock Dodger",
            "Settle 3 nodes that had already gone past due.",
            Stat.OVERDUE_PAID, 3, QuestGoalType.REACH, 520, 140),
        QuestDef("M06", QuestCycle.MONTHLY, "Reconciler",
            "Clear \u20B115,000 of the money YOU owe.",
            Stat.VALUE_OWED_CLEARED, 15_000, QuestGoalType.REACH, 480, 125),
        QuestDef("M07", QuestCycle.MONTHLY, "Collector Run",
            "Collect \u20B115,000 that was owed to you.",
            Stat.VALUE_LENT_COLLECTED, 15_000, QuestGoalType.REACH, 480, 125),
        QuestDef("M08", QuestCycle.MONTHLY, "Recruitment Drive",
            "Recruit 5 new NPCs into the matrix.",
            Stat.NPCS_RECRUITED, 5, QuestGoalType.REACH, 340, 90),
        QuestDef("M09", QuestCycle.MONTHLY, "Diplomacy",
            "Fully settle the debts of 3 NPCs.",
            Stat.NPCS_PACIFIED, 3, QuestGoalType.REACH, 560, 150),
        QuestDef("M10", QuestCycle.MONTHLY, "De-escalation",
            "Talk 3 enraged NPCs back down to calm.",
            Stat.NPC_RAGE_CLEARED, 3, QuestGoalType.REACH, 540, 145),
        QuestDef("M11", QuestCycle.MONTHLY, "Bonding",
            "Earn 15 NPC level-ups this month.",
            Stat.NPC_LEVEL_UPS, 15, QuestGoalType.REACH, 500, 135),
        QuestDef("M12", QuestCycle.MONTHLY, "Contract Machine",
            "Complete 20 daily contracts this month.",
            Stat.QUESTS_DAILY_DONE, 20, QuestGoalType.REACH, 440, 115),
        QuestDef("M13", QuestCycle.MONTHLY, "Operation Veteran",
            "Complete 5 monthly operations.",
            Stat.QUESTS_MONTHLY_DONE, 5, QuestGoalType.REACH, 600, 160),
        QuestDef("M14", QuestCycle.MONTHLY, "Nothing Late",
            "Bring your overdue node count to zero.",
            Stat.G_OVERDUE, 0, QuestGoalType.REDUCE, 560, 150),
        QuestDef("M15", QuestCycle.MONTHLY, "No Loose Ends",
            "Clear your active node list down to zero.",
            Stat.G_ACTIVE, 0, QuestGoalType.REDUCE, 820, 230),
        QuestDef("M16", QuestCycle.MONTHLY, "Owes-Me Zero",
            "Collect everything that was owed to you.",
            Stat.G_OPEN_LENT, 0, QuestGoalType.REDUCE, 780, 215),
        QuestDef("M17", QuestCycle.MONTHLY, "I-Owe Zero",
            "Settle everything you owe anyone.",
            Stat.G_OPEN_OWE, 0, QuestGoalType.REDUCE, 780, 215),
        QuestDef("M18", QuestCycle.MONTHLY, "Heavy Purge",
            "Settle \u20B1200,000 worth of debt this month.",
            Stat.VALUE_SETTLED, 200_000, QuestGoalType.REACH, 700, 190),
        QuestDef("M19", QuestCycle.MONTHLY, "Whale Watch",
            "Settle a single node worth \u20B1100,000 or more.",
            Stat.LARGEST_SETTLEMENT, 100_000, QuestGoalType.ABSOLUTE, 760, 205),
        QuestDef("M20", QuestCycle.MONTHLY, "Follow Through",
            "Finish 8 nodes that you had started paying in instalments.",
            Stat.PARTIAL_TO_FULL, 8, QuestGoalType.REACH, 480, 125),
        QuestDef("M21", QuestCycle.MONTHLY, "Spotless",
            "Record 5 settlements made ahead of schedule.",
            Stat.PERFECT_NODE, 5, QuestGoalType.REACH, 460, 120),
        QuestDef("M22", QuestCycle.MONTHLY, "Overdue Slayer",
            "Settle 8 nodes that had already gone past due.",
            Stat.OVERDUE_PAID, 8, QuestGoalType.REACH, 640, 175),
        QuestDef("M23", QuestCycle.MONTHLY, "Teddy Collector",
            "Collect 30 teddy-bear rewards this month.",
            Stat.REWARDS_RECEIVED, 30, QuestGoalType.REACH, 360, 95),
        QuestDef("M24", QuestCycle.MONTHLY, "Survivor",
            "Take 10 penalties this month and still stand.",
            Stat.PENALTIES_RECEIVED, 10, QuestGoalType.REACH, 340, 90),
        QuestDef("M25", QuestCycle.MONTHLY, "Nerve Up",
            "Restore your nerve to maximum twice this month.",
            Stat.NERVE_FULL, 2, QuestGoalType.REACH, 400, 105),
        QuestDef("M26", QuestCycle.MONTHLY, "Level Surge",
            "Reach level 15.",
            Stat.TOP_LEVEL, 15, QuestGoalType.ABSOLUTE, 620, 165),
        QuestDef("M27", QuestCycle.MONTHLY, "Achievement Hunter",
            "Unlock 8 achievements this month.",
            Stat.ACHIEVEMENTS_UNLOCKED, 8, QuestGoalType.REACH, 420, 115),
        QuestDef("M28", QuestCycle.MONTHLY, "In The Black",
            "Get your net balance to zero or better.",
            Stat.G_NET_OWED, 0, QuestGoalType.REDUCE, 680, 185),
        QuestDef("M29", QuestCycle.MONTHLY, "Ledger Hygiene",
            "Archive 3 settled nodes into the vault.",
            Stat.ARCHIVES, 3, QuestGoalType.REACH, 320, 85),
        QuestDef("M30", QuestCycle.MONTHLY, "Full Contract",
            "Complete 25 daily contracts this month.",
            Stat.QUESTS_DAILY_DONE, 25, QuestGoalType.REACH, 520, 145),
    )

    // ─── ANNUAL (100) ────────────────────────────────────────────────────────

    val annual: List<QuestDef> = listOf(
        // Block 1 — volume
        QuestDef("Y001", QuestCycle.ANNUAL, "Century of Settlements", "Settle 100 debt nodes this year.", Stat.DEBTS_SETTLED, 100, QuestGoalType.REACH, 900, 260),
        QuestDef("Y002", QuestCycle.ANNUAL, "Double Century", "Settle 200 debt nodes this year.", Stat.DEBTS_SETTLED, 200, QuestGoalType.REACH, 1400, 420),
        QuestDef("Y003", QuestCycle.ANNUAL, "Five Hundred Clean", "Settle 500 debt nodes this year.", Stat.DEBTS_SETTLED, 500, QuestGoalType.REACH, 3200, 980, 2),
        QuestDef("Y004", QuestCycle.ANNUAL, "Million in the Bag", "Settle \u20B11,000,000 this year.", Stat.VALUE_SETTLED, 1_000_000, QuestGoalType.REACH, 1500, 450),
        QuestDef("Y005", QuestCycle.ANNUAL, "Five Million Purge", "Settle \u20B15,000,000 this year.", Stat.VALUE_SETTLED, 5_000_000, QuestGoalType.REACH, 3000, 900, 2),
        QuestDef("Y006", QuestCycle.ANNUAL, "Ten Million Clean", "Settle \u20B110,000,000 this year.", Stat.VALUE_SETTLED, 10_000_000, QuestGoalType.REACH, 6000, 1800, 4),
        QuestDef("Y007", QuestCycle.ANNUAL, "Millionaire Node", "Settle a single node worth \u20B11,000,000 or more.", Stat.LARGEST_SETTLEMENT, 1_000_000, QuestGoalType.ABSOLUTE, 5000, 1500, 3),
        QuestDef("Y008", QuestCycle.ANNUAL, "Two Hundred Terms", "Record 200 payments this year.", Stat.PAYMENTS_RECORDED, 200, QuestGoalType.REACH, 1200, 360),
        QuestDef("Y009", QuestCycle.ANNUAL, "Five Hundred Terms", "Record 500 payments this year.", Stat.PAYMENTS_RECORDED, 500, QuestGoalType.REACH, 2600, 800, 2),
        QuestDef("Y010", QuestCycle.ANNUAL, "One Thousand Terms", "Record 1,000 payments this year.", Stat.PAYMENTS_RECORDED, 1_000, QuestGoalType.REACH, 5200, 1600, 3),

        // Block 2 — overdue discipline
        QuestDef("Y011", QuestCycle.ANNUAL, "Twenty-Five Redlines", "Settle 25 nodes that had gone past due.", Stat.OVERDUE_PAID, 25, QuestGoalType.REACH, 1400, 420),
        QuestDef("Y012", QuestCycle.ANNUAL, "Fifty Redlines", "Settle 50 nodes that had gone past due.", Stat.OVERDUE_PAID, 50, QuestGoalType.REACH, 2800, 860, 2),
        QuestDef("Y013", QuestCycle.ANNUAL, "Hundred Redlines", "Settle 100 nodes that had gone past due.", Stat.OVERDUE_PAID, 100, QuestGoalType.REACH, 5500, 1700, 3),
        QuestDef("Y014", QuestCycle.ANNUAL, "Quarter Thousand Redlines", "Settle 250 nodes that had gone past due.", Stat.OVERDUE_PAID, 250, QuestGoalType.REACH, 11000, 3400, 5),
        QuestDef("Y015", QuestCycle.ANNUAL, "Thirty Clean Days", "Finish 30 days with zero overdue nodes.", Stat.CLEAN_DAYS, 30, QuestGoalType.REACH, 1300, 390),
        QuestDef("Y016", QuestCycle.ANNUAL, "Sixty Clean Days", "Finish 60 days with zero overdue nodes.", Stat.CLEAN_DAYS, 60, QuestGoalType.REACH, 2600, 780, 2),
        QuestDef("Y017", QuestCycle.ANNUAL, "Hundred Clean Days", "Finish 100 days with zero overdue nodes.", Stat.CLEAN_DAYS, 100, QuestGoalType.REACH, 4300, 1300, 3),
        QuestDef("Y018", QuestCycle.ANNUAL, "Year Without Late", "Finish 365 days with zero overdue nodes.", Stat.CLEAN_DAYS, 365, QuestGoalType.REACH, 12000, 3800, 6),
        QuestDef("Y019", QuestCycle.ANNUAL, "Twenty Spotless", "Record 20 settlements made early and exact.", Stat.PERFECT_NODE, 20, QuestGoalType.REACH, 1600, 480),
        QuestDef("Y020", QuestCycle.ANNUAL, "Hundred Spotless", "Record 100 settlements made early and exact.", Stat.PERFECT_NODE, 100, QuestGoalType.REACH, 6000, 1900, 4),

        // Block 3 — NPC network
        QuestDef("Y021", QuestCycle.ANNUAL, "Twenty-Five Contacts", "Recruit 25 NPCs this year.", Stat.NPCS_RECRUITED, 25, QuestGoalType.REACH, 1500, 450),
        QuestDef("Y022", QuestCycle.ANNUAL, "Fifty Contacts", "Recruit 50 NPCs this year.", Stat.NPCS_RECRUITED, 50, QuestGoalType.REACH, 3000, 900, 2),
        QuestDef("Y023", QuestCycle.ANNUAL, "Hundred Contacts", "Recruit 100 NPCs this year.", Stat.NPCS_RECRUITED, 100, QuestGoalType.REACH, 6500, 2000, 4),
        QuestDef("Y024", QuestCycle.ANNUAL, "Ten Pacified", "Fully settle the debts of 10 NPCs.", Stat.NPCS_PACIFIED, 10, QuestGoalType.REACH, 1800, 540),
        QuestDef("Y025", QuestCycle.ANNUAL, "Thirty Pacified", "Fully settle the debts of 30 NPCs.", Stat.NPCS_PACIFIED, 30, QuestGoalType.REACH, 4200, 1300, 2),
        QuestDef("Y026", QuestCycle.ANNUAL, "Seventy-Five Pacified", "Fully settle the debts of 75 NPCs.", Stat.NPCS_PACIFIED, 75, QuestGoalType.REACH, 9000, 2800, 4),
        QuestDef("Y027", QuestCycle.ANNUAL, "Ten Wiped Out", "Clear 10 NPCs off your ledger completely.", Stat.NPCS_WIPED, 10, QuestGoalType.REACH, 1900, 570),
        QuestDef("Y028", QuestCycle.ANNUAL, "Forty Wiped Out", "Clear 40 NPCs off your ledger completely.", Stat.NPCS_WIPED, 40, QuestGoalType.REACH, 5400, 1700, 3),
        QuestDef("Y029", QuestCycle.ANNUAL, "Bond of Ten", "Push an NPC to level 10.", Stat.NPC_TOP_LEVEL, 10, QuestGoalType.ABSOLUTE, 2200, 660),
        QuestDef("Y030", QuestCycle.ANNUAL, "Bond of Twenty-Five", "Push an NPC to level 25.", Stat.NPC_TOP_LEVEL, 25, QuestGoalType.ABSOLUTE, 6000, 1800, 3),

        // Block 4 — NPC bonds
        QuestDef("Y031", QuestCycle.ANNUAL, "Fifty Level-Ups", "Earn 50 NPC level-ups this year.", Stat.NPC_LEVEL_UPS, 50, QuestGoalType.REACH, 1500, 450),
        QuestDef("Y032", QuestCycle.ANNUAL, "Hundred Fifty Level-Ups", "Earn 150 NPC level-ups this year.", Stat.NPC_LEVEL_UPS, 150, QuestGoalType.REACH, 3800, 1150, 2),
        QuestDef("Y033", QuestCycle.ANNUAL, "Five Hundred Level-Ups", "Earn 500 NPC level-ups this year.", Stat.NPC_LEVEL_UPS, 500, QuestGoalType.REACH, 9000, 2800, 4),
        QuestDef("Y034", QuestCycle.ANNUAL, "Thirty De-Escalations", "Clear 30 NPC rages this year.", Stat.NPC_RAGE_CLEARED, 30, QuestGoalType.REACH, 2000, 600),
        QuestDef("Y035", QuestCycle.ANNUAL, "Hundred De-Escalations", "Clear 100 NPC rages this year.", Stat.NPC_RAGE_CLEARED, 100, QuestGoalType.REACH, 5600, 1700, 3),
        QuestDef("Y036", QuestCycle.ANNUAL, "Two Hundred Nudges", "Send 200 friendly reminders this year.", Stat.NPC_NUDGES, 200, QuestGoalType.REACH, 1300, 390),
        QuestDef("Y037", QuestCycle.ANNUAL, "Hundred Pacifications", "Spend coins to pacify 100 times this year.", Stat.NPC_PACIFIES, 100, QuestGoalType.REACH, 2600, 780, 2),
        QuestDef("Y038", QuestCycle.ANNUAL, "Hundred Confrontations", "Confront 100 NPCs this year. They remember.", Stat.NPC_CONFRONTATIONS, 100, QuestGoalType.REACH, 1900, 570),
        QuestDef("Y039", QuestCycle.ANNUAL, "Fifty NPC Settlements", "Settle 50 nodes belonging to NPCs.", Stat.NPC_SETTLE, 50, QuestGoalType.REACH, 2100, 630),
        QuestDef("Y040", QuestCycle.ANNUAL, "Two Hundred NPC Settlements", "Settle 200 nodes belonging to NPCs.", Stat.NPC_SETTLE, 200, QuestGoalType.REACH, 6000, 1800, 3),

        // Block 5 — quest mastery
        QuestDef("Y041", QuestCycle.ANNUAL, "Hundred Contracts", "Complete 100 daily quests this year.", Stat.QUESTS_DAILY_DONE, 100, QuestGoalType.REACH, 1400, 420),
        QuestDef("Y042", QuestCycle.ANNUAL, "Year of Contracts", "Complete 365 daily quests this year.", Stat.QUESTS_DAILY_DONE, 365, QuestGoalType.REACH, 4600, 1400, 3),
        QuestDef("Y043", QuestCycle.ANNUAL, "Two Years of Contracts", "Complete 730 daily quests this year.", Stat.QUESTS_DAILY_DONE, 730, QuestGoalType.REACH, 9000, 2800, 5),
        QuestDef("Y044", QuestCycle.ANNUAL, "Twelve Operations", "Complete 12 monthly quests this year.", Stat.QUESTS_MONTHLY_DONE, 12, QuestGoalType.REACH, 2200, 660),
        QuestDef("Y045", QuestCycle.ANNUAL, "Two Years of Operations", "Complete 24 monthly quests.", Stat.QUESTS_MONTHLY_DONE, 24, QuestGoalType.REACH, 4400, 1400, 3),
        QuestDef("Y046", QuestCycle.ANNUAL, "All Thirty Operations", "Complete 30 monthly quests.", Stat.QUESTS_MONTHLY_DONE, 30, QuestGoalType.ABSOLUTE, 8000, 2500, 4),
        QuestDef("Y047", QuestCycle.ANNUAL, "Five Campaigns", "Complete 5 annual quests.", Stat.QUESTS_ANNUAL_DONE, 5, QuestGoalType.REACH, 3000, 900, 2),
        QuestDef("Y048", QuestCycle.ANNUAL, "Fifteen Campaigns", "Complete 15 annual quests.", Stat.QUESTS_ANNUAL_DONE, 15, QuestGoalType.REACH, 7500, 2300, 4),
        QuestDef("Y049", QuestCycle.ANNUAL, "Thirty Campaigns", "Complete 30 annual quests.", Stat.QUESTS_ANNUAL_DONE, 30, QuestGoalType.REACH, 12000, 3800, 6),
        QuestDef("Y050", QuestCycle.ANNUAL, "Total Campaign Sweep", "Complete all 100 annual quests.", Stat.QUESTS_ANNUAL_DONE, 100, QuestGoalType.ABSOLUTE, 30000, 9000, 12),

        // Block 6 — streaks and days
        QuestDef("Y051", QuestCycle.ANNUAL, "Month of Iron", "Hold a 30-day streak.", Stat.STREAK_BEST, 30, QuestGoalType.ABSOLUTE, 1600, 480),
        QuestDef("Y052", QuestCycle.ANNUAL, "Two-Month Streak", "Hold a 60-day streak.", Stat.STREAK_BEST, 60, QuestGoalType.ABSOLUTE, 3200, 960, 2),
        QuestDef("Y053", QuestCycle.ANNUAL, "Century Streak", "Hold a 100-day streak.", Stat.STREAK_BEST, 100, QuestGoalType.ABSOLUTE, 5200, 1600, 3),
        QuestDef("Y054", QuestCycle.ANNUAL, "Two-Century Streak", "Hold a 200-day streak.", Stat.STREAK_BEST, 200, QuestGoalType.ABSOLUTE, 9000, 2800, 5),
        QuestDef("Y055", QuestCycle.ANNUAL, "Full Orbit", "Hold a 365-day streak.", Stat.STREAK_BEST, 365, QuestGoalType.ABSOLUTE, 20000, 6000, 8),
        QuestDef("Y056", QuestCycle.ANNUAL, "Two Orbits", "Hold a 730-day streak.", Stat.STREAK_BEST, 730, QuestGoalType.ABSOLUTE, 40000, 12000, 12),
        QuestDef("Y057", QuestCycle.ANNUAL, "Fifty Days Present", "Be active on 50 separate days.", Stat.DAYS_PLAYED, 50, QuestGoalType.REACH, 1200, 360),
        QuestDef("Y058", QuestCycle.ANNUAL, "Hundred Fifty Days Present", "Be active on 150 separate days.", Stat.DAYS_PLAYED, 150, QuestGoalType.REACH, 3000, 900, 2),
        QuestDef("Y059", QuestCycle.ANNUAL, "Three Hundred Days Present", "Be active on 300 separate days.", Stat.DAYS_PLAYED, 300, QuestGoalType.REACH, 5600, 1700, 3),
        QuestDef("Y060", QuestCycle.ANNUAL, "Every Day", "Be active on 365 separate days.", Stat.DAYS_PLAYED, 365, QuestGoalType.REACH, 12000, 3800, 6),

        // Block 7 — economy
        QuestDef("Y061", QuestCycle.ANNUAL, "Ten Thousand Coins", "Earn 10,000 coins this year.", Stat.COINS_EARNED, 10_000, QuestGoalType.REACH, 1600, 480),
        QuestDef("Y062", QuestCycle.ANNUAL, "Hundred Thousand Coins", "Earn 100,000 coins this year.", Stat.COINS_EARNED, 100_000, QuestGoalType.REACH, 4200, 1300, 2),
        QuestDef("Y063", QuestCycle.ANNUAL, "Half Million Coins", "Earn 500,000 coins this year.", Stat.COINS_EARNED, 500_000, QuestGoalType.REACH, 8000, 2500, 4),
        QuestDef("Y064", QuestCycle.ANNUAL, "Two Million Coins", "Earn 2,000,000 coins this year.", Stat.COINS_EARNED, 2_000_000, QuestGoalType.REACH, 14000, 4400, 6),
        QuestDef("Y065", QuestCycle.ANNUAL, "Five Bolts", "Collect 5 bolts this year.", Stat.BOLTS_EARNED, 5, QuestGoalType.REACH, 2800, 700),
        QuestDef("Y066", QuestCycle.ANNUAL, "Twenty Bolts", "Collect 20 bolts this year.", Stat.BOLTS_EARNED, 20, QuestGoalType.REACH, 9000, 2200, 2),
        QuestDef("Y067", QuestCycle.ANNUAL, "Fifty Bolts", "Collect 50 bolts this year.", Stat.BOLTS_EARNED, 50, QuestGoalType.REACH, 18000, 4500, 4),
        QuestDef("Y068", QuestCycle.ANNUAL, "Hundred Bolts", "Collect 100 bolts this year.", Stat.BOLTS_EARNED, 100, QuestGoalType.REACH, 32000, 8000, 8),
        QuestDef("Y069", QuestCycle.ANNUAL, "Thousand Teddy Bears", "Collect 1,000 rewards this year.", Stat.REWARDS_RECEIVED, 1_000, QuestGoalType.REACH, 4500, 1400, 3),
        QuestDef("Y070", QuestCycle.ANNUAL, "Five Thousand Teddy Bears", "Collect 5,000 rewards this year.", Stat.REWARDS_RECEIVED, 5_000, QuestGoalType.REACH, 15000, 5000, 6),

        // Block 8 — achievements and levels
        QuestDef("Y071", QuestCycle.ANNUAL, "Twenty Achievements", "Unlock 20 achievements.", Stat.ACHIEVEMENTS_UNLOCKED, 20, QuestGoalType.REACH, 1500, 450),
        QuestDef("Y072", QuestCycle.ANNUAL, "Forty Achievements", "Unlock 40 achievements.", Stat.ACHIEVEMENTS_UNLOCKED, 40, QuestGoalType.REACH, 3200, 960, 2),
        QuestDef("Y073", QuestCycle.ANNUAL, "Sixty Achievements", "Unlock 60 achievements.", Stat.ACHIEVEMENTS_UNLOCKED, 60, QuestGoalType.REACH, 5200, 1600, 3),
        QuestDef("Y074", QuestCycle.ANNUAL, "Eighty Achievements", "Unlock 80 achievements.", Stat.ACHIEVEMENTS_UNLOCKED, 80, QuestGoalType.REACH, 8000, 2500, 4),
        QuestDef("Y075", QuestCycle.ANNUAL, "Every Achievement", "Unlock all 100 achievements.", Stat.ACHIEVEMENTS_UNLOCKED, 100, QuestGoalType.ABSOLUTE, 20000, 6000, 8),
        QuestDef("Y076", QuestCycle.ANNUAL, "Level Thirty", "Reach level 30.", Stat.TOP_LEVEL, 30, QuestGoalType.ABSOLUTE, 2200, 660),
        QuestDef("Y077", QuestCycle.ANNUAL, "Level Fifty", "Reach level 50.", Stat.TOP_LEVEL, 50, QuestGoalType.ABSOLUTE, 4800, 1450, 2),
        QuestDef("Y078", QuestCycle.ANNUAL, "Level One Hundred", "Reach level 100.", Stat.TOP_LEVEL, 100, QuestGoalType.ABSOLUTE, 11000, 3400, 5),
        QuestDef("Y079", QuestCycle.ANNUAL, "Level Two Hundred", "Reach level 200.", Stat.TOP_LEVEL, 200, QuestGoalType.ABSOLUTE, 24000, 7200, 8),
        QuestDef("Y080", QuestCycle.ANNUAL, "Level Five Hundred", "Reach level 500.", Stat.TOP_LEVEL, 500, QuestGoalType.ABSOLUTE, 60000, 18000, 15),

        // Block 9 — ledger operations
        QuestDef("Y081", QuestCycle.ANNUAL, "Two Hundred Fifty Nodes", "Add 250 new debt nodes this year.", Stat.DEBTS_ADDED, 250, QuestGoalType.REACH, 1600, 480),
        QuestDef("Y082", QuestCycle.ANNUAL, "Five Hundred Nodes", "Add 500 new debt nodes this year.", Stat.DEBTS_ADDED, 500, QuestGoalType.REACH, 3400, 1000, 2),
        QuestDef("Y083", QuestCycle.ANNUAL, "One Thousand Nodes", "Add 1,000 new debt nodes this year.", Stat.DEBTS_ADDED, 1_000, QuestGoalType.REACH, 7000, 2200, 3),
        QuestDef("Y084", QuestCycle.ANNUAL, "Ten Imports", "Import a backup 10 times this year.", Stat.IMPORTS, 10, QuestGoalType.REACH, 1300, 390),
        QuestDef("Y085", QuestCycle.ANNUAL, "Twenty Five Exports", "Export your ledger 25 times this year.", Stat.EXPORTS, 25, QuestGoalType.REACH, 1500, 450),
        QuestDef("Y086", QuestCycle.ANNUAL, "Fifty Archives", "Archive 50 settled nodes this year.", Stat.ARCHIVES, 50, QuestGoalType.REACH, 2600, 800, 2),
        QuestDef("Y087", QuestCycle.ANNUAL, "Empty The List", "Reach zero active debt nodes.", Stat.G_ACTIVE, 0, QuestGoalType.REDUCE, 3000, 900, 2),
        QuestDef("Y088", QuestCycle.ANNUAL, "Nobody Angry", "Bring every enraged NPC down to calm.", Stat.G_NPCS_ANGRY, 0, QuestGoalType.REDUCE, 4200, 1300, 2),
        QuestDef("Y089", QuestCycle.ANNUAL, "Total Detachment", "Leave no open debt node attached to any NPC.", Stat.G_NPC_DEBTS, 0, QuestGoalType.REDUCE, 6500, 2000, 4),
        QuestDef("Y090", QuestCycle.ANNUAL, "Hundred Double Kills", "Have 100 days where you settled 2+ nodes.", Stat.BIG_DAY, 100, QuestGoalType.REACH, 5600, 1700, 3),

        // Block 10 — side-specific finals
        QuestDef("Y091", QuestCycle.ANNUAL, "Owe Side Quarter Million", "Clear \u20B1250,000 of the money you owe.", Stat.VALUE_OWED_CLEARED, 250_000, QuestGoalType.REACH, 2600, 780),
        QuestDef("Y092", QuestCycle.ANNUAL, "Owe Side Half Million", "Clear \u20B1500,000 of the money you owe.", Stat.VALUE_OWED_CLEARED, 500_000, QuestGoalType.REACH, 5200, 1600, 2),
        QuestDef("Y093", QuestCycle.ANNUAL, "Lent Side Quarter Million", "Collect \u20B1250,000 that was owed to you.", Stat.VALUE_LENT_COLLECTED, 250_000, QuestGoalType.REACH, 2600, 780),
        QuestDef("Y094", QuestCycle.ANNUAL, "Lent Side Half Million", "Collect \u20B1500,000 that was owed to you.", Stat.VALUE_LENT_COLLECTED, 500_000, QuestGoalType.REACH, 5200, 1600, 2),
        QuestDef("Y095", QuestCycle.ANNUAL, "Ten Owe-Side Sweeps", "Empty your entire I-Owe list 10 times over.", Stat.OWE_SIDE_SETTLED, 10, QuestGoalType.REACH, 6000, 1800, 3),
        QuestDef("Y096", QuestCycle.ANNUAL, "Ten Lent-Side Sweeps", "Collect your entire Owes-Me list 10 times over.", Stat.LENT_SIDE_SETTLED, 10, QuestGoalType.REACH, 6000, 1800, 3),
        QuestDef("Y097", QuestCycle.ANNUAL, "Thousand Skulls Survived", "Take 1,000 penalties and keep your nerve.", Stat.PENALTIES_RECEIVED, 1_000, QuestGoalType.REACH, 8000, 2500, 4),
        QuestDef("Y098", QuestCycle.ANNUAL, "Ten Thousand Skulls Survived", "Take 10,000 penalties. The ossuary is full.", Stat.PENALTIES_RECEIVED, 10_000, QuestGoalType.REACH, 25000, 7500, 10),
        QuestDef("Y099", QuestCycle.ANNUAL, "Year of Perfect Sweeps", "Complete all 7 daily quests on 100 separate days.", Stat.ALL_SEVEN_DONE, 100, QuestGoalType.REACH, 15000, 4800, 6),
        QuestDef("Y100", QuestCycle.ANNUAL, "The Whole Campaign", "Complete every single annual quest in one year.", Stat.YEARS_CLEARED, 1, QuestGoalType.ABSOLUTE, 100000, 30000, 25),
    )

    val byId: Map<String, QuestDef> = (daily + monthly + annual).associateBy { it.id }

    fun forCycle(cycle: QuestCycle): List<QuestDef> = when (cycle) {
        QuestCycle.DAILY -> daily
        QuestCycle.MONTHLY -> monthly
        QuestCycle.ANNUAL -> annual
    }

    /** Index of quests watching a stat, for cheap re-evaluation. */
    fun byStat(): Map<String, List<QuestDef>> =
        (daily + monthly + annual).groupBy { it.statKey }

    fun countDaily(): Int = daily.size
    fun countMonthly(): Int = monthly.size
    fun countAnnual(): Int = annual.size
    fun countAll(): Int = daily.size + monthly.size + annual.size
}
