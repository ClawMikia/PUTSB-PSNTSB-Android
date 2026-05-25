package com.cyberpunk.debttracker.di

import android.content.Context
import androidx.room.Room
import com.cyberpunk.debttracker.data.db.DebtDao
import com.cyberpunk.debttracker.data.db.DebtDatabase
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
}
