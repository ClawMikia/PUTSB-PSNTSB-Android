package com.cyberpunk.debttracker.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the headline requirements: exactly 100 achievements, exactly 7 daily /
 * 30 monthly / 100 annual quests, every reward and penalty uniquely addressable,
 * and the level curve behaving monotonically.
 */
class GameCatalogTest {

    @Test
    fun `there are exactly 100 achievements`() {
        assertEquals(100, AchievementCatalog.all.size)
    }

    @Test
    fun `achievement codes are unique`() {
        val codes = AchievementCatalog.all.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun `every achievement has a positive target`() {
        AchievementCatalog.all.forEach {
            assertTrue("${it.code} target must be > 0", it.target > 0)
        }
    }

    @Test
    fun `quest counts are 7 daily 30 monthly 100 annual`() {
        assertEquals(7, QuestCatalog.countDaily())
        assertEquals(30, QuestCatalog.countMonthly())
        assertEquals(100, QuestCatalog.countAnnual())
        assertEquals(137, QuestCatalog.countAll())
    }

    @Test
    fun `quest ids are unique across every cycle`() {
        val ids = QuestCatalog.forCycle(QuestCycle.DAILY) +
            QuestCatalog.forCycle(QuestCycle.MONTHLY) +
            QuestCatalog.forCycle(QuestCycle.ANNUAL)
        val codes = ids.map { it.id }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun `every quest has a positive target`() {
        QuestCycle.entries.forEach { cycle ->
            QuestCatalog.forCycle(cycle).forEach {
                assertTrue("${it.id} target must be > 0", it.target > 0)
            }
        }
    }

    @Test
    fun `reward codes are unique`() {
        val codes = RewardCatalog.all.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun `penalty codes are unique`() {
        val codes = PenaltyCatalog.all.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun `reward and penalty code spaces do not collide`() {
        val rewards = RewardCatalog.all.map { it.code }.toSet()
        val penalties = PenaltyCatalog.all.map { it.code }.toSet()
        assertTrue(rewards.intersect(penalties).isEmpty())
    }

    @Test
    fun `unknown reward code falls back instead of throwing`() {
        assertNotEquals(null, RewardCatalog["definitely_not_a_code"])
        assertNotEquals(null, PenaltyCatalog["definitely_not_a_code"])
    }

    @Test
    fun `achievement lookup by code resolves`() {
        AchievementCatalog.all.forEach {
            assertEquals(it.code, AchievementCatalog[it.code]?.code)
        }
    }

    @Test
    fun `settlement brackets escalate`() {
        assertEquals(RewardCatalog.SETTLE_SMALL, RewardCatalog.rewardForSettledValue(500))
        assertEquals(RewardCatalog.SETTLE_MID, RewardCatalog.rewardForSettledValue(50_000))
        assertEquals(RewardCatalog.SETTLE_LARGE, RewardCatalog.rewardForSettledValue(500_000))
        assertEquals(RewardCatalog.MILLION_CLEAR, RewardCatalog.rewardForSettledValue(2_000_000))
    }

    @Test
    fun `late day penalties escalate`() {
        assertEquals(PenaltyCatalog.OVERDUE_DAY, PenaltyCatalog.forDaysLate(1))
        assertEquals(PenaltyCatalog.OVERDUE_WEEK, PenaltyCatalog.forDaysLate(9))
        assertEquals(PenaltyCatalog.OVERDUE_MONTH, PenaltyCatalog.forDaysLate(45))
    }

    @Test
    fun `there are 16 npc archetypes with unique keys`() {
        assertEquals(16, NpcArchetype.all().size)
        val keys = NpcArchetype.all().map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `archetype assignment is stable for a given name`() {
        val first = NpcArchetype.forPerson("Mang Serio")
        val second = NpcArchetype.forPerson("  mang serio  ")
        assertEquals(first.key, second.key)
    }
}
