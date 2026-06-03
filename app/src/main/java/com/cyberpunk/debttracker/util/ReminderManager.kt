package com.cyberpunk.debttracker.util

import android.content.Context
import androidx.work.*
import com.cyberpunk.debttracker.notification.OverdueReminderWorker
import java.util.concurrent.TimeUnit

object ReminderManager {
    private const val WORK_NAME = "overdue_reminder_work"

    fun scheduleReminders(context: Context, frequency: String) {
        val workManager = WorkManager.getInstance(context)
        
        if (frequency == "none") {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val repeatInterval = when (frequency) {
            "hourly" -> 1L to TimeUnit.HOURS
            "daily"  -> 1L to TimeUnit.DAYS
            "weekly" -> 7L to TimeUnit.DAYS
            else     -> 1L to TimeUnit.DAYS
        }

        val workRequest = PeriodicWorkRequestBuilder<OverdueReminderWorker>(
            repeatInterval.first, repeatInterval.second
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        ).build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
