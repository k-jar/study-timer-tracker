package com.example.studytimertracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog

import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studytimertracker.model.Activity
import com.example.studytimertracker.model.ActivityType

@Composable
fun UpdateDeleteActivityDialog(
    activity: Activity,
    onDismiss: () -> Unit,
    onUpdate: (String, Float, ActivityType) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(activity.name) }
    var multiplier by remember { mutableStateOf(activity.multiplier.toString()) }
    var type by remember { mutableStateOf(activity.type) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Update Activity") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Activity Name") }
                )
                TextField(
                    value = multiplier,
                    onValueChange = { multiplier = it },
                    label = { Text("Multiplier (e.g., 1.0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row {
                    RadioButton(
                        selected = type == ActivityType.WORK,
                        onClick = { type = ActivityType.WORK }
                    )
                    Text("Work")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(
                        selected = type == ActivityType.REST,
                        onClick = { type = ActivityType.REST }
                    )
                    Text("Rest")
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDelete){
                    Text("Delete")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                TextButton(onClick = {
                    val multiplierValue = multiplier.toFloatOrNull() ?: 1.0f
                    onUpdate(name, multiplierValue, type)
                }) {
                    Text("Update")
                }
            }
        }
    )
}