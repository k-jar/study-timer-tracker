package com.example.studytimertracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studytimertracker.model.Activity
import com.example.studytimertracker.model.ActivityType
import com.example.studytimertracker.model.SessionActivity
import com.example.studytimertracker.utils.DateTimeUtils.formatTime
import com.example.studytimertracker.utils.DateTimeUtils.formatTimestamp

@Composable
fun SessionActivityItem(sessionActivity: SessionActivity, activity: Activity?) {
    val startTime = formatTimestamp(sessionActivity.startTime)
    val endTime = formatTimestamp(sessionActivity.endTime)

    // If it's a pause session, don't calculate rest or work time
    if (activity == null || sessionActivity.activityId == -1) {
        val duration = sessionActivity.endTime - sessionActivity.startTime
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = MaterialTheme.shapes.small
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Paused")
                Text(text = "Start: $startTime")
                Text(text = "End: $endTime")
                Text(text = "Duration: ${formatTime(duration)}")
            }
        }
    } else {
        var duration = sessionActivity.endTime - sessionActivity.startTime
        val restAccumulated = (duration * activity.multiplier).toLong()
        val restConsumed = (duration * activity.multiplier).toLong()

        val activityType = if (activity.type == ActivityType.WORK) "Work" else "Rest"

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = MaterialTheme.shapes.small
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "$activityType Activity: ${activity.name}")
                Text(text = "Start: $startTime")
                Text(text = "End: $endTime")
                Text(text = "Duration: ${formatTime(duration)}")

                // Show rest accumulated or consumed depending on activity type
                if (activity.type == ActivityType.REST) {
                    Text(text = "Rest accumulated: ${formatTime(restAccumulated)}")
                } else {
                    Text(text = "Rest consumed: ${formatTime(restConsumed)}")
                }
            }
        }
    }
}