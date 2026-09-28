package com.cyberpunk.debttracker.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelCurveTest {

    @Test
    fun `a fresh player is level one with no progress`() {
        val result = LevelCurve.evaluate(xp = 0, level = 1)
        assertEquals(1, result.level)
        assertEquals(0, result.levelsGained)
        assertEquals(0, result.progressPercent)
    }

    @Test
    fun `xp requirements strictly increase with level`() {
        var previous = 0
        for (level in 1..60) {
            val need = LevelCurve.xpToNext(level)
            assertTrue("level $level need $need must exceed $previous", need > previous)
            previous = need
        }
    }

    @Test
    fun `exactly enough xp grants exactly one level`() {
        val need = LevelCurve.xpToNext(1)
        val result = LevelCurve.evaluate(xp = need, level = 1)
        assertEquals(2, result.level)
        assertEquals(1, result.levelsGained)
        assertEquals(0, result.xpIntoLevel)
    }

    @Test
    fun `one xp short does not level up`() {
        val need = LevelCurve.xpToNext(1)
        val result = LevelCurve.evaluate(xp = need - 1, level = 1)
        assertEquals(1, result.level)
        assertEquals(0, result.levelsGained)
    }

    @Test
    fun `a large xp lump can gain several levels at once`() {
        val result = LevelCurve.evaluate(xp = 100_000, level = 1)
        assertTrue("expected multiple levels, got ${result.levelsGained}", result.levelsGained > 1)
        assertTrue(result.level > 1)
    }

    @Test
    fun `level is capped at the maximum`() {
        val result = LevelCurve.evaluate(xp = Int.MAX_VALUE, level = 1)
        assertEquals(LevelCurve.MAX_LEVEL, result.level)
    }

    @Test
    fun `rank titles get more impressive as levels rise`() {
        val early = LevelCurve.rankTitle(1)
        val late = LevelCurve.rankTitle(500)
        assertTrue(early != late)
        assertTrue(LevelCurve.rankTitle(100) != LevelCurve.rankTitle(200))
    }
}

class CyclesTest {

    @Test
    fun `daily monthly and annual keys are distinct shapes`() {
        val now = System.currentTimeMillis()
        val daily = Cycles.dailyKey(now)
        val monthly = Cycles.monthlyKey(now)
        val annual = Cycles.annualKey(now)
        assertTrue(daily.startsWith("D"))
        assertTrue(monthly.startsWith("M"))
        assertTrue(annual.startsWith("Y"))
    }

    @Test
    fun `keyFor matches the individual helpers`() {
        val now = System.currentTimeMillis()
        QuestCycle.entries.forEach { cycle ->
            assertEquals(Cycles.keyFor(cycle, now), Cycles.keyFor(cycle, now))
        }
        assertEquals(Cycles.dailyKey(now), Cycles.keyFor(QuestCycle.DAILY, now))
        assertEquals(Cycles.monthlyKey(now), Cycles.keyFor(QuestCycle.MONTHLY, now))
        assertEquals(Cycles.annualKey(now), Cycles.keyFor(QuestCycle.ANNUAL, now))
    }

    @Test
    fun `reset countdowns are always in the future`() {
        QuestCycle.entries.forEach { cycle ->
            assertTrue(
                "$cycle must reset later",
                Cycles.millisUntilReset(cycle) >= 0L,
            )
        }
    }

    @Test
    fun `duration formatting is human readable`() {
        assertTrue(Cycles.formatDuration(0).isNotEmpty())
        assertEquals("1d 2h", Cycles.formatDuration(26 * 3_600_000L))
        assertEquals("5m", Cycles.formatDuration(5 * 60_000L))
    }
}
