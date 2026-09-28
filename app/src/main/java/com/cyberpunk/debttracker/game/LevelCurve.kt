package com.cyberpunk.debttracker.game

import kotlin.math.floor
import kotlin.math.pow

// ═══════════════════════════════════════════════════════════════════════════
//  PLAYER PROGRESSION
// ═══════════════════════════════════════════════════════════════════════════

object LevelCurve {

    const val MAX_LEVEL = 500

    /** XP required to advance *from* the given level to the next one. */
    fun xpToNext(level: Int): Int {
        if (level >= MAX_LEVEL) return Int.MAX_VALUE / 4
        val l = level.coerceAtLeast(1).toDouble()
        return (90.0 + 60.0 * l + 4.0 * l * l).toInt()
    }

    /** Total XP needed to reach a level from scratch. */
    fun cumulativeXp(level: Int): Int {
        var total = 0L
        for (l in 1 until level.coerceIn(1, MAX_LEVEL)) total += xpToNext(l)
        return total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    data class Result(val level: Int, val xpIntoLevel: Int, val needed: Int, val levelsGained: Int) {
        val progressPercent: Int
            get() = if (needed <= 0) 100 else ((xpIntoLevel * 100) / needed).coerceIn(0, 100)
    }

    fun evaluate(xp: Int, level: Int): Result {
        var lvl = level.coerceIn(1, MAX_LEVEL)
        var remaining = xp
        var gained = 0
        while (lvl < MAX_LEVEL) {
            val need = xpToNext(lvl)
            if (remaining < need) break
            remaining -= need
            lvl++
            gained++
        }
        return Result(lvl, remaining, xpToNext(lvl), gained)
    }

    /** Cyberpunk-flavored rank titles awarded at level milestones. */
    fun rankTitle(level: Int): String = when {
        level >= 300 -> "LEGEND OF THE LEDGER"
        level >= 200 -> "DEBT EMPEROR"
        level >= 150 -> "NEON ARBITER"
        level >= 100 -> "SYNTHETIC MONEYBAG"
        level >= 75 -> "CREDIT TITAN"
        level >= 50 -> "LOAN SHARK SUPREME"
        level >= 35 -> "INTEREST GOBLIN"
        level >= 25 -> "SETTLEMENT KNIGHT"
        level >= 15 -> "STREET ACCOUNTANT"
        level >= 10 -> "COLLECTION ADEPT"
        level >= 5 -> "LEDGER GRUNT"
        level >= 2 -> "ROOKIE NODE"
        else -> "FRESH IN THE MATRIX"
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  QUEST CYCLES
// ═══════════════════════════════════════════════════════════════════════════

enum class QuestCycle(val label: String) {
    DAILY("DAILY CONTRACTS"),
    MONTHLY("MONTHLY OPERATIONS"),
    ANNUAL("ANNUAL CAMPAIGN"),
}

object Cycles {

    fun epochDay(now: Long = System.currentTimeMillis()): Long =
        java.time.Instant.ofEpochMilli(now)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .toEpochDay()

    fun dailyKey(now: Long = System.currentTimeMillis()): String = "D" + epochDay(now)

    fun monthlyKey(now: Long = System.currentTimeMillis()): String {
        val d = java.time.Instant.ofEpochMilli(now).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        return "M%d%02d".format(d.year, d.monthValue)
    }

    fun annualKey(now: Long = System.currentTimeMillis()): String {
        val d = java.time.Instant.ofEpochMilli(now).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        return "Y" + d.year
    }

    fun keyFor(cycle: QuestCycle, now: Long = System.currentTimeMillis()): String = when (cycle) {
        QuestCycle.DAILY -> dailyKey(now)
        QuestCycle.MONTHLY -> monthlyKey(now)
        QuestCycle.ANNUAL -> annualKey(now)
    }

    /** Local midnight of the given epoch day, in millis. */
    fun dayStartMillis(epochDay: Long): Long =
        java.time.LocalDate.ofEpochDay(epochDay)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    /** Millis until the next local midnight. */
    fun millisUntilTomorrow(now: Long = System.currentTimeMillis()): Long {
        val today = epochDay(now)
        val tomorrowStart = dayStartMillis(today + 1)
        return (tomorrowStart - now).coerceAtLeast(0L)
    }

    fun millisUntilNextMonth(now: Long = System.currentTimeMillis()): Long {
        val z = java.time.ZoneId.systemDefault()
        val d = java.time.Instant.ofEpochMilli(now).atZone(z).toLocalDate()
        val next = d.withDayOfMonth(1).plusMonths(1).atStartOfDay(z).toInstant().toEpochMilli()
        return (next - now).coerceAtLeast(0L)
    }

    fun millisUntilNextYear(now: Long = System.currentTimeMillis()): Long {
        val z = java.time.ZoneId.systemDefault()
        val d = java.time.Instant.ofEpochMilli(now).atZone(z).toLocalDate()
        val next = d.withDayOfYear(1).plusYears(1).atStartOfDay(z).toInstant().toEpochMilli()
        return (next - now).coerceAtLeast(0L)
    }

    fun millisUntilReset(cycle: QuestCycle, now: Long = System.currentTimeMillis()): Long = when (cycle) {
        QuestCycle.DAILY -> millisUntilTomorrow(now)
        QuestCycle.MONTHLY -> millisUntilNextMonth(now)
        QuestCycle.ANNUAL -> millisUntilNextYear(now)
    }

    fun formatDuration(millis: Long): String {
        val totalHours = floor(millis / 3_600_000.0).toLong()
        val days = totalHours / 24
        val hours = totalHours % 24
        val minutes = (millis % 3_600_000L) / 60_000L
        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
}
