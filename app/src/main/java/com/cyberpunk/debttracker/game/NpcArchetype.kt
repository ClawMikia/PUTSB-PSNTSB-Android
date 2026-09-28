package com.cyberpunk.debttracker.game

/**
 * NPC personality templates. Every person you have a debt node with gets
 * assigned one of these deterministically from their name, so the same
 * person always behaves the same way inside the campaign.
 */
data class NpcArchetype(
    val key: String,
    val name: String,
    val role: String,
    val trait1: String,
    val trait2: String,
    val trait3: String,
    val basePatience: Int,
    val xpPerSettle: Int,
    val relationPerSettle: Int,
    val accent: Int,
    val idle: String,
    val happy: String,
    val angry: String,
    val broken: String,
    val retaliation: Int = 1,
) {
    val traits: List<String> = listOf(trait1, trait2, trait3)

    companion object {
        private val ALL = listOf(
            NpcArchetype(
                key = "COLLECTOR", name = "The Collector", role = "CREDIT SHD",
                trait1 = "Never forgets a peso", trait2 = "Counts receipts twice", trait3 = "Smiles while tightening the knot",
                basePatience = 70, xpPerSettle = 12, relationPerSettle = 4, accent = 0xFFFF1744.toInt(),
                idle = "Ticking a pen against the counter, waiting.",
                happy = "Folds the stub in half. \"Tangent na, tanga mo.\"",
                angry = "Slams a second ledger on the table. \"Bayaryin mo ba, boss?\"",
                broken = "\"Wala na akong laban. Bayad na ko.\"",
            ),
            NpcArchetype(
                key = "BOSS", name = "The Boss", role = "TRIBAL HEAD",
                trait1 = "Loyal to a fault", trait2 = "Loud in every room", trait3 = "Pays, but only on payday",
                basePatience = 90, xpPerSettle = 14, relationPerSettle = 6, accent = 0xFFFF9100.toInt(),
                idle = "Cleaning sunglasses slowly, waiting for you to speak first.",
                happy = "Slaps your shoulder hard. \"Ayos ka, pre!\"",
                angry = "Stands up fast. \"Napaka-sabi mo, bumbay ko?\"",
                broken = "Slumps. \"Kaya ko na 'yon, boss. Tigil na ako.\"",
                retaliation = 0,
            ),
            NpcArchetype(
                key = "GHOST", name = "The Ghost", role = "SILENT DEBTOR",
                trait1 = "Answers three days late", trait2 = "Switches numbers", trait3 = "Owes everywhere, nothing owed",
                basePatience = 40, xpPerSettle = 16, relationPerSettle = 2, accent = 0xFF7C4DFF.toInt(),
                idle = "Phone on silent. No response since Tuesday.",
                happy = "\"Legit. Screenshot 'to para proof ka.\"",
                angry = "Doesn't open the message. Read receipts on, though.",
                broken = "\"Wala na. Boses ako na nung mayaman, basahan na ako.\"",
            ),
            NpcArchetype(
                key = "SENTINEL", name = "The Sentinel", role = "STRICT LEDGER",
                trait1 = "Zero tolerance for lateness", trait2 = "Files every receipt", trait3 = "Zero tolerance for excuses",
                basePatience = 55, xpPerSettle = 13, relationPerSettle = 3, accent = 0xFF00E5FF.toInt(),
                idle = "Tapping the counter in exact seconds. Counting your delays.",
                happy = "Stamps the document. RECEIVED, in green.",
                angry = "\"That's day nine. I keep records.\"",
                broken = "\"Fine. Accepted. Don't do it again.\"",
            ),
            NpcArchetype(
                key = "MONEYBAG", name = "The Moneybag", role = "DEEP POCKETS",
                trait1 = "Always has cash", trait2 = "Never has time", trait3 = "Forgives in cash",
                basePatience = 95, xpPerSettle = 10, relationPerSettle = 8, accent = 0xFF00E676.toInt(),
                idle = "Scrolling through property listings, unbothered.",
                happy = "\"Keep the change. Seryoso kita, kaibigan.\"",
                angry = "\"Ang mahal ng interest mo, tsk tsk tsk.\"",
                broken = "\"Take it, take it. Sano na ako.\"",
            ),
            NpcArchetype(
                key = "NEIGHBOR", name = "The Neighbor", role = "TRUSTING FRIEND",
                trait1 = "Lent without asking", trait2 = "Too polite to chase", trait3 = "Smiles through everything",
                basePatience = 85, xpPerSettle = 15, relationPerSettle = 10, accent = 0xFFAEEA00.toInt(),
                idle = "Holding the door open for you, again.",
                happy = "\"Kaya mo 'yan, ipagpapalit kana ng pagkain.\"",
                angry = "Never angry. Just... disappointed. Somehow worse.",
                broken = "\"Hindi na kita mag-aalala, kaibigan.\"",
            ),
            NpcArchetype(
                key = "LOANSHARK", name = "The Loan Shark", role = "APOLLO",
                trait1 = "Lends at lethal rates", trait2 = "Remembers birthdays", trait3 = "Smiles while sharpening",
                basePatience = 45, xpPerSettle = 18, relationPerSettle = 1, accent = 0xFF00E5B6.toInt(),
                idle = "Whetting a blade that has never once cut fruit.",
                happy = "\"Nice. The books balance. For now.\"",
                angry = "\"Extend the term. With interest. On interest.\"",
                broken = "\"Sino ka naman, agent? Alis.\"",
                retaliation = 2,
            ),
            NpcArchetype(
                key = "PAL", name = "The Pal", role = "BUDDY SYSTEM",
                trait1 = "Says yes to everything", trait2 = "Pays in excuses", trait3 = "Hugs instead of collections",
                basePatience = 80, xpPerSettle = 14, relationPerSettle = 9, accent = 0xFFFF4081.toInt(),
                idle = "Sending a meme instead of a receipt.",
                happy = "\"Brawl! Next one's on me. ...Maybe.\"",
                angry = "\"Bro, di ko na kaya magpadala ng 'di mo na siya ma-contact'.\"",
                broken = "\"Kaya ko na 'yon, pre. Kahit hindi kita ma-contact.\"",
            ),
            NpcArchetype(
                key = "LANDLORD", name = "The Landlord", role = "UNIT 4B",
                trait1 = "Upgrades are never included", trait2 = "Keeps the deposit warm", trait3 = "Reminds you monthly",
                basePatience = 60, xpPerSettle = 12, relationPerSettle = 2, accent = 0xFF8D6E63.toInt(),
                idle = "Knocking on the door at 9:01 AM sharp.",
                happy = "\"Noted. Keep the lights off, walay ganon.\"",
                angry = "\"Second notice. This is not a drill.\"",
                broken = "\"Whatever, just... keep it quiet, please.\"",
            ),
            NpcArchetype(
                key = "REPAIRS", name = "The Mechanic", role = "AUTO REPAIR",
                trait1 = "Fixes it with balipak", trait2 = "Bill grows on its own", trait3 = "Hums while working",
                basePatience = 75, xpPerSettle = 13, relationPerSettle = 5, accent = 0xFFFFC107.toInt(),
                idle = "Radio static, then a familiar cough.",
                happy = "\"Ay, kinakawan na. Tara, ride na.\"",
                angry = "\"Wala nang parts. Or... bayad mo na ba?\"",
                broken = "\"Wala na parts, pre. Kaya mo na bang gawingTotoy.\"",
            ),
            NpcArchetype(
                key = "BINGO", name = "The Bingo", role = "ALL-NIGHT EYE",
                trait1 = "Never sleeps", trait2 = "Remembers every number", trait3 = "Runs the neighbourhood credit score",
                basePatience = 65, xpPerSettle = 11, relationPerSettle = 3, accent = 0xFFB388FF.toInt(),
                idle = "Chattering at nobody about everyone's business.",
                happy = "\"Ikaw, number 7! Lucky number, that one.\"",
                angry = "\"Ako 'to, napansin mo na ha?\"",
                broken = "\"Sige na, huwag ka nang maging number 7.\"",
            ),
            NpcArchetype(
                key = "LANDLORD_TWIN", name = "The Sibling", role = "FAMILY LEDGER",
                trait1 = "Keeps a separate tally", trait2 = "Will bring it up at 3 AM", trait3 = "Loves you, unfortunately",
                basePatience = 50, xpPerSettle = 17, relationPerSettle = 4, accent = 0xFFF48FB1.toInt(),
                idle = "A long, patient silence on the other end.",
                happy = "\"Tara, we settle this. Forever, ha.\"",
                angry = "\"Nandito 'yung listahan. Naka-underline.\"",
                broken = "\"Kapatid ko, kaya ko na 'yon. Wag mo nang alagaan.\"",
            ),
            NpcArchetype(
                key = "VENDOR", name = "The Vendor", role = "SAKSI ROLLER",
                trait1 = "Credit without a receipt", trait2 = "Remembers every order", trait3 = "Prices are elastic",
                basePatience = 70, xpPerSettle = 12, relationPerSettle = 5, accent = 0xFF4DB6AC.toInt(),
                idle = "Arranging goods that will be paid for eventually.",
                happy = "\"Abuti ka, boss! Sulat na!\"",
                angry = "\"Last price ha. Last price.\"",
                broken = "\"Bayad ka na, di na ako nag-iimpiya.\"",
            ),
            NpcArchetype(
                key = "STREET_KID", name = "The Street Kid", role = "RUNNER",
                trait1 = "Fast, never late", trait2 = "Spends before earning", trait3 = "Calls you tiktik",
                basePatience = 45, xpPerSettle = 12, relationPerSettle = 7, accent = 0xFF8BC34A.toInt(),
                idle = "Ringing once, twice, then hanging up.",
                happy = "\"Aba! Bayad ka na!\"",
                angry = "\"Tiktik na talaga, tiktik na.\"",
                broken = "\"Ako na bahala sa 'to, tiktik.\"",
            ),
            NpcArchetype(
                key = "CREDIT_ORACLE", name = "The Oracle", role = "FORECASTER",
                trait1 = "Always predicts the payment", trait2 = "Always predicts it late", trait3 = "Never wrong about the amount",
                basePatience = 35, xpPerSettle = 19, relationPerSettle = 1, accent = 0xFF00B0FF.toInt(),
                idle = "Staring at a chart you are not on.",
                happy = "\"I said you'd pay. I did not say when.\"",
                angry = "\"The chart says you won't. And the chart is right.\"",
                broken = "\"Defeat is a reading, too.\"",
                retaliation = 2,
            ),
            NpcArchetype(
                key = "GHOST_BROKER", name = "The Broker", role = "MIDDLEMAN",
                trait1 = "Never names the principal", trait2 = "Takes a cut anyway", trait3 = "Disappears after payday",
                basePatience = 55, xpPerSettle = 15, relationPerSettle = 2, accent = 0xFF9FA8DA.toInt(),
                idle = "Checking both ends of a phone he never mentions.",
                happy = "\"Consider it settled. Don't ask.\"",
                angry = "\"That's above my clearance level.\"",
                broken = "\"I don't know anything and I never did.\"",
            ),
        )

        private val BY_KEY = ALL.associateBy { it.key }

        fun all(): List<NpcArchetype> = ALL

        fun byKey(key: String): NpcArchetype = BY_KEY[key] ?: BY_KEY.getValue("COLLECTOR")

        /** Stable assignment from a person's name. */
        fun forPerson(personName: String): NpcArchetype {
            val h = (seedHash("npc:" + personName.trim().lowercase())).let { if (it == Long.MIN_VALUE) 0 else kotlin.math.abs(it) }
            return ALL[(h % ALL.size).toInt()]
        }
    }
}
