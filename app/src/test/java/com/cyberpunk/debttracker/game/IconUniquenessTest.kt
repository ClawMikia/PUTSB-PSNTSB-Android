package com.cyberpunk.debttracker.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The brief requires a unique teddy bear per reward and a unique white skull per
 * penalty. Those are generated procedurally from the code, so uniqueness is
 * verified here by fingerprinting every design and asserting no two collide.
 */
class IconUniquenessTest {

    /** Two designs look the same only if every visible parameter matches. */
    private fun BearDesign.fingerprint(): String = listOf(
        headShape, earShape, eyeStyle, noseShape, mouthStyle, accessory,
        furBottom, furTop, muzzleColor, noseColor, innerEarColor, accent, eyeColor,
        blush, headScaleX, headScaleY, headRadius, earSize, earAngle,
        eyeSpacing, eyeRadius, muzzleY, muzzleScale,
    ).joinToString("|")

    private fun SkullDesign.fingerprint(): String = listOf(
        craniumWidth, craniumHeight, craniumTop, jawWidth, jawBottom,
        socketShape, noseShape, accessory, boneTint, boneShade, accent,
        socketWidth, socketHeight, socketSpread, socketTilt, socketY,
        teethCount, teethWidth, crackSeed, crackDepth, browThickness, cheekAngle,
    ).joinToString("|")

    @Test
    fun `every reward gets a distinct teddy bear`() {
        val fingerprints = RewardCatalog.all.map { BearDesign.from(it.code).fingerprint() }
        assertEquals(
            "two rewards share the same bear design",
            fingerprints.size,
            fingerprints.toSet().size,
        )
    }

    @Test
    fun `every penalty gets a distinct skull`() {
        val fingerprints = PenaltyCatalog.all.map { SkullDesign.from(it.code).fingerprint() }
        assertEquals(
            "two penalties share the same skull design",
            fingerprints.size,
            fingerprints.toSet().size,
        )
    }

    @Test
    fun `every achievement gets a distinct teddy bear`() {
        val fingerprints = AchievementCatalog.all.map { BearDesign.from(it.code).fingerprint() }
        assertEquals(
            "two achievements share the same bear design",
            fingerprints.size,
            fingerprints.toSet().size,
        )
    }

    @Test
    fun `every quest gets a distinct teddy bear`() {
        val defs = QuestCycle.entries.flatMap { QuestCatalog.forCycle(it) }
        val fingerprints = defs.map { BearDesign.from(it.id).fingerprint() }
        assertEquals(
            "two quests share the same bear design",
            fingerprints.size,
            fingerprints.toSet().size,
        )
    }

    @Test
    fun `generation is deterministic for a given seed`() {
        assertEquals(BearDesign.from("RW_FIRST_SETTLE").fingerprint(), BearDesign.from("RW_FIRST_SETTLE").fingerprint())
        assertEquals(SkullDesign.from("PN_NEW_DEBT").fingerprint(), SkullDesign.from("PN_NEW_DEBT").fingerprint())
    }

    @Test
    fun `different seeds really do produce different designs`() {
        assertTrue(BearDesign.from("A").fingerprint() != BearDesign.from("B").fingerprint())
        assertTrue(SkullDesign.from("A").fingerprint() != SkullDesign.from("B").fingerprint())
    }
}
