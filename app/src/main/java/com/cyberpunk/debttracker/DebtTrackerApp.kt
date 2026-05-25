package com.cyberpunk.debttracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DebtTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // App-level init (analytics, logging, etc.) goes here
    }
}
