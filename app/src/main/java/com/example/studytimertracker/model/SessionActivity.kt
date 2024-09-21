package com.example.studytimertracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_activities")
data class SessionActivity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val activityId: Int,  // Reference to the Activity
    val startTime: Long,    // Start time of the activity in milliseconds
    val endTime: Long,      // End time of the activity in milliseconds
    val date: String
)