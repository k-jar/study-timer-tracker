package com.example.studytimertracker.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studytimertracker.model.History
import com.example.studytimertracker.ui.components.HistoryCalendar
import com.example.studytimertracker.viewmodel.HistoryViewModel
import com.kizitonwose.calendar.core.CalendarDay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    // State for selected date
    val selectedDate = remember { mutableStateOf<CalendarDay?>(null) }

    // Observe the history for the selected day
    val historyList by viewModel.getHistoryByDate(selectedDate.value?.date.toString()).observeAsState(emptyList())

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
                HistoryCard(history)
            }
        }

        // Button for more details
        Button(
            onClick = { /* TODO: Show more details */ },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
        ) {
            Text("More Details")
        }
    }
}

@Composable
fun HistoryCard(history: History) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    // Convert milliseconds to formatted time
    val startTime = Instant.ofEpochMilli(history.sessionStartTime)
        .atZone(ZoneId.systemDefault())
        .format(formatter)

    val endTime = Instant.ofEpochMilli(history.sessionEndTime)
        .atZone(ZoneId.systemDefault())
        .format(formatter)

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Session Start: $startTime", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Session End: $endTime", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Total Time Worked: ${history.totalTimeWorked / 1000}s", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Rest Accumulated: ${history.restStoreAccumulated / 1000}s", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Rest Used: ${history.restStoreUsed / 1000}s", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun DatePickerDialog(onDateSelected: (String) -> Unit, onDismiss: () -> Unit) {
    // Get the current context to display the dialog
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // LaunchedEffect ensures this code is executed in a proper lifecycle context
    LaunchedEffect(Unit) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDate = "$dayOfMonth-${month + 1}-$year"
                onDateSelected(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { onDismiss() }
            show()
        }
    }
}