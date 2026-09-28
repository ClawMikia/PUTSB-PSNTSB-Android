package com.cyberpunk.debttracker.game

// ═══════════════════════════════════════════════════════════════════════════
//  ACHIEVEMENT CATALOG — exactly 100 entries.
//  Each one has its own unique teddy-bear head (IconForge.bear(code)).
// ═══════════════════════════════════════════════════════════════════════════

enum class AchievementTier(val label: String) {
    BRONZE("BRONZE"),
    SILVER("SILVER"),
    GOLD("GOLD"),
    PLATINUM("PLATINUM"),
    LEGENDARY("LEGENDARY");

    companion object {
        fun fromOrdinalSafe(i: Int): AchievementTier = entries.getOrElse(i) { BRONZE }
    }
}

data class AchievementDef(
    val code: String,
    val title: String,
    val description: String,
    val tier: AchievementTier,
    val statKey: String,
    val target: Int,
    val xp: Int,
    val coins: Int,
    val bolts: Int = 0,
)

object AchievementCatalog {

    val all: List<AchievementDef> = listOf(

        // ─── A. Initiation (5) ───────────────────────────────────────────────
        A("ACH_FIRST_NODE", "First Contact", "Create your very first debt node.", AchievementTier.BRONZE, Stat.DEBTS_ADDED, 1, 60, 20),
        A("ACH_FIVE_NODES", "Ledger Filled", "Register 5 debt nodes in the matrix.", AchievementTier.BRONZE, Stat.DEBTS_ADDED, 5, 110, 35),
        A("ACH_TEN_NODES", "Network Established", "Register 10 debt nodes.", AchievementTier.SILVER, Stat.DEBTS_ADDED, 10, 200, 60),
        A("ACH_FIFTY_NODES", "Sprawl Crawler", "Register 50 debt nodes across your life.", AchievementTier.GOLD, Stat.DEBTS_ADDED, 50, 600, 180),
        A("ACH_HUNDRED_NODES", "City-Wide Reach", "Register 100 debt nodes. A hundred names remember you.", AchievementTier.PLATINUM, Stat.DEBTS_ADDED, 100, 1600, 520),

        // ─── B. Settlements (6) ──────────────────────────────────────────────
        A("ACH_FIRST_SETTLE", "Tagged In", "Settle your first debt node.", AchievementTier.BRONZE, Stat.DEBTS_SETTLED, 1, 100, 30),
        A("ACH_FIVE_SETTLE", "Small Purge", "Settle 5 debt nodes.", AchievementTier.BRONZE, Stat.DEBTS_SETTLED, 5, 160, 50),
        A("ACH_TEN_SETTLE", "Cleanup Crew", "Settle 10 debt nodes.", AchievementTier.SILVER, Stat.DEBTS_SETTLED, 10, 280, 85),
        A("ACH_TWENTYFIVE_SETTLE", "Debt Slayer", "Settle 25 debt nodes.", AchievementTier.SILVER, Stat.DEBTS_SETTLED, 25, 420, 130),
        A("ACH_FIFTY_SETTLE", "Ghost Protocol", "Settle 50 debt nodes and leave no trace.", AchievementTier.GOLD, Stat.DEBTS_SETTLED, 50, 900, 280),
        A("ACH_HUNDRED_SETTLE", "Total Eradication", "Settle 100 debt nodes. Nothing survives you.", AchievementTier.PLATINUM, Stat.DEBTS_SETTLED, 100, 2200, 700, 2),

        // ─── C. Partial payments (5) ─────────────────────────────────────────
        A("ACH_FIRST_PARTIAL", "Downpayment", "Record your first partial payment.", AchievementTier.BRONZE, Stat.PAYMENTS_RECORDED, 1, 70, 20),
        A("ACH_TEN_PARTIAL", "Installment Plan", "Record 10 partial payments.", AchievementTier.BRONZE, Stat.PAYMENTS_RECORDED, 10, 150, 45),
        A("ACH_FIFTY_PARTIAL", "Debt Schedar", "Record 50 partial payments without running away.", AchievementTier.SILVER, Stat.PAYMENTS_RECORDED, 50, 400, 120),
        A("ACH_TWOHUND_PARTIAL", "Rolling Wallet", "Record 200 partial payments.", AchievementTier.GOLD, Stat.PAYMENTS_RECORDED, 200, 1100, 340),
        A("ACH_PARTIAL_TO_FULL", "Follow Through", "Finish a node you had started paying in instalments.", AchievementTier.SILVER, Stat.PARTIAL_TO_FULL, 1, 240, 70),

        // ─── D. Value (8) ────────────────────────────────────────────────────
        A("ACH_VALUE_1K", "Grand One", "Settle \u20B11,000 in total.", AchievementTier.BRONZE, Stat.VALUE_SETTLED, 1_000, 80, 25),
        A("ACH_VALUE_10K", "Ten Grand Purge", "Settle \u20B110,000 in total.", AchievementTier.SILVER, Stat.VALUE_SETTLED, 10_000, 240, 70),
        A("ACH_VALUE_50K", "Fifty K Discipline", "Settle \u20B150,000 in total.", AchievementTier.SILVER, Stat.VALUE_SETTLED, 50_000, 450, 135),
        A("ACH_VALUE_100K", "Six Figures", "Settle \u20B1100,000 in total.", AchievementTier.GOLD, Stat.VALUE_SETTLED, 100_000, 850, 260),
        A("ACH_VALUE_500K", "Half Million", "Settle \u20B1500,000 in total.", AchievementTier.GOLD, Stat.VALUE_SETTLED, 500_000, 1400, 420),
        A("ACH_VALUE_1M", "Millionaire Clean", "Settle \u20B11,000,000 in total. The books are sacred now.", AchievementTier.PLATINUM, Stat.VALUE_SETTLED, 1_000_000, 2600, 800, 3),
        A("ACH_BIG_SETTLE_50K", "Heavyweight", "Settle a single node worth \u20B150,000 or more.", AchievementTier.GOLD, Stat.LARGEST_SETTLEMENT, 50_000, 600, 170),
        A("ACH_BIG_SETTLE_500K", "Whale Killer", "Settle a single node worth \u20B1500,000 or more.", AchievementTier.LEGENDARY, Stat.LARGEST_SETTLEMENT, 500_000, 3000, 900, 3),

        // ─── E. Overdue (6) ──────────────────────────────────────────────────
        A("ACH_OVERDUE_1", "Clock Breaker", "Settle a node that was already overdue.", AchievementTier.BRONZE, Stat.OVERDUE_PAID, 1, 120, 35),
        A("ACH_OVERDUE_10", "Deadline Negotiator", "Settle 10 nodes that had gone past due.", AchievementTier.SILVER, Stat.OVERDUE_PAID, 10, 320, 100),
        A("ACH_OVERDUE_25", "Retroactive Payoff", "Settle 25 overdue nodes.", AchievementTier.SILVER, Stat.OVERDUE_PAID, 25, 520, 160),
        A("ACH_OVERDUE_100", "Time Lord", "Settle 100 overdue nodes. Time is a debt you pay.", AchievementTier.PLATINUM, Stat.OVERDUE_PAID, 100, 2400, 760, 2),
        A("ACH_CLEAN_WEEK", "Clean Week", "Complete a day with zero overdue nodes.", AchievementTier.SILVER, Stat.CLEAN_DAYS, 1, 180, 50),
        A("ACH_CLEAN_MONTHS", "Clean Cycle", "Complete 5 days with zero overdue nodes.", AchievementTier.GOLD, Stat.CLEAN_DAYS, 5, 700, 210),

        // ─── F. NPCs (10) ────────────────────────────────────────────────────
        A("ACH_NPC_FIRST", "First Contact: NPC", "Recruit your first NPC into the matrix.", AchievementTier.BRONZE, Stat.NPCS_RECRUITED, 1, 70, 20),
        A("ACH_NPC_FIVE", "Gang Forming", "Recruit 5 NPCs.", AchievementTier.BRONZE, Stat.NPCS_RECRUITED, 5, 150, 45),
        A("ACH_NPC_TEN", "Syndicate", "Recruit 10 NPCs.", AchievementTier.SILVER, Stat.NPCS_RECRUITED, 10, 300, 90),
        A("ACH_NPC_TWENTYFIVE", "Cartel Boss", "Recruit 25 NPCs.", AchievementTier.GOLD, Stat.NPCS_RECRUITED, 25, 700, 210),
        A("ACH_NPC_FIFTY", "City Ruler", "Recruit 50 NPCs. You know everybody now.", AchievementTier.PLATINUM, Stat.NPCS_RECRUITED, 50, 1800, 580, 2),
        A("ACH_PACIFIED_ONE", "Pacifier", "Fully settle the debt of one NPC.", AchievementTier.BRONZE, Stat.NPCS_PACIFIED, 1, 160, 45),
        A("ACH_PACIFIED_TEN", "Peacemaker", "Fully settle the debts of 10 NPCs.", AchievementTier.GOLD, Stat.NPCS_PACIFIED, 10, 800, 240),
        A("ACH_PACIFIED_25", "Ambassador", "Fully settle the debts of 25 NPCs.", AchievementTier.PLATINUM, Stat.NPCS_PACIFIED, 25, 1900, 620, 2),
        A("ACH_RAGE_CLEARED_5", "Cooler Head", "Talk 5 enraged NPCs back down.", AchievementTier.SILVER, Stat.NPC_RAGE_CLEARED, 5, 380, 110),
        A("ACH_NPC_TOP_LEVEL", "Legendary Bond", "Push an NPC to level 20 or higher.", AchievementTier.LEGENDARY, Stat.NPC_TOP_LEVEL, 20, 2600, 820, 3),

        // ─── G. Quests (9) ───────────────────────────────────────────────────
        A("ACH_Q_DAILY_1", "First Contract", "Complete 1 daily quest.", AchievementTier.BRONZE, Stat.QUESTS_DAILY_DONE, 1, 90, 30),
        A("ACH_Q_DAILY_10", "Contract Machine", "Complete 10 daily quests.", AchievementTier.SILVER, Stat.QUESTS_DAILY_DONE, 10, 280, 85),
        A("ACH_Q_DAILY_50", "Contract Fiend", "Complete 50 daily quests.", AchievementTier.SILVER, Stat.QUESTS_DAILY_DONE, 50, 620, 190),
        A("ACH_Q_DAILY_200", "Contract Legend", "Complete 200 daily quests.", AchievementTier.GOLD, Stat.QUESTS_DAILY_DONE, 200, 1500, 480),
        A("ACH_Q_MONTHLY_1", "First Operation", "Complete 1 monthly quest.", AchievementTier.SILVER, Stat.QUESTS_MONTHLY_DONE, 1, 300, 90),
        A("ACH_Q_MONTHLY_10", "Operations Lead", "Complete 10 monthly quests.", AchievementTier.GOLD, Stat.QUESTS_MONTHLY_DONE, 10, 900, 280),
        A("ACH_Q_MONTHLY_30", "Operation Master", "Complete all 30 monthly quests.", AchievementTier.PLATINUM, Stat.QUESTS_MONTHLY_DONE, 30, 2400, 780, 3),
        A("ACH_Q_ANNUAL_1", "First Campaign", "Complete 1 annual quest.", AchievementTier.GOLD, Stat.QUESTS_ANNUAL_DONE, 1, 800, 240),
        A("ACH_Q_ANNUAL_25", "Campaign Victor", "Complete 25 annual quests.", AchievementTier.LEGENDARY, Stat.QUESTS_ANNUAL_DONE, 25, 3000, 950, 3),

        // ─── H. Streaks (7) ──────────────────────────────────────────────────
        A("ACH_STREAK_3", "Three Alive", "Hold a 3-day streak.", AchievementTier.BRONZE, Stat.STREAK_BEST, 3, 90, 30),
        A("ACH_STREAK_7", "Week of Grit", "Hold a 7-day streak.", AchievementTier.BRONZE, Stat.STREAK_BEST, 7, 180, 55),
        A("ACH_STREAK_14", "Fortnight", "Hold a 14-day streak.", AchievementTier.SILVER, Stat.STREAK_BEST, 14, 340, 100),
        A("ACH_STREAK_30", "Month of Iron", "Hold a 30-day streak.", AchievementTier.SILVER, Stat.STREAK_BEST, 30, 700, 210),
        A("ACH_STREAK_60", "Bimonthly Discipline", "Hold a 60-day streak.", AchievementTier.GOLD, Stat.STREAK_BEST, 60, 1200, 380),
        A("ACH_STREAK_100", "Century", "Hold a 100-day streak.", AchievementTier.GOLD, Stat.STREAK_BEST, 100, 2000, 640),
        A("ACH_STREAK_365", "Full Orbit", "Hold a 365-day streak. A full year unbroken.", AchievementTier.LEGENDARY, Stat.STREAK_BEST, 365, 8000, 2600, 6),

        // ─── I. Economy (6) ──────────────────────────────────────────────────
        A("ACH_COIN_100", "Pocket Lining", "Earn 100 coins in total.", AchievementTier.BRONZE, Stat.COINS_EARNED, 100, 80, 20),
        A("ACH_COIN_1K", "Stashed", "Earn 1,000 coins in total.", AchievementTier.BRONZE, Stat.COINS_EARNED, 1_000, 160, 45),
        A("ACH_COIN_10K", "Loaded", "Earn 10,000 coins in total.", AchievementTier.SILVER, Stat.COINS_EARNED, 10_000, 420, 130),
        A("ACH_COIN_100K", "Fat Stash", "Earn 100,000 coins in total.", AchievementTier.GOLD, Stat.COINS_EARNED, 100_000, 1000, 320),
        A("ACH_BOLT_1", "Rare Drop", "Collect your first bolt.", AchievementTier.SILVER, Stat.BOLTS_EARNED, 1, 300, 80, 0),
        A("ACH_BOLT_10", "Bolt Baron", "Collect 10 bolts.", AchievementTier.PLATINUM, Stat.BOLTS_EARNED, 10, 2200, 700, 0),

        // ─── J. Levels (6) ───────────────────────────────────────────────────
        A("ACH_LEVEL_5", "Ledger Grunt", "Reach level 5.", AchievementTier.BRONZE, Stat.TOP_LEVEL, 5, 80, 25),
        A("ACH_LEVEL_10", "Collection Adept", "Reach level 10.", AchievementTier.BRONZE, Stat.TOP_LEVEL, 10, 180, 55),
        A("ACH_LEVEL_25", "Settlement Knight", "Reach level 25.", AchievementTier.SILVER, Stat.TOP_LEVEL, 25, 520, 160),
        A("ACH_LEVEL_50", "Loan Shark Supreme", "Reach level 50.", AchievementTier.GOLD, Stat.TOP_LEVEL, 50, 1200, 400),
        A("ACH_LEVEL_100", "Synthetic Moneybag", "Reach level 100.", AchievementTier.PLATINUM, Stat.TOP_LEVEL, 100, 2600, 850, 3),
        A("ACH_LEVEL_250", "Debt Emperor", "Reach level 250.", AchievementTier.LEGENDARY, Stat.TOP_LEVEL, 250, 9000, 3000, 8),

        // ─── K. Rewards & penalties (6) ──────────────────────────────────────
        A("ACH_REWARD_10", "First Sweets", "Collect 10 teddy-bear rewards.", AchievementTier.BRONZE, Stat.REWARDS_RECEIVED, 10, 100, 30),
        A("ACH_REWARD_100", "Teddy Wall", "Collect 100 teddy-bear rewards.", AchievementTier.SILVER, Stat.REWARDS_RECEIVED, 100, 380, 120),
        A("ACH_REWARD_500", "Plush Empire", "Collect 500 teddy-bear rewards.", AchievementTier.GOLD, Stat.REWARDS_RECEIVED, 500, 1300, 420),
        A("ACH_PENALTY_1", "First Skull", "Take your first penalty.", AchievementTier.BRONZE, Stat.PENALTIES_RECEIVED, 1, 60, 15),
        A("ACH_PENALTY_50", "Skull Collector", "Take 50 penalties.", AchievementTier.SILVER, Stat.PENALTIES_RECEIVED, 50, 400, 120),
        A("ACH_PENALTY_150", "Ossuary", "Take 150 penalties and still standing.", AchievementTier.GOLD, Stat.PENALTIES_RECEIVED, 150, 1400, 460),

        // ─── L. Achievement meta (4) ─────────────────────────────────────────
        A("ACH_META_10", "Decade of Teddy", "Unlock 10 achievements.", AchievementTier.BRONZE, Stat.ACHIEVEMENTS_UNLOCKED, 10, 120, 35),
        A("ACH_META_25", "Quarter Century", "Unlock 25 achievements.", AchievementTier.SILVER, Stat.ACHIEVEMENTS_UNLOCKED, 25, 360, 110),
        A("ACH_META_50", "Half Century", "Unlock 50 achievements.", AchievementTier.GOLD, Stat.ACHIEVEMENTS_UNLOCKED, 50, 900, 290),
        A("ACH_META_100", "Full Collection", "Unlock all 100 achievements.", AchievementTier.PLATINUM, Stat.ACHIEVEMENTS_UNLOCKED, 100, 5000, 1600, 6),

        // ─── M. Tier sweeps (4) ──────────────────────────────────────────────
        A("ACH_TIER_1", "Full Class", "Complete every achievement in one tier.", AchievementTier.BRONZE, Stat.ACH_SAME_TIER_10, 1, 200, 60),
        A("ACH_TIER_2", "Double Class", "Complete two entire tiers.", AchievementTier.SILVER, Stat.ACH_SAME_TIER_10, 2, 460, 140),
        A("ACH_TIER_3", "Triple Class", "Complete three entire tiers.", AchievementTier.SILVER, Stat.ACH_SAME_TIER_10, 3, 1000, 320),
        A("ACH_TIER_4", "Quadruple Class", "Complete four entire tiers.", AchievementTier.GOLD, Stat.ACH_SAME_TIER_10, 4, 2200, 720, 2),

        // ─── N. Ledger craft (8) ─────────────────────────────────────────────
        A("ACH_BIG_DAY", "Double Kill", "Settle 2 debts in one day.", AchievementTier.BRONZE, Stat.BIG_DAY, 1, 160, 45),
        A("ACH_TRIPLE_DAY", "Triple Threat", "Settle 3 debts in one day.", AchievementTier.SILVER, Stat.TRIPLE_DAY, 1, 320, 95),
        A("ACH_ALL_SEVEN", "Full Daily Sweep", "Complete all 7 daily quests in a single day.", AchievementTier.GOLD, Stat.ALL_SEVEN_DONE, 1, 700, 200),
        A("ACH_ALL_SEVEN_10", "Ten Perfect Days", "Do 10 full daily sweeps.", AchievementTier.PLATINUM, Stat.ALL_SEVEN_DONE, 10, 2200, 700, 2),
        A("ACH_PERFECT_NODE", "Spotless Record", "Settle a node early and to the peso.", AchievementTier.BRONZE, Stat.PERFECT_NODE, 1, 140, 40),
        A("ACH_PERFECT_NODE_25", "Flawless 25", "Record 25 spotless settlements.", AchievementTier.GOLD, Stat.PERFECT_NODE, 25, 1100, 340),
        A("ACH_SWEEP_OWE", "You-Owe Wiped", "Run your I-Owe side down to zero three times over.", AchievementTier.GOLD, Stat.OWE_SIDE_SETTLED, 3, 800, 230),
        A("ACH_SWEEP_LENT", "Owed-Me Wiped", "Run your Owes-Me side down to zero three times over.", AchievementTier.GOLD, Stat.LENT_SIDE_SETTLED, 3, 800, 230),

        // ─── O. Engagement (6) ───────────────────────────────────────────────
        A("ACH_DAYS_10", "Regular", "Be active on 10 separate days.", AchievementTier.BRONZE, Stat.DAYS_PLAYED, 10, 110, 30),
        A("ACH_DAYS_50", "Dedicated", "Be active on 50 separate days.", AchievementTier.SILVER, Stat.DAYS_PLAYED, 50, 420, 130),
        A("ACH_DAYS_200", "Relentless", "Be active on 200 separate days.", AchievementTier.GOLD, Stat.DAYS_PLAYED, 200, 1500, 480),
        A("ACH_IMPORT", "Ledger Ingested", "Import a JSON backup at least once.", AchievementTier.BRONZE, Stat.IMPORTS, 1, 70, 20),
        A("ACH_EXPORT", "Data Extracted", "Export your ledger at least once.", AchievementTier.BRONZE, Stat.EXPORTS, 1, 70, 20),
        A("ACH_ARCHIVE", "Ghost Ledger", "Archive a settled node into the vault.", AchievementTier.BRONZE, Stat.ARCHIVES, 1, 90, 25),

        // ─── P. Finale (4) ───────────────────────────────────────────────────
        A("ACH_NERVE_FULL", "Unshakeable", "Restore your nerve to maximum at least once.", AchievementTier.SILVER, Stat.NERVE_FULL, 1, 300, 80),
        A("ACH_NERVE_EMPTY", "Rock Bottom", "Get your nerve down to zero and keep going.", AchievementTier.SILVER, Stat.NERVE_EMPTY, 1, 320, 90),
        A("ACH_MONTHS_CLEARED_3", "Three Clean Months", "Complete every monthly quest in 3 separate months.", AchievementTier.GOLD, Stat.MONTHS_CLEARED, 3, 1800, 560, 2),
        A("ACH_YEARS_CLEARED_1", "Annual Champion", "Complete every annual quest in a single year.", AchievementTier.LEGENDARY, Stat.YEARS_CLEARED, 1, 6000, 2000, 5),
    )

    private val byCode = all.associateBy { it.code }

    operator fun get(code: String): AchievementDef? = byCode[code]

    fun count(): Int = all.size

    fun totalsByTier(): Map<AchievementTier, Int> =
        all.groupingBy { it.tier }.eachCount()

    /** Index of achievements that watch a given stat, for cheap re-evaluation. */
    fun byStat(): Map<String, List<AchievementDef>> = all.groupBy { it.statKey }

    private fun A(
        code: String,
        title: String,
        description: String,
        tier: AchievementTier,
        statKey: String,
        target: Int,
        xp: Int,
        coins: Int,
        bolts: Int = 0,
    ) = AchievementDef(code, title, description, tier, statKey, target, xp, coins, bolts)
}
