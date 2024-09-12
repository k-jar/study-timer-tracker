package com.example.studytimertracker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studytimertracker.viewmodel.TimerViewModel


@Composable
fun MainApp(viewModel: TimerViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "timer") {
        composable("timer") { TimerScreen(viewModel) }
    }
}