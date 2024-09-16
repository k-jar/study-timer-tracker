package com.example.studytimertracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.studytimertracker.ui.MainApp
import com.example.studytimertracker.viewmodel.ActivitiesViewModel
import com.example.studytimertracker.viewmodel.HistoryViewModel
import com.example.studytimertracker.viewmodel.SettingsViewModel
import com.example.studytimertracker.viewmodel.TimerViewModel

class MainActivity : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModels { TimerViewModel.Factory }
    private val activityViewModel: ActivitiesViewModel by viewModels { ActivitiesViewModel.Factory }
    private val historyViewModel: HistoryViewModel by viewModels { HistoryViewModel.Factory }
    private val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainApp(
                timerViewModel = timerViewModel,
                activityViewModel = activityViewModel,
                historyViewModel = historyViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
}