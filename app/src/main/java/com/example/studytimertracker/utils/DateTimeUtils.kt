package com.example.studytimertracker.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.abs

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
        val totalSeconds = abs(milliseconds / 1000)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

        // Return formatted time with a negative sign if milliseconds are negative
        return if (milliseconds < 0) "-$formattedTime" else formattedTime
    }

    fun formatTimestamp(timeMillis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
        return formatter.format(Instant.ofEpochMilli(timeMillis))
    }

    fun getPreviousDay(currentDateString: String): String {
        // Parse the current date string
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val currentDate = LocalDate.parse(currentDateString, formatter)

        // Subtract one day
        val previousDate = currentDate.minusDays(1)

        // Return as formatted string
        return previousDate.format(formatter)
    }
}