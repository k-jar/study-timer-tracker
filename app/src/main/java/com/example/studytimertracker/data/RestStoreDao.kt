package com.example.studytimertracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.studytimertracker.model.RestStore
import kotlinx.coroutines.flow.Flow

@Dao
interface RestStoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(restStore: RestStore)

    @Query("SELECT * FROM rest_store_table WHERE id = 0")
    fun getRestStore(): Flow<RestStore>

    @Query("SELECT * FROM rest_store_table WHERE id = 0")
    suspend fun getRestStoreOnce(): RestStore
}