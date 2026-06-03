package com.cyberpunk.debttracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.util.NotificationHelper
import androidx.core.content.IntentCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val debt = IntentCompat.getParcelableExtra(intent, "extra_debt", Debt::class.java)
        debt?.let {
            NotificationHelper.showDebtNotification(context, it)
        }
    }
}
