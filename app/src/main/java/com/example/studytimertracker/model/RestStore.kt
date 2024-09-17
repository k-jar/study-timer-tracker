package com.example.studytimertracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.studytimertracker.utils.DateTimeUtils.getCurrentDate

@Entity(tableName = "rest_store_table")
data class RestStore(
    @PrimaryKey val id: Int = 0,       // Singleton pattern, only one rest store record
    val restStoreAccumulated: Long = 0L, // Total rest gained
    val restStoreUsed: Long = 0L,        // Total rest used
    val restTimeLeft: Long = restStoreAccumulated - restStoreUsed, // Remaining rest time
    val lastResetDate: String = getCurrentDate()     // Date of the last reset
)