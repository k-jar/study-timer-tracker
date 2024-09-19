package com.example.studytimertracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.studytimertracker.model.Activity

@Composable
fun DropdownMenu(
    label: String,
    activities: List<Activity>,
    selectedActivity: Activity?,
    onActivitySelected: (Activity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column (
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .border(1.dp, Color.Gray, MaterialTheme.shapes.small)
                .background(Color.Transparent, MaterialTheme.shapes.small)
                .clickable { expanded = true }
                .padding(8.dp)
        ) {
            Text(
                text = selectedActivity?.name ?: "Select $label",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = if (selectedActivity == null) Color.Gray else MaterialTheme.colorScheme.onSurface
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            activities.forEach { activity ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(activity.name)
                            Text("x${activity.multiplier}")  // Show multiplier
                        }
                    },
                    onClick = {
                        onActivitySelected(activity)
                        expanded = false
                    }
                )
            }
        }
    }
}
