package com.cyberpunk.debttracker.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cyberpunk.debttracker.data.repository.DebtRepository
import com.cyberpunk.debttracker.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class OverdueReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: DebtRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val overdueCount = repository.getOverdueCount().first()
            if (overdueCount > 0) {
                NotificationHelper.showOverdueSummaryNotification(context, overdueCount)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
