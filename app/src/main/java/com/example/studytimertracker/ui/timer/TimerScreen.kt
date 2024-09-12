package com.example.studytimertracker.ui.timer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studytimertracker.model.Activity
import com.example.studytimertracker.model.ActivityType
import com.example.studytimertracker.viewmodel.TimerViewModel

@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    // Observe LiveData or Flow from ViewModel
    val restStore by viewModel.restStore.observeAsState()
    val activities by viewModel.activities.observeAsState(emptyList())
    val workTime by viewModel.workTime.observeAsState(0L)
    val isSessionActive by viewModel.isSessionActive.observeAsState(false)
    val isWorking by viewModel.isWorking.observeAsState(false)
    val isPaused by viewModel.isPaused.observeAsState(false)

    // UI state management
    var selectedWorkActivity by remember { mutableStateOf<Activity?>(null) }
    var selectedRestActivity by remember { mutableStateOf<Activity?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // Initialize the first activity as default
    LaunchedEffect(activities) {
        if (activities.isNotEmpty()) {
            selectedWorkActivity = activities.firstOrNull { it.type == ActivityType.WORK }
            selectedRestActivity = activities.firstOrNull { it.type == ActivityType.REST }
        }
    }

    // Total rest time (for Rest Budget)
    val totalRestTime = restStore?.totalRestTime ?: 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Work Activity Dropdown
        Text("Work Activity")
        DropdownMenu(
            activities.filter { it.type == ActivityType.WORK },
            selectedWorkActivity
        ) { activity ->
            selectedWorkActivity = activity
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer for total work time
        Text(
            text = "Total Time Worked: ${workTime / 1000}s",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Rest Activity Dropdown
        Text("Rest Activity")
        DropdownMenu(
            activities.filter { it.type == ActivityType.REST },
            selectedRestActivity
        ) { activity ->
            selectedRestActivity = activity
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer for rest budget
        Text(
            text = "Rest Budget: ${totalRestTime / 1000}s",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Start/Stop Session and Switch Buttons
        Row {
            Button(
                onClick = {
                    if (!isSessionActive) {
                        // Start the session with the selected work activity
                        val selectedActivity = selectedWorkActivity ?: return@Button
                        viewModel.startSession(selectedActivity, true)
                    } else {
                        // Switch between work and rest mode
                        viewModel.switchMode()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSessionActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    if (!isSessionActive) "Start Session"
                    else if (isWorking) "Switch to Rest"
                    else "Switch to Work"
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Pause/Resume Button
            if (isSessionActive) {
                Button(
                    onClick = {
                        if (isPaused) {
                            viewModel.resumeTimer()
                        } else {
                            viewModel.pauseTimer()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isPaused) "Resume" else "Pause")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // End session button
        if (isSessionActive && isPaused) {
            Button(
                onClick = {
                    viewModel.endSession()
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
            ) {
                Text("End Session")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
