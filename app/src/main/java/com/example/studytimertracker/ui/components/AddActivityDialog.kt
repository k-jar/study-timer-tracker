package com.example.studytimertracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studytimertracker.model.ActivityType

@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onAddActivity: (String, Float, ActivityType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var multiplier by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ActivityType.WORK) }

    var nameTouched by remember { mutableStateOf(false) }
    var multiplierTouched by remember { mutableStateOf(false) }

    // Validation states
    val isNameValid by remember { derivedStateOf { name.isNotBlank() } }
    val isMultiplierValid by remember { derivedStateOf { multiplier.toFloatOrNull() ?: 0f > 0f } }
    val isFormValid by remember { derivedStateOf { isNameValid && isMultiplierValid } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Activity") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    label = { Text("Activity Name") },
                    isError = nameTouched && !isNameValid,
                    placeholder = { Text("Enter activity name") }
                )
                TextField(
                    value = multiplier,
                    onValueChange = {
                        multiplier = it
                        multiplierTouched = true
                    },
                    label = { Text("Multiplier (e.g., 1.0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = multiplierTouched && !isMultiplierValid,
                    placeholder = { Text("Enter multiplier") }
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
                // Display validation messages
                if (nameTouched && !isNameValid) {
                    Text("Activity name cannot be empty", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
                if (multiplierTouched && !isMultiplierValid) {
                    Text("Multiplier must be a positive number", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        val multiplierValue = multiplier.toFloatOrNull() ?: 1.0f
                        onAddActivity(name, multiplierValue, type)
                    }
                },
                enabled = isFormValid
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}