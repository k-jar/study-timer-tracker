package com.example.studytimertracker.utils

import java.time.DayOfWeek
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

object CalendarUtils {
    private fun Month.displayText(short: Boolean = true): String {
        val style = if (short) TextStyle.SHORT else TextStyle.FULL
        return getDisplayName(style, Locale.ENGLISH)
    }

    fun YearMonth.displayText(short: Boolean = true): String {
        return "${this.month.displayText(short = short)} ${this.year}"
    }

    fun DayOfWeek.displayText(uppercase: Boolean = false, narrow: Boolean = false): String {
        val style = if (narrow) TextStyle.NARROW else TextStyle.SHORT
        return getDisplayName(style, Locale.ENGLISH).let { value ->
            if (uppercase) value.uppercase(Locale.ENGLISH) else value
        }
    }
}