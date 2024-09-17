package com.example.studytimertracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,               // Date of the session (you can store as string or timestamp)
    val sessionStartTime: Long,      // Session start time in milliseconds
    val sessionEndTime: Long,        // Session end time in milliseconds
    val totalTimeWorked: Long,       // Total time worked in this session (milliseconds)
    val restStoreAccumulated: Long,  // Rest store accumulated during the session
    val restStoreUsed: Long,          // Rest store used during rest activities
    val sessionActivities: List<SessionActivity> // List of activities and their time spent
)

data class SessionActivity(
    val activityId: Int,  // Reference to the Activity
    val startTime: Long,    // Start time of the activity in milliseconds
    val endTime: Long,      // End time of the activity in milliseconds
)