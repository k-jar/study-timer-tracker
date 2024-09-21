package com.example.studytimertracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.studytimertracker.model.Activity
import com.example.studytimertracker.model.SessionActivity

@Composable
fun ActivityLogDialog(
    sessionActivities: List<SessionActivity>,
    activities: List<Activity>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Activity Log",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyColumn {
                    items(sessionActivities) { sessionActivity ->
                        // Pause has activityId = -1
                        val activity = if (sessionActivity.activityId == -1) {
                            null
                        } else {
                            activities.find { it.id == sessionActivity.activityId } // Look up the activity
                        }
                        SessionActivityItem(sessionActivity = sessionActivity, activity = activity)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onDismiss() }) {
                    Text(text = "Close")
                }
            }
        }
    }
}