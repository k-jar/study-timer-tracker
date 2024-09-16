package com.example.studytimertracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studytimertracker.ui.components.BottomNavigationBar
import com.example.studytimertracker.ui.components.TitleSettingsBar
import com.example.studytimertracker.ui.navigation.NavigationComponent
import com.example.studytimertracker.ui.screens.SettingsScreen
import com.example.studytimertracker.viewmodel.ActivitiesViewModel
import com.example.studytimertracker.viewmodel.HistoryViewModel
import com.example.studytimertracker.viewmodel.SettingsViewModel
import com.example.studytimertracker.viewmodel.TimerViewModel


@Composable
fun MainApp(timerViewModel: TimerViewModel, activityViewModel: ActivitiesViewModel, historyViewModel: HistoryViewModel, settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    var showSettingsDialog by remember { mutableStateOf(false) }

    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = currentBackStackEntry?.destination?.route

    val title = when (currentDestination) {
        "timer" -> "Timer"
        "activities" -> "Activities"
        "history" -> "History"
        "settings" -> "Settings"
        else -> "App Title"
    }

    Scaffold(
        topBar = {
            TitleSettingsBar(
                title = title,
                onSettingsClick = {
                    showSettingsDialog = true
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavigationComponent(
            navController = navController,
            timerViewModel = timerViewModel,
            activityViewModel = activityViewModel,
            historyViewModel = historyViewModel,
            settingsViewModel = settingsViewModel,
            modifier = Modifier.padding(innerPadding)
        )
        if (showSettingsDialog) {
            Dialog(onDismissRequest = { showSettingsDialog = false }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(16.dp)
                ) {
                    SettingsScreen(viewModel = settingsViewModel)
                }
            }
        }
    }
}