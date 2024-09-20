package com.example.studytimertracker

import android.app.Application
import com.example.studytimertracker.data.AppDatabase
import com.example.studytimertracker.data.TimerRepository

class StudyTimerTrackerApp : Application() {
    // Lazy initialization of the AppDatabase
    private val database by lazy { AppDatabase.getDatabase(this) }

    // Lazy initialization of the TimerRepository
    val timerRepository by lazy {
        TimerRepository(
            activityDao = database.activityDao(),
            restStoreDao = database.restStoreDao(),
            userPreferencesDao = database.userPreferencesDao(),
            historyDao = database.historyDao(),
            sessionActivityDao = database.sessionActivityDao()
        )
    }
}