package com.grama.vasathi

import android.app.Application
import android.util.Log
import kotlin.system.exitProcess

class GramaVasathiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("GramaVasathi", "CRASH in thread ${thread.name}", throwable)
            // In a real app, you'd send this to Crashlytics
            exitProcess(1)
        }
    }
}
