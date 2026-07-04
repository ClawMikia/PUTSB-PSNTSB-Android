package com.cyberpunk.debttracker.util

import android.content.Context
import androidx.preference.PreferenceManager

object SecurityHelper {
    private const val PREF_ARCHIVE_PASSWORD = "archive_password"
    
    fun getArchivePassword(context: Context): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(PREF_ARCHIVE_PASSWORD, null)
    }

    fun setArchivePassword(context: Context, password: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(PREF_ARCHIVE_PASSWORD, password).apply()
    }

    fun hasArchivePassword(context: Context): Boolean {
        return !getArchivePassword(context).isNullOrBlank()
    }
}
