package com.example.studytimertracker.utils

import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Return the current time in "HH:mm" format
    fun getCurrentTime(): String {
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        return LocalTime.now().format(timeFormat)
    }

    // Convert time in ms to "HH:mm:ss" format
    fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
}