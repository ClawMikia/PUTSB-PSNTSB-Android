package com.cyberpunk.debttracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.ui.debtdetail.DebtDetailActivity

object NotificationHelper {
    private const val CHANNEL_ID = "debt_reminders"
    private const val CHANNEL_NAME = "Debt Reminders"
    private const val CHANNEL_DESC = "Notifications for upcoming and overdue debts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDebtNotification(context: Context, debt: Debt) {
        val intent = Intent(context, DebtDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(DebtDetailActivity.EXTRA_DEBT, debt)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, debt.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_owe) // Using an existing icon
            .setContentTitle("Debt Due: ${debt.personName}")
            .setContentText("The debt of ${debt.amount.toCurrencyString()} is due today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(debt.id.toInt(), builder.build())
    }

    fun showOverdueSummaryNotification(context: Context, overdueCount: Int) {
        val intent = Intent(context, com.cyberpunk.debttracker.ui.dashboard.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("Overdue Debt Alert")
            .setContentText("You have $overdueCount overdue debt nodes that need attention!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(999, builder.build())
    }

    fun scheduleDebtNotification(context: Context, debt: Debt) {
        val dueDate = debt.dueDate ?: return
        if (dueDate < System.currentTimeMillis()) return

        val intent = Intent(context, com.cyberpunk.debttracker.notification.NotificationReceiver::class.java).apply {
            putExtra("extra_debt", debt)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, debt.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
            } else {
                alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
        }
    }
}
