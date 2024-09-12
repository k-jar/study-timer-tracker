package com.example.studytimertracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.studytimertracker.utils.DateUtils.getCurrentDate

@Entity(tableName = "rest_store_table")
data class RestStore(
    @PrimaryKey val id: Int = 0,       // Singleton pattern, only one rest store record
    val totalRestTime: Long = 0L,       // Total accumulated rest time in milliseconds
    val lastResetDate: String = getCurrentDate()     // Date of the last reset
)