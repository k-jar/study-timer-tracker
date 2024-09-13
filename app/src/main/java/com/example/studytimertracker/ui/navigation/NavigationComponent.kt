package com.example.studytimertracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.studytimertracker.ui.screens.ActivitiesScreen
import com.example.studytimertracker.ui.screens.TimerScreen
import com.example.studytimertracker.viewmodel.ActivitiesViewModel
import com.example.studytimertracker.viewmodel.TimerViewModel

sealed class Screen(val route: String) {
    data object Timer : Screen("timer")
    data object Activities : Screen("activities")
}

@Composable
fun NavigationComponent(
    navController: NavHostController,
    timerViewModel: TimerViewModel,
    activityViewModel: ActivitiesViewModel,
    modifier: Modifier
) {
    NavHost(navController = navController, startDestination = Screen.Timer.route, modifier = modifier) {
        composable(Screen.Timer.route) { TimerScreen(timerViewModel) }
        composable(Screen.Activities.route) { ActivitiesScreen(activityViewModel) }
    }
}