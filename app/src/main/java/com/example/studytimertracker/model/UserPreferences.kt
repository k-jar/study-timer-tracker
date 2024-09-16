package com.example.studytimertracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences_table")
data class UserPreferences(
    @PrimaryKey val id: Int = 0,         // Singleton pattern for single entry
    val carryOverPercentage: Int = 50,  // Percentage of rest time to carry over
    val dayStartTime: String = "07:00"     // Start of the day (HH:mm format)
)