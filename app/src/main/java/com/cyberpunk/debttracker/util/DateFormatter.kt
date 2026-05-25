package com.cyberpunk.debttracker.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateFormatter {

    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val inputFormat   = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    fun formatDisplay(timestamp: Long): String = displayFormat.format(Date(timestamp))

    fun formatInput(timestamp: Long): String = inputFormat.format(Date(timestamp))

    fun parseInput(dateString: String): Long? = try {
        inputFormat.parse(dateString)?.time
    } catch (e: Exception) {
        null
    }

    fun daysUntilDue(dueTimestamp: Long): Long {
        val now = System.currentTimeMillis()
        val diff = dueTimestamp - now
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    fun dueDateLabel(dueTimestamp: Long): String {
        val days = daysUntilDue(dueTimestamp)
        return when {
            days < 0     -> "Overdue by ${-days}d"
            days == 0L   -> "Due today"
            days == 1L   -> "Due tomorrow"
            days <= 7    -> "Due in ${days}d"
            else         -> "Due ${formatDisplay(dueTimestamp)}"
        }
    }

    fun isOverdue(dueTimestamp: Long?): Boolean {
        if (dueTimestamp == null) return false
        return dueTimestamp < System.currentTimeMillis()
    }

    fun startOfCurrentMonth(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun monthLabel(timestamp: Long): String =
        SimpleDateFormat("MMM", Locale.getDefault()).format(Date(timestamp))
}
