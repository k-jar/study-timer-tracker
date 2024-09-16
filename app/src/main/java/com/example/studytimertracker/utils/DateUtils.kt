package com.example.studytimertracker.utils

import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateUtils {
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun getCurrentTime(): String {
        // Return the current time in "HH:mm" format
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        return LocalTime.now().format(timeFormat)
    }
}