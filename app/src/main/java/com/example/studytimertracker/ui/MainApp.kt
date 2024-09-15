package com.example.studytimertracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.studytimertracker.ui.components.BottomNavigationBar
import com.example.studytimertracker.ui.navigation.NavigationComponent
import com.example.studytimertracker.viewmodel.ActivitiesViewModel
import com.example.studytimertracker.viewmodel.HistoryViewModel
import com.example.studytimertracker.viewmodel.TimerViewModel


@Composable
fun MainApp(timerViewModel: TimerViewModel, activityViewModel: ActivitiesViewModel, historyViewModel: HistoryViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavigationComponent(
            navController = navController,
            timerViewModel = timerViewModel,
            activityViewModel = activityViewModel,
            historyViewModel = historyViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}