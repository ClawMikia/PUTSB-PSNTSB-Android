package com.cyberpunk.debttracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cyberpunk.debttracker.data.model.Debt

@Database(
    entities = [Debt::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class DebtDatabase : RoomDatabase() {
    abstract fun debtDao(): DebtDao

    companion object {
        const val DATABASE_NAME = "debt_tracker_db"
    }
}
