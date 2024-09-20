package com.example.studytimertracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.studytimertracker.model.SessionActivity

@Dao
interface SessionActivityDao {
    @Query("SELECT * FROM session_activities")
    suspend fun getAllSessionActivities(): List<SessionActivity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionActivity(activity: SessionActivity)

    @Delete
    suspend fun deleteSessionActivity(activity: SessionActivity)

    @Query("DELETE FROM session_activities")
    suspend fun clearAllSessionActivities()
}