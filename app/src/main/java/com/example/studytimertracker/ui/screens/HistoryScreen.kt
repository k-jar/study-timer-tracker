package com.example.studytimertracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studytimertracker.model.Activity
import com.example.studytimertracker.model.History
import com.example.studytimertracker.model.SessionActivity
import com.example.studytimertracker.ui.components.ActivityLogDialog
import com.example.studytimertracker.ui.components.HistoryCalendar
import com.example.studytimertracker.viewmodel.HistoryViewModel
import com.kizitonwose.calendar.core.CalendarDay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, activities: List<Activity>) {
    // State for selected date
    val selectedDate = remember { mutableStateOf<CalendarDay?>(null) }

    // Observe the history for the selected day
    val historyList by viewModel.getHistoryByDate(selectedDate.value?.date.toString()).observeAsState(emptyList())

    val showActivityLogDialog by viewModel.showActivityLogDialog.observeAsState(false)
    val selectedActivities by viewModel.selectedHistoryActivities.observeAsState(emptyList())


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Calendar Section
        Text(text = "Select a Date", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Persistent Calendar at the top
        HistoryCalendar { selectedDay ->
            selectedDate.value = selectedDay
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Show history details for selected day
        Text(text = "History for ${selectedDate.value?.date ?: "Select a date"}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(historyList) { history ->
                HistoryCard(
                    history=history,
                    onShowActivityLog = { sessionActivities -> viewModel.showActivityLog(sessionActivities) }
                )
            }
        }

        if (showActivityLogDialog) {
            ActivityLogDialog(
                sessionActivities = selectedActivities,
                activities = activities,
                onDismiss = { viewModel.dismissActivityLog() }
            )
        }
    }
}

@Composable
fun HistoryCard(
    history: History,
    onShowActivityLog: (List<SessionActivity>) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    // Convert milliseconds to formatted time
    val startTime = Instant.ofEpochMilli(history.dayStartTime)
        .atZone(ZoneId.systemDefault())
        .format(formatter)

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Day Start Time: $startTime", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Total Time Worked: ${history.totalTimeWorked / 1000}s", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Rest Accumulated: ${history.restStoreAccumulated / 1000}s", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Rest Used: ${history.restStoreUsed / 1000}s", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))

            // Button to open activity log dialog
            Button(onClick = { onShowActivityLog(history.sessionActivities) }) {
                Text(text = "Show Activity Log")
            }
        }
    }
}