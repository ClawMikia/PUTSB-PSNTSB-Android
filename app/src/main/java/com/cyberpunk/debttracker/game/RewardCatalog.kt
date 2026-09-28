package com.cyberpunk.debttracker.game

// ═══════════════════════════════════════════════════════════════════════════
//  REWARDS  —  every one gets its own unique cute teddy-bear head, keyed by
//  the reward code. Icons are produced by IconForge.bear(code).
// ═══════════════════════════════════════════════════════════════════════════

enum class RewardKind { SETTLE, PAYMENT, COLLECT, STREAK, NPC, QUEST, ACHIEVEMENT, LEVEL, MISC }

data class RewardDef(
    val code: String,
    val title: String,
    val blurb: String,
    val xp: Int,
    val coins: Int,
    val nerve: Int = 0,
    val bolts: Int = 0,
    val kind: RewardKind = RewardKind.MISC,
)

object RewardCatalog {

    const val FIRST_SETTLE = "RW_FIRST_SETTLE"
    const val SETTLE_SMALL = "RW_SETTLE_SMALL"
    const val SETTLE_MID = "RW_SETTLE_MID"
    const val SETTLE_LARGE = "RW_SETTLE_LARGE"
    const val OVERDUE_SLAYER = "RW_OVERDUE_SLAYER"
    const val OVERDUE_SWEEP = "RW_OVERDUE_SWEEP"
    const val ONSITE_PAYMENT = "RW_ONSITE_PAYMENT"
    const val PERFECT_NODE = "RW_PERFECT_NODE"
    const val PARTIAL_STEP = "RW_PARTIAL_STEP"
    const val PARTIAL_TO_FULL = "RW_PARTIAL_TO_FULL"
    const val COLLECT_LENT = "RW_COLLECT_LENT"
    const val COLLECT_BIG = "RW_COLLECT_BIG"
    const val BIG_DAY = "RW_BIG_DAY"
    const val TRIPLE_DAY = "RW_TRIPLE_DAY"
    const val CLEAN_DAY = "RW_CLEAN_DAY"
    const val STREAK_DAY = "RW_STREAK_DAY"
    const val STREAK_7 = "RW_STREAK_7"
    const val STREAK_30 = "RW_STREAK_30"
    const val STREAK_100 = "RW_STREAK_100"
    const val STREAK_365 = "RW_STREAK_365"
    const val NPC_RECRUIT = "RW_NPC_RECRUIT"
    const val NPC_PACIFIED = "RW_NPC_PACIFIED"
    const val NPC_PURGED = "RW_NPC_PURGED"
    const val NPC_LEVEL_UP = "RW_NPC_LEVEL_UP"
    const val NPC_MAX_LEVEL = "RW_NPC_MAX_LEVEL"
    const val NPC_RAGE_CLEARED = "RW_NPC_RAGE_CLEARED"
    const val NPC_BONDED = "RW_NPC_BONDED"
    const val NPC_NUDGED = "RW_NPC_NUDGED"
    const val NPC_PACIFIED_ACTION = "RW_NPC_PACIFIED_ACTION"
    const val QUEST_DAILY = "RW_QUEST_DAILY"
    const val QUEST_MONTHLY = "RW_QUEST_MONTHLY"
    const val QUEST_ANNUAL = "RW_QUEST_ANNUAL"
    const val QUEST_ALL_DAILY = "RW_QUEST_ALL_DAILY"
    const val MONTH_CLEARED = "RW_MONTH_CLEARED"
    const val YEAR_CLEARED = "RW_YEAR_CLEARED"
    const val ACH_UNLOCKED = "RW_ACH_UNLOCKED"
    const val ACH_TIER = "RW_ACH_TIER"
    const val LEVEL_UP = "RW_LEVEL_UP"
    const val LEVEL_10 = "RW_LEVEL_10"
    const val LEVEL_25 = "RW_LEVEL_25"
    const val LEVEL_50 = "RW_LEVEL_50"
    const val LEVEL_100 = "RW_LEVEL_100"
    const val LEVEL_250 = "RW_LEVEL_250"
    const val COIN_STASH = "RW_COIN_STASH"
    const val BOLT_CACHE = "RW_BOLT_CACHE"
    const val NERVE_RESTORED = "RW_NERVE_RESTORED"
    const val NERVE_FULL = "RW_NERVE_FULL"
    const val NEW_RECORD = "RW_NEW_RECORD"
    const val NET_POSITIVE = "RW_NET_POSITIVE"
    const val SWEEP_ALL = "RW_SWEEP_ALL"
    const val LEDGER_CLEAN = "RW_LEDGER_CLEAN"
    const val FIRST_LAUNCH = "RW_FIRST_LAUNCH"
    const val REINCARNATE = "RW_REINCARNATE"
    const val NPC_SWARM = "RW_NPC_SWARM"
    const val DAILY_SWEEP = "RW_DAILY_SWEEP"
    const val YEAR_OF_DEBTS = "RW_YEAR_OF_DEBTS"
    const val MILLION_CLEAR = "RW_MILLION_CLEAR"
    const val NET_ZERO = "RW_NET_ZERO"

    val all: List<RewardDef> = listOf(
        RewardDef(FIRST_SETTLE, "FIRST BLOOD SETTLED", "A node went dark. The grid noticed.", 120, 30, nerve = 12, kind = RewardKind.SETTLE),
        RewardDef(SETTLE_SMALL, "POCKET CHANGE", "Small weight lifted. Neurons rewarded.", 45, 10, nerve = 4, kind = RewardKind.SETTLE),
        RewardDef(SETTLE_MID, "CLEAN SWEEP", "A respectable chunk of the matrix cleared.", 90, 20, nerve = 6, kind = RewardKind.SETTLE),
        RewardDef(SETTLE_LARGE, "HEAVY LIFT", "That one had weight. It is gone now.", 200, 45, nerve = 10, kind = RewardKind.SETTLE),
        RewardDef(OVERDUE_SLAYER, "OVERDUE SLAYER", "You killed a past-due threat before it grew teeth.", 160, 35, nerve = 14, kind = RewardKind.SETTLE),
        RewardDef(OVERDUE_SWEEP, "REDLINE PURGE", "Overdue queue flushed. Nothing late survives.", 320, 80, nerve = 20, kind = RewardKind.SETTLE),
        RewardDef(ONSITE_PAYMENT, "RIGHT ON TIME", "Paid before the clock even blinked.", 70, 15, nerve = 6, kind = RewardKind.PAYMENT),
        RewardDef(PERFECT_NODE, "SPOTLESS RECORD", "Settled to the peso, ahead of schedule.", 130, 30, nerve = 10, kind = RewardKind.PAYMENT),
        RewardDef(PARTIAL_STEP, "SMALL VICTORY", "Every installment is still a win.", 30, 8, nerve = 3, kind = RewardKind.PAYMENT),
        RewardDef(PARTIAL_TO_FULL, "FOLLOWED THROUGH", "You promised installments. You delivered death.", 110, 28, nerve = 8, kind = RewardKind.PAYMENT),
        RewardDef(COLLECT_LENT, "MONEY IN", "Someone finally paid up. Cash in hand.", 80, 22, nerve = 6, kind = RewardKind.COLLECT),
        RewardDef(COLLECT_BIG, "JACKPOT", "A receivable cleared in one shot.", 210, 60, nerve = 12, kind = RewardKind.COLLECT),
        RewardDef(BIG_DAY, "DOUBLE KILL", "Two nodes closed in a single day.", 150, 40, nerve = 8, kind = RewardKind.SETTLE),
        RewardDef(TRIPLE_DAY, "TRIPLE THREAT NEUTRALISED", "Three down before sunset.", 260, 70, nerve = 12, kind = RewardKind.SETTLE),
        RewardDef(CLEAN_DAY, "ZERO OVERDUE", "Not a single node past due. Beautiful.", 90, 20, nerve = 10, kind = RewardKind.STREAK),
        RewardDef(STREAK_DAY, "STREAK ALIVE", "Another day without slipping.", 35, 10, nerve = 5, kind = RewardKind.STREAK),
        RewardDef(STREAK_7, "WEEK OF GRIT", "Seven consecutive days of showing up.", 220, 60, nerve = 18, kind = RewardKind.STREAK),
        RewardDef(STREAK_30, "MONTH OF IRON", "Thirty days. Unbroken. Legendary.", 900, 260, nerve = 40, kind = RewardKind.STREAK),
        RewardDef(STREAK_100, "CENTURY OF SILENCE", "One hundred days of pure, unbroken discipline.", 3200, 900, nerve = 80, bolts = 3, kind = RewardKind.STREAK),
        RewardDef(STREAK_365, "FULL ORBIT", "A whole year. The matrix salutes you.", 12000, 3600, nerve = 100, bolts = 12, kind = RewardKind.STREAK),
        RewardDef(NPC_RECRUIT, "NEW CONTACT", "Someone entered your circle of debt.", 25, 8, kind = RewardKind.NPC),
        RewardDef(NPC_PACIFIED, "PACIFIED", "They stand down. The pressure is off.", 140, 35, nerve = 10, kind = RewardKind.NPC),
        RewardDef(NPC_PURGED, "NODE CLEARED", "One name struck from your whole ledger.", 180, 45, nerve = 14, kind = RewardKind.NPC),
        RewardDef(NPC_LEVEL_UP, "BOND DEEPENED", "An NPC grew closer to you.", 60, 15, nerve = 4, kind = RewardKind.NPC),
        RewardDef(NPC_MAX_LEVEL, "SWORN ALLY", "They would take a bullet for you now.", 700, 200, nerve = 30, bolts = 2, kind = RewardKind.NPC),
        RewardDef(NPC_RAGE_CLEARED, "CALMED DOWN", "An enrage was talked out of existence.", 120, 30, nerve = 12, kind = RewardKind.NPC),
        RewardDef(NPC_BONDED, "BOND FORGED", "Ten settlements with the same person.", 260, 70, nerve = 16, kind = RewardKind.NPC),
        RewardDef(NPC_NUDGED, "FRIENDLY REMINDER", "A gentle nudge was received.", 15, 5, kind = RewardKind.NPC),
        RewardDef(NPC_PACIFIED_ACTION, "BOUGHT GOODWILL", "Coins spent, tension released.", 25, 0, nerve = 18, kind = RewardKind.NPC),
        RewardDef(QUEST_DAILY, "CONTRACT COMPLETE", "A daily contract is signed off.", 100, 25, nerve = 6, kind = RewardKind.QUEST),
        RewardDef(QUEST_MONTHLY, "OPERATION COMPLETE", "A monthly operation is signed off.", 420, 110, nerve = 18, kind = RewardKind.QUEST),
        RewardDef(QUEST_ANNUAL, "CAMPAIGN WON", "An annual campaign is won.", 1500, 420, nerve = 40, bolts = 2, kind = RewardKind.QUEST),
        RewardDef(QUEST_ALL_DAILY, "FULL DAILY SWEEP", "All seven contracts closed. Flawless.", 340, 90, nerve = 16, kind = RewardKind.QUEST),
        RewardDef(MONTH_CLEARED, "MONTH CLEARED", "Every monthly operation complete.", 800, 220, nerve = 24, kind = RewardKind.QUEST),
        RewardDef(YEAR_CLEARED, "YEAR CLEARED", "An entire annual campaign, done.", 3000, 900, nerve = 60, bolts = 5, kind = RewardKind.QUEST),
        RewardDef(ACH_UNLOCKED, "ACHIEVEMENT UNLOCKED", "A teddy bear joins your wall.", 70, 18, kind = RewardKind.ACHIEVEMENT),
        RewardDef(ACH_TIER, "TIER COMPLETE", "An entire achievement tier is yours.", 300, 80, nerve = 12, kind = RewardKind.ACHIEVEMENT),
        RewardDef(LEVEL_UP, "LEVEL UP", "Your rank increased.", 50, 0, kind = RewardKind.LEVEL),
        RewardDef(LEVEL_10, "COLLECTION ADEPT", "Rank 10 reached.", 250, 70, kind = RewardKind.LEVEL),
        RewardDef(LEVEL_25, "SETTLEMENT KNIGHT", "Rank 25 reached.", 600, 170, kind = RewardKind.LEVEL),
        RewardDef(LEVEL_50, "LOAN SHARK SUPREME", "Rank 50 reached.", 1400, 400, bolts = 2, kind = RewardKind.LEVEL),
        RewardDef(LEVEL_100, "SYNTHETIC MONEYBAG", "Rank 100 reached.", 5000, 1400, bolts = 5, kind = RewardKind.LEVEL),
        RewardDef(LEVEL_250, "DEBT EMPEROR", "Rank 250 reached.", 15000, 4200, bolts = 10, kind = RewardKind.LEVEL),
        RewardDef(COIN_STASH, "STASH TOPPED UP", "Coins flowing in.", 40, 100, kind = RewardKind.MISC),
        RewardDef(BOLT_CACHE, "BOLT CACHE FOUND", "Rare currency recovered.", 200, 0, bolts = 1, kind = RewardKind.MISC),
        RewardDef(NERVE_RESTORED, "NERVE RESTORED", "Steel in your spine. Points back.", 35, 0, nerve = 20, kind = RewardKind.MISC),
        RewardDef(NERVE_FULL, "FLAWLESS NERVE", "Nerve at maximum. Unshakeable.", 220, 60, kind = RewardKind.MISC),
        RewardDef(NEW_RECORD, "PERSONAL BEST", "You beat your own high score.", 110, 30, kind = RewardKind.MISC),
        RewardDef(NET_POSITIVE, "IN THE BLACK", "You are owed more than you owe.", 260, 70, nerve = 12, kind = RewardKind.MISC),
        RewardDef(SWEEP_ALL, "TOTAL SWEEP", "An entire side of the ledger wiped clean.", 520, 150, nerve = 22, kind = RewardKind.MISC),
        RewardDef(LEDGER_CLEAN, "PERFECT LEDGER", "Zero active nodes remain.", 900, 260, nerve = 30, bolts = 2, kind = RewardKind.MISC),
        RewardDef(FIRST_LAUNCH, "WELCOME, OPERATIVE", "The campaign begins. Everything starts at zero.", 30, 15, kind = RewardKind.MISC),
        RewardDef(REINCARNATE, "SECOND LIFE", "You reset the campaign and came back stronger.", 400, 120, nerve = 20, kind = RewardKind.MISC),
        RewardDef(NPC_SWARM, "SWARM ROUTE", "Twenty-five contacts in the matrix at once.", 380, 100, nerve = 16, kind = RewardKind.NPC),
        RewardDef(DAILY_SWEEP, "HOUR-12 SWEEP", "You cleaned house and checked the clock.", 160, 40, nerve = 10, kind = RewardKind.SETTLE),
        RewardDef(YEAR_OF_DEBTS, "ANNIVERSARY", "A full year of campaign data recorded.", 1100, 320, nerve = 30, bolts = 2, kind = RewardKind.MISC),
        RewardDef(MILLION_CLEAR, "MILLION CLEARED", "One peso-million wiped from existence.", 2200, 640, nerve = 40, bolts = 3, kind = RewardKind.SETTLE),
        RewardDef(NET_ZERO, "SQUARE TO ZERO", "Net balance landed exactly on zero.", 640, 180, nerve = 24, kind = RewardKind.MISC),
    )

    private val byCode = all.associateBy { it.code }

    operator fun get(code: String): RewardDef = byCode[code] ?: byCode.getValue(FIRST_LAUNCH)

    fun rewardForSettledValue(value: Long): String = when {
        value >= 1_000_000L -> MILLION_CLEAR
        value >= 100_000L -> SETTLE_LARGE
        value >= 10_000L -> SETTLE_MID
        else -> SETTLE_SMALL
    }

    /** How close an amount is to a bigger bracket, for progress-style rewards. */
    fun nextBracket(value: Long): Long? = when {
        value >= 1_000_000L -> null
        value < 10_000L -> 10_000L
        value < 100_000L -> 100_000L
        value < 1_000_000L -> 1_000_000L
        else -> null
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  PENALTIES  —  every one gets its own unique white skull, keyed by code.
// ═══════════════════════════════════════════════════════════════════════════

enum class PenaltyKind { NEW_DEBT, OVERDUE, STREAK, NPC, NEGATIVE, ABANDON, EDIT }

data class PenaltyDef(
    val code: String,
    val title: String,
    val blurb: String,
    val coins: Int,
    val nerve: Int = 0,
    val kind: PenaltyKind = PenaltyKind.EDIT,
)

object PenaltyCatalog {

    const val NEW_DEBT = "PN_NEW_DEBT"
    const val NEW_DEBT_BIG = "PN_NEW_DEBT_BIG"
    const val NEW_DEBT_OVERDUE = "PN_NEW_DEBT_OVERDUE"
    const val BORROW_BROKE = "PN_BORROW_BROKE"
    const val OVERDUE_DAY = "PN_OVERDUE_DAY"
    const val OVERDUE_WEEK = "PN_OVERDUE_WEEK"
    const val OVERDUE_MONTH = "PN_OVERDUE_MONTH"
    const val OVERDUE_OWE = "PN_OVERDUE_OWE"
    const val OVERDUE_LENT = "PN_OVERDUE_LENT"
    const val STREAK_LOST = "PN_STREAK_LOST"
    const val STREAK_SHORT = "PN_STREAK_SHORT"
    const val NPC_RAGE = "PN_NPC_RAGE"
    const val NPC_ENRAGE = "PN_NPC_ENRAGE"
    const val NPC_ABANDONED = "PN_NPC_ABANDONED"
    const val NPC_CONFRONT = "PN_NPC_CONFRONT"
    const val NPC_BROKE = "PN_NPC_BROKE"
    const val NEGATIVE_NET = "PN_NEGATIVE_NET"
    const val DEBT_MOUNTAIN = "PN_DEBT_MOUNTAIN"
    const val DEBT_AVALANCHE = "PN_DEBT_AVALANCHE"
    const val UNPAID_COLLECTOR = "PN_UNPAID_COLLECTOR"
    const val IGNORED_QUEST = "PN_IGNORED_QUEST"
    const val ABANDONED_3D = "PN_ABANDONED_3D"
    const val ABANDONED_7D = "PN_ABANDONED_7D"
    const val NERVE_DRAIN = "PN_NERVE_DRAIN"
    const val NERVE_EMPTY = "PN_NERVE_EMPTY"
    const val EDIT_DELETE = "PN_EDIT_DELETE"
    const val COIN_TAX = "PN_COIN_TAX"
    const val NPC_MULTIPLIER = "PN_NPC_MULTIPLIER"
    const val INTEREST_COMPOUND = "PN_INTEREST_COMPOUND"
    const val PATIENCE_GONE = "PN_PATIENCE_GONE"
    const val SPENDING_SPREE = "PN_SPENDING_SPREE"
    const val BAD_DEBT_CLUSTER = "PN_BAD_DEBT_CLUSTER"
    const val SILENT_CREDITOR = "PN_SILENT_CREDITOR"

    val all: List<PenaltyDef> = listOf(
        PenaltyDef(NEW_DEBT, "NEW LIABILITY", "You signed another debt into existence.", coins = -5, nerve = -4, kind = PenaltyKind.NEW_DEBT),
        PenaltyDef(NEW_DEBT_BIG, "HEAVY BORROWING", "That one is a chunk of your future income.", coins = -18, nerve = -10, kind = PenaltyKind.NEW_DEBT),
        PenaltyDef(NEW_DEBT_OVERDUE, "BORN OVERDUE", "You registered a node already in the red.", coins = -12, nerve = -8, kind = PenaltyKind.NEW_DEBT),
        PenaltyDef(BORROW_BROKE, "BORROWED TO ZERO", "You are borrowing against nothing. Bold.", coins = -30, nerve = -14, kind = PenaltyKind.NEGATIVE),
        PenaltyDef(OVERDUE_DAY, "PAST DUE", "A node crossed its date. Interest starts now.", coins = -6, nerve = -3, kind = PenaltyKind.OVERDUE),
        PenaltyDef(OVERDUE_WEEK, "SEVEN DAYS LATE", "A week past due. The calls begin.", coins = -28, nerve = -10, kind = PenaltyKind.OVERDUE),
        PenaltyDef(OVERDUE_MONTH, "A MONTH GONE", "Thirty days late. Serious money talking.", coins = -110, nerve = -26, kind = PenaltyKind.OVERDUE),
        PenaltyDef(OVERDUE_OWE, "YOU ARE THE DELINQUENT", "You are the one being chased. Ouch.", coins = -8, nerve = -5, kind = PenaltyKind.OVERDUE),
        PenaltyDef(OVERDUE_LENT, "THEY STALLED", "Someone you lent to blew your date.", coins = -5, nerve = -3, kind = PenaltyKind.OVERDUE),
        PenaltyDef(STREAK_LOST, "STREAK EXTINGUISHED", "You vanished. The streak burned to nothing.", coins = -25, nerve = -12, kind = PenaltyKind.STREAK),
        PenaltyDef(STREAK_SHORT, "STREAK AT RISK", "You are one day from losing the chain.", coins = -8, nerve = -4, kind = PenaltyKind.STREAK),
        PenaltyDef(NPC_RAGE, "PATIENCE LOW", "An NPC is close to snapping at you.", coins = -10, nerve = -5, kind = PenaltyKind.NPC),
        PenaltyDef(NPC_ENRAGE, "FULL ENRAGE", "An NPC has lost all patience with you.", coins = -35, nerve = -14, kind = PenaltyKind.NPC),
        PenaltyDef(NPC_ABANDONED, "GHOSTED", "You left a debt node behind. Careless.", coins = -8, nerve = -4, kind = PenaltyKind.NPC),
        PenaltyDef(NPC_CONFRONT, "YOU PUSHED", "You escalated. The relationship bled.", coins = -6, nerve = -6, kind = PenaltyKind.NPC),
        PenaltyDef(NPC_BROKE, "COUNTERPART IS BROKE", "They cannot pay. The node is stuck.", coins = 0, nerve = -2, kind = PenaltyKind.NPC),
        PenaltyDef(NEGATIVE_NET, "DEEP IN THE RED", "You owe more than you are owed.", coins = -15, nerve = -8, kind = PenaltyKind.NEGATIVE),
        PenaltyDef(DEBT_MOUNTAIN, "DEBT MOUNTAIN", "Too much active balance sitting on you.", coins = -14, nerve = -7, kind = PenaltyKind.NEGATIVE),
        PenaltyDef(DEBT_AVALANCHE, "DEBT AVALANCHE", "The stack is collapsing on your head.", coins = -45, nerve = -16, kind = PenaltyKind.NEGATIVE),
        PenaltyDef(UNPAID_COLLECTOR, "COLLECTOR CALLED", "You are owed a lot and collected none of it.", coins = 0, nerve = -5, kind = PenaltyKind.NEGATIVE),
        PenaltyDef(IGNORED_QUEST, "CONTRACT FAILED", "A quest window closed without completion.", coins = -10, nerve = -4, kind = PenaltyKind.ABANDON),
        PenaltyDef(ABANDONED_3D, "THREE DAYS GONE", "No activity for three days.", coins = -8, nerve = -5, kind = PenaltyKind.ABANDON),
        PenaltyDef(ABANDONED_7D, "WEEK GONE", "A full week of silence. The matrix forgot you.", coins = -40, nerve = -14, kind = PenaltyKind.ABANDON),
        PenaltyDef(NERVE_DRAIN, "NERVE FRAYED", "Pressure stacking. Confidence dropping.", coins = 0, nerve = -10, kind = PenaltyKind.NEGATIVE),
        PenaltyDef(NERVE_EMPTY, "NERVEBREAK", "Nerve hit zero. You are barely holding it together.", coins = 0, nerve = 0, kind = PenaltyKind.NEGATIVE),
        PenaltyDef(EDIT_DELETE, "NODE SCRUBBED", "You erased history. The ledger weeps.", coins = 0, nerve = -2, kind = PenaltyKind.EDIT),
        PenaltyDef(COIN_TAX, "REPO MAN VISITED", "Penalty tax skimmed off your stash.", coins = -40, kind = PenaltyKind.EDIT),
        PenaltyDef(NPC_MULTIPLIER, "EXTRA WITNESSES", "An angry NPC told the other angry NPCs.", coins = 0, nerve = -3, kind = PenaltyKind.NPC),
        PenaltyDef(INTEREST_COMPOUND, "INTEREST COMPOUNDED", "Late fees stacked on late fees.", coins = -22, nerve = -9, kind = PenaltyKind.OVERDUE),
        PenaltyDef(PATIENCE_GONE, "ZERO PATIENCE", "This relationship is functionally over.", coins = -10, nerve = -8, kind = PenaltyKind.NPC),
        PenaltyDef(SPENDING_SPREE, "SPENDING SPREE", "You opened more than you could ever close.", coins = -25, nerve = -12, kind = PenaltyKind.NEW_DEBT),
        PenaltyDef(BAD_DEBT_CLUSTER, "CLUSTER OF BAD", "Several nodes went red at the same time.", coins = -32, nerve = -13, kind = PenaltyKind.OVERDUE),
        PenaltyDef(SILENT_CREDITOR, "SILENT CREDITOR", "They went quiet. That is never good.", coins = -12, nerve = -6, kind = PenaltyKind.NPC),
    )

    private val byCode = all.associateBy { it.code }

    operator fun get(code: String): PenaltyDef = byCode[code] ?: byCode.getValue(OVERDUE_DAY)

    fun forDaysLate(days: Long): String = when {
        days >= 30L -> OVERDUE_MONTH
        days >= 7L -> OVERDUE_WEEK
        else -> OVERDUE_DAY
    }
}
