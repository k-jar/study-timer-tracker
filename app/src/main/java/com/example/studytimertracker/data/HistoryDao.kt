package com.example.studytimertracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.studytimertracker.model.History
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: History)

    @Query("SELECT * FROM history_table ORDER BY date DESC")
    fun getAllHistories(): Flow<List<History>>

    @Query("SELECT * FROM history_table WHERE date = :date")
    fun getHistoryByDate(date: String): Flow<List<History>>
}