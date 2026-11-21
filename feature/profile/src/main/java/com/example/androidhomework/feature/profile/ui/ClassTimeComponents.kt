package com.example.androidhomework.feature.profile.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClassTimePickerDialog(
    initialTime: LocalTime,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timeState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select class time") },
        text = { TimePicker(state = timeState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(timeState.hour, timeState.minute) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}