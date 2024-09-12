package com.example.studytimertracker.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studytimertracker.model.Activity

@Composable
fun DropdownMenu(
    activities: List<Activity>,
    selectedActivity: Activity?,
    onActivitySelected: (Activity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = selectedActivity?.name ?: "Select Activity",
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            activities.forEach { activity ->
                DropdownMenuItem(
                    text = { Text(activity.name) },
                    onClick = {
                        onActivitySelected(activity)
                        expanded = false
                    }
                )
            }
        }
    }
}