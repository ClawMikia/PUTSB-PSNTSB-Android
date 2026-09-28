package com.cyberpunk.debttracker.di

import android.content.Context
import androidx.room.Room
import com.cyberpunk.debttracker.data.db.DebtDao
import com.cyberpunk.debttracker.data.db.DebtDatabase
import com.cyberpunk.debttracker.data.db.GameDao
import com.cyberpunk.debttracker.data.db.GameDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDebtDatabase(@ApplicationContext context: Context): DebtDatabase {
        return Room.databaseBuilder(
            context,
            DebtDatabase::class.java,
            DebtDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDebtDao(database: DebtDatabase): DebtDao {
        return database.debtDao()
    }

    // --- Gamification -------------------------------------------------------
    //
    // Deliberately a separate database file. It is listed in
    // res/xml/backup_rules.xml and res/xml/data_extraction_rules.xml as an
    // <exclude>, so nothing here is ever backed up or transferred. Uninstall
    // wipes the campaign back to zero while debt records survive.

    @Provides
    @Singleton
    fun provideGameDatabase(@ApplicationContext context: Context): GameDatabase {
        return Room.databaseBuilder(
            context,
            GameDatabase::class.java,
            GameDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideGameDao(database: GameDatabase): GameDao {
        return database.gameDao()
    }
}
