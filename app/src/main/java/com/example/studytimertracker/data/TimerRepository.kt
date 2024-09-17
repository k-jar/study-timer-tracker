package com.example.studytimertracker.data

import com.example.studytimertracker.model.Activity
import com.example.studytimertracker.model.History
import com.example.studytimertracker.model.RestStore
import com.example.studytimertracker.model.UserPreferences
import com.example.studytimertracker.utils.DateTimeUtils.getCurrentDate
import com.example.studytimertracker.utils.DateTimeUtils.getCurrentTime
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
        return restStore ?: RestStore() // Return default RestStore if null
    }

    private suspend fun updateRestStore(restStore: RestStore) {
        restStoreDao.insertOrUpdate(restStore)
    }

    // Update work time in the rest store
    suspend fun updateWorkTime(amount: Long) {
        val restStore = getRestStoreOnce() // Get the current RestStore
        val updatedRestStore = restStore.copy(restTimeLeft = restStore.restTimeLeft + amount)
        updateRestStore(updatedRestStore)
    }

    suspend fun accumulateRestTime(amount: Long) {
        val restStore = getRestStoreOnce() // Get the current RestStore
        val updatedRestStore = restStore.copy(restTimeLeft = restStore.restTimeLeft + amount)
        updateRestStore(updatedRestStore)
    }

    // Consume rest time from the rest store
    suspend fun consumeRestTime(amount: Long) {
        val restStore = getRestStoreOnce() // Get the current RestStore
        val updatedRestStore =
            restStore.copy(restTimeLeft = maxOf(0, restStore.restTimeLeft - amount))
        updateRestStore(updatedRestStore)
    }

    // Reset rest store daily based on user preferences
    suspend fun resetRestStoreForNewDay() {
        val restStore = getRestStoreOnce()
        val userPreferences = getUserPreferencesOnce()
        val lastResetDate = restStore.lastResetDate
        val currentDate = getCurrentDate()

        // Check if the last reset was not today
        if (lastResetDate != currentDate) {
            val currentTime = getCurrentTime() // Get the current time as HH:mm
            val dayStartTime = userPreferences.dayStartTime

            // Check if the current time is past the user's start of the day time
            if (currentTime >= dayStartTime) {
                // Calculate carryover
                val carryOverPercentage = userPreferences.carryOverPercentage
                val carryOverRest = (restStore.restTimeLeft * carryOverPercentage / 100)

                // Reset the rest store with carryover
                val updatedRestStore = restStore.copy(
                    restTimeLeft = carryOverRest,
                    lastResetDate = currentDate
                )
                updateRestStore(updatedRestStore)
            }
        }
    }

    // Insert history record
    suspend fun insertHistory(history: History) {
        historyDao.insertHistory(history)
    }

    // Retrieve all histories
    fun getAllHistories(): Flow<List<History>> = historyDao.getAllHistories()

    // UserPreferences operations
    fun getUserPreferences(): Flow<UserPreferences> = userPreferencesDao.getUserPreferences()

    suspend fun getUserPreferencesOnce(): UserPreferences {
        val userPreferences = userPreferencesDao.getUserPreferencesOnce()
        return userPreferences ?: UserPreferences() // Return default UserPreferences if null
    }

    suspend fun updateUserPreferences(preferences: UserPreferences) {
        userPreferencesDao.insertOrUpdate(preferences)
    }

    fun getHistoryByDate(date: String): Flow<List<History>> = historyDao.getHistoryByDate(date)

}
