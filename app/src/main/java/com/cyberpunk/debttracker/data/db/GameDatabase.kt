package com.cyberpunk.debttracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cyberpunk.debttracker.data.model.game.*

/**
 * Dedicated database for the whole gamification campaign.
 *
 * Kept separate from [DebtDatabase] on purpose: backup_rules.xml and
 * data_extraction_rules.xml exclude this file, so uninstalling the app
 * wipes every achievement, quest, NPC, coin, bolt and level. A fresh
 * install starts again at level 1 with 0 XP, 0 coins, nothing unlocked.
 */
@Database(
    entities = [
        PlayerProfile::class,
        StatCounter::class,
        StatGauge::class,
        CycleBaseline::class,
        AchievementState::class,
        QuestState::class,
        Npc::class,
        GameLogEntry::class,
        RolloverState::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(GameConverters::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        const val DATABASE_NAME = "gamification_db"
    }
}
