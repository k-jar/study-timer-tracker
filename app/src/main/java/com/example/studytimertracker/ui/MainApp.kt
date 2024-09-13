package com.example.studytimertracker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studytimertracker.ui.screens.ActivitiesScreen
import com.example.studytimertracker.ui.screens.TimerScreen
import com.example.studytimertracker.viewmodel.ActivitiesViewModel
import com.example.studytimertracker.viewmodel.TimerViewModel


@Composable
fun MainApp(timerViewModel: TimerViewModel, activityViewModel: ActivitiesViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "timer") {
        composable("timer") { TimerScreen(timerViewModel) }
        composable("activities") { ActivitiesScreen(activityViewModel) }
    }
}