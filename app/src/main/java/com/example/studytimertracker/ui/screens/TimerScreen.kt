package com.example.studytimertracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.studytimertracker.model.Activity
import com.example.studytimertracker.model.ActivityType
import com.example.studytimertracker.ui.components.ActivityLogDialog
import com.example.studytimertracker.ui.components.DropdownMenu
import com.example.studytimertracker.utils.DateTimeUtils.formatTime
import com.example.studytimertracker.viewmodel.TimerViewModel

@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    // Observe LiveData or Flow from ViewModel
    val activities by viewModel.activities.observeAsState(emptyList())
    val sessionActivities by viewModel.sessionActivities.observeAsState(emptyList())
    val workTime by viewModel.workTime.observeAsState(0L)
    val totalRestTime by viewModel.restTime.observeAsState(0L)
    val isSessionActive by viewModel.isSessionActive.observeAsState(false)
    val isWorking by viewModel.isWorking.observeAsState(false)
    val isPaused by viewModel.isPaused.observeAsState(false)

    // UI state management
    var selectedWorkActivity by remember { mutableStateOf<Activity?>(null) }
    var selectedRestActivity by remember { mutableStateOf<Activity?>(null) }
    var showActivityLog by remember { mutableStateOf(false) }

    // Initialize the first activity as default
    LaunchedEffect(activities) {
        if (activities.isNotEmpty()) {
            selectedWorkActivity = activities.firstOrNull { it.type == ActivityType.WORK }
            selectedRestActivity = activities.firstOrNull { it.type == ActivityType.REST }
        }
    }

    // Total rest time (for Rest Budget)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Work activity card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Work Activity Dropdown
                    DropdownMenu(
                        label = "Work Activity",
                        activities = activities.filter { it.type == ActivityType.WORK },
                        selectedActivity = selectedWorkActivity
                    ) { activity ->
                        selectedWorkActivity = activity
                        if (isSessionActive && isWorking) {
                            viewModel.switchMode(
                                selectedWorkActivity,
                                selectedRestActivity,
                                switch = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Timer for total work time
                    Text(
                        text = "Total Time Worked",
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = formatTime(workTime),
                        style = MaterialTheme.typography.displayLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        item {
            // Rest Activity Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (totalRestTime < 0) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else Color.Transparent) // Change background color
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Rest Activity Dropdown
                        DropdownMenu(
                            label = "Rest Activity",
                            activities = activities.filter { it.type == ActivityType.REST },
                            selectedActivity = selectedRestActivity
                        ) { activity ->
                            selectedRestActivity = activity
                            if (isSessionActive && !isWorking) {
                                viewModel.switchMode(
                                    selectedWorkActivity,
                                    selectedRestActivity,
                                    switch = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Timer for rest budget
                        Text(
                            text = "Rest Budget",
                            style = MaterialTheme.typography.displaySmall,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = formatTime(totalRestTime),
                            style = MaterialTheme.typography.displayLarge,
                            textAlign = TextAlign.Center
                        )

                        // Work and rest multipliers
                        Text(
                            text = buildAnnotatedString {
                                append("↑")
                                withStyle(style = SpanStyle(color = Color.Cyan)) {
                                    append(selectedWorkActivity?.multiplier?.toString() ?: "0")
                                    append("x")
                                }
                                append("     ↓")
                                withStyle(style = SpanStyle(color = Color.Red)) {
                                    append(selectedRestActivity?.multiplier?.toString() ?: "0")
                                    append("x")
                                }
                            },
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        item {
            // Start Day and Switch Buttons
            Row {
                Button(
                    onClick = {
                        if (!isSessionActive) {
                            val selectedWork = selectedWorkActivity ?: return@Button
                            val selectedRest = selectedRestActivity ?: return@Button
                            viewModel.startSession(selectedWork, selectedRest)
                        } else {
                            viewModel.switchMode(
                                selectedWorkActivity,
                                selectedRestActivity,
                                switch = true
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSessionActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        if (!isSessionActive and (selectedWorkActivity != null) and (selectedRestActivity != null)) "Start Day"
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
        }

        item {
            Button(onClick = { showActivityLog = true }) {
                Text("Show Activity Log")
            }

            if (showActivityLog) {
                ActivityLogDialog(
                    sessionActivities = sessionActivities,
                    activities = activities,
                    onDismiss = { showActivityLog = false }
                )
            }
        }
        }
    }


