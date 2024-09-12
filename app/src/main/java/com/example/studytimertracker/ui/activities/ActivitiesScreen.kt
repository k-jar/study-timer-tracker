package com.example.studytimertracker.ui.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studytimertracker.model.ActivityType
import com.example.studytimertracker.viewmodel.ActivitiesViewModel
import com.example.studytimertracker.viewmodel.TimerViewModel

@Composable
fun ActivitiesScreen(viewModel: ActivitiesViewModel) {
    val activities by viewModel.activities.observeAsState(emptyList())

    var showDialog by remember { mutableStateOf(false) }

    // Separate work and rest activities
    val workActivities = activities.filter { it.type == ActivityType.WORK }
    val restActivities = activities.filter { it.type == ActivityType.REST }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Work Activities", style = MaterialTheme.typography.titleLarge)

        // List of Work Activities
        LazyColumn {
            items(workActivities) { activity ->
                ActivityRow(activity = activity)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Rest Activities", style = MaterialTheme.typography.titleLarge)

        // List of Rest Activities
        LazyColumn {
            items(restActivities) { activity ->
                ActivityRow(activity = activity)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Activity Button
        Button(onClick = { showDialog = true }) {
            Text("Add Activity")
        }

        if (showDialog) {
            AddActivityDialog(onDismiss = { showDialog = false }) { name, multiplier, type ->
                viewModel.addActivity(name, multiplier, type) // Add the activity through the ViewModel
                showDialog = false
            }
        }


    }
}
