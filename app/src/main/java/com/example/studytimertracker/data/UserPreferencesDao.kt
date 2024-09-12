package com.example.studytimertracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.studytimertracker.model.UserPreferences
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(preferences: UserPreferences)

    @Query("SELECT * FROM user_preferences_table WHERE id = 0")
    fun getUserPreferences(): Flow<UserPreferences>

    @Query("SELECT * FROM user_preferences_table WHERE id = 0")
    suspend fun getUserPreferencesOnce(): UserPreferences?
}