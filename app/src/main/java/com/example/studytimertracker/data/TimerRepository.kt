package com.example.studytimertracker.data

import com.example.studytimertracker.model.Activity
import com.example.studytimertracker.model.History
import com.example.studytimertracker.model.RestStore
import com.example.studytimertracker.model.UserPreferences
import kotlinx.coroutines.flow.Flow

class TimerRepository(
    private val activityDao: ActivityDao,
    private val restStoreDao: RestStoreDao,
    private val userPreferencesDao: UserPreferencesDao,
    private val historyDao: HistoryDao
) {

    // Activity operations
    fun getAllActivities(): Flow<List<Activity>> = activityDao.getAllActivities()

    suspend fun getActivityById(id: Int): Activity? = activityDao.getActivityById(id)

    suspend fun insertOrUpdateActivity(activity: Activity) {
        activityDao.insertOrUpdate(activity)
    }

    // RestStore operations
    fun getRestStore(): Flow<RestStore> = restStoreDao.getRestStore()

    suspend fun getRestStoreOnce(): RestStore {
        val restStore = restStoreDao.getRestStoreOnce()
        return restStore
    }

    suspend fun updateRestStore(restStore: RestStore) {
        restStoreDao.insertOrUpdate(restStore)
    }

    // Update work time in the rest store
    suspend fun updateWorkTime(amount: Long) {
        val restStore = getRestStoreOnce() // Get the current RestStore
        val updatedRestStore = restStore.copy(totalRestTime = restStore.totalRestTime + amount)
        updateRestStore(updatedRestStore)
    }

    suspend fun accumulateRestTime(amount: Long) {
        val restStore = getRestStoreOnce() // Get the current RestStore
        val updatedRestStore = restStore.copy(totalRestTime = restStore.totalRestTime + amount)
        updateRestStore(updatedRestStore)
    }

    // Consume rest time from the rest store
    suspend fun consumeRestTime(amount: Long) {
        val restStore = getRestStoreOnce() // Get the current RestStore
        val updatedRestStore =
            restStore.copy(totalRestTime = maxOf(0, restStore.totalRestTime - amount))
        updateRestStore(updatedRestStore)
    }

    // Reset rest store daily based on user preferences
    fun resetRestStoreForNewDay() {
        // TODO: Implement logic to reset rest store for new day
    }

    // Insert history record
    suspend fun insertHistory(history: History) {
        historyDao.insertHistory(history)
    }

    // Retrieve all histories
    fun getAllHistories(): Flow<List<History>> = historyDao.getAllHistories()

    // UserPreferences operations
    fun getUserPreferences(): Flow<UserPreferences> = userPreferencesDao.getUserPreferences()

    suspend fun getUserPreferencesOnce(): UserPreferences? =
        userPreferencesDao.getUserPreferencesOnce()

    suspend fun updateUserPreferences(preferences: UserPreferences) {
        userPreferencesDao.insertOrUpdate(preferences)
    }

    fun getHistoryByDate(date: String): Flow<List<History>> = historyDao.getHistoryByDate(date)

}
