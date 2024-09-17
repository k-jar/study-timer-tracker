package com.example.studytimertracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_table")
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val multiplier: Float,  // How fast rest is accumulated/consumed
    val type: ActivityType, // Either WORK or REST
)

enum class ActivityType {
    WORK, REST
}