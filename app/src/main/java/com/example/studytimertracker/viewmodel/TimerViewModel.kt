package com.example.studytimertracker.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.studytimertracker.data.TimerRepository
import com.example.studytimertracker.model.Activity
import com.example.studytimertracker.model.History
import com.example.studytimertracker.model.RestStore
import com.example.studytimertracker.model.SessionActivity
import com.example.studytimertracker.model.UserPreferences
import com.example.studytimertracker.utils.DateTimeUtils.getCurrentDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime

class TimerViewModel(
    private val repository: TimerRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private object SessionPreferences {
        val IS_SESSION_ACTIVE = booleanPreferencesKey("is_session_active")
        val IS_WORKING = booleanPreferencesKey("is_working")
        val IS_PAUSED = booleanPreferencesKey("is_paused")
    }

    // LiveData for observing data changes in the UI
    val restStore: LiveData<RestStore> = repository.getRestStore().asLiveData()

    private val userPreferences: LiveData<UserPreferences> =
        repository.getUserPreferences().asLiveData()

    val activities: LiveData<List<Activity>> = repository.getAllActivities().asLiveData()

    val workTime: LiveData<Long> = MutableLiveData(0L)
    val restTime: LiveData<Long> = MutableLiveData(0L)

    // LiveData or StateFlow for session-related variables
    private val _isWorking = MutableLiveData(false)
    val isWorking: LiveData<Boolean> get() = _isWorking

    private val _isSessionActive = MutableLiveData(false)
    val isSessionActive: LiveData<Boolean> get() = _isSessionActive

    private val _isPaused = MutableLiveData(false)
    val isPaused: LiveData<Boolean> get() = _isPaused

    private var workTimerJob: Job? = null
    private var restTimerJob: Job? = null

    // Session-related variables
    private var currentSessionStartTime: Long? = null
    private var currentActivity: Activity? = null
    private var activityStartTime: Long? = null

    private val _sessionActivities = MutableLiveData<List<SessionActivity>>(mutableListOf())
    val sessionActivities: LiveData<List<SessionActivity>> = _sessionActivities

    // Activity variables
    private var currentWorkActivity: Activity? = null
    private var currentRestActivity: Activity? = null


    init {
        viewModelScope.launch {
            // Collect data store in a separate coroutine
            launch {
                dataStore.data.collect { preferences ->
                    _isWorking.value = preferences[SessionPreferences.IS_WORKING] ?: false
                    _isSessionActive.value =
                        preferences[SessionPreferences.IS_SESSION_ACTIVE] ?: false
                    _isPaused.value = preferences[SessionPreferences.IS_PAUSED] ?: false
                }
            }

            val restStore = repository.getRestStoreOnce()
            (workTime as MutableLiveData).postValue(restStore.totalTimeWorked) // Restore total time worked
            (restTime as MutableLiveData).postValue(restStore.restTimeLeft)

            loadSessionActivities()

            // Collect the user preferences flow
            repository.getUserPreferences().collect { userPrefs ->
                scheduleDayStartCheck(userPrefs)
            }
        }
    }

    private fun saveIsWorkingState(isWorking: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[SessionPreferences.IS_WORKING] = isWorking
            }
        }
    }

    private fun saveIsPausedState(isPaused: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[SessionPreferences.IS_PAUSED] = isPaused
            }
        }
    }

    private fun saveIsSessionActiveState(isActive: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[SessionPreferences.IS_SESSION_ACTIVE] = isActive
            }
        }
    }

    private fun scheduleDayStartCheck(userPrefs: UserPreferences) {
        println("In scheduleDayStartCheck")
        viewModelScope.launch {
            while (true) {
                delay(1000L) // Check every 5 minutes

                val currentTime = LocalTime.now() // Get the current time
                val dayStartTime = LocalTime.parse(userPrefs.dayStartTime)
                val lastResetDate = repository.getLastResetDate()

                // Check if it's a new day and after the specified start time
                if (currentTime.isAfter(dayStartTime) && lastResetDate != getCurrentDate()) {
                    endSession()
                }
            }
        }
    }

    private fun loadSessionActivities() {
        viewModelScope.launch {
            val currentDate = getCurrentDate()
            val savedActivities = repository.getSessionActivitiesForDay(currentDate)
            _sessionActivities.postValue(savedActivities)
        }
    }

    // Timer Logic

    fun startSession(workActivity: Activity, restActivity: Activity) {
        currentSessionStartTime = System.currentTimeMillis()
        currentWorkActivity = workActivity
        currentRestActivity = restActivity

        _isWorking.value = true
        _isSessionActive.value = true
        _isPaused.value = false
        saveIsWorkingState(true)
        saveIsSessionActiveState(true)
        saveIsPausedState(false)

        activityStartTime = System.currentTimeMillis()

        startWorkTimer()
        startRestTimer()
    }

    fun switchMode(workActivity: Activity?, restActivity: Activity?, switch: Boolean = true) {
        if (workActivity != null) currentWorkActivity = workActivity
        if (restActivity != null) currentRestActivity = restActivity

        recordAndAddActivity()
        if (switch) {
            _isWorking.value = !_isWorking.value!!
            saveIsWorkingState(_isWorking.value ?: false)
        }
        if (_isWorking.value == true) {
            // Resume work and accumulate rest
            startWorkTimer()
        } else {
            // Consume rest and stop work
            startRestTimer()
        }
    }

    private fun stopTimers() {
        workTimerJob?.cancel()
        restTimerJob?.cancel()
        workTimerJob = null
        restTimerJob = null
    }

    private fun startWorkTimer() {
        if (workTimerJob == null) {
            workTimerJob = viewModelScope.launch {
                while (true) {
                    delay(1000L)
                    if (_isPaused.value == false && _isWorking.value == true) {
                        updateWorkTime(1000L)
                    }
                }
            }
        }
    }

    private fun startRestTimer() {
        if (restTimerJob == null) {
            restTimerJob = viewModelScope.launch {
                while (true) {
                    delay(1000L)
                    if (_isPaused.value == false) {
                        if (_isWorking.value == true) {
                            // Work mode, accumulate rest using the work activity multiplier
                            currentWorkActivity?.let {
                                accumulateRestTime(1000L, it.multiplier)
                            }
                        } else {
                            // Rest mode, consume rest using the rest activity multiplier
                            currentRestActivity?.let {
                                consumeRestTime(1000L, it.multiplier)
                            }
                        }
                    }
                }
            }
        }
    }

    // Time Updates

    private fun updateWorkTime(timeIncrement: Long) {
        viewModelScope.launch {
            repository.updateWorkTime(timeIncrement)
            (workTime as MutableLiveData).postValue(workTime.value!! + timeIncrement)
        }
    }

    private fun accumulateRestTime(timeIncrement: Long, multiplier: Float) {
        viewModelScope.launch {
            val restEarned = (timeIncrement * multiplier).toLong()
            repository.accumulateRestTime(restEarned)
            (restTime as MutableLiveData).postValue(restTime.value!! + restEarned)
        }
    }

    private fun consumeRestTime(timeIncrement: Long, multiplier: Float) {
        viewModelScope.launch {
            val restConsumed = (timeIncrement * multiplier).toLong()
            repository.consumeRestTime(restConsumed)
            (restTime as MutableLiveData).postValue(restTime.value!! - restConsumed)
        }
    }

    fun pauseTimer() {
        if (_isPaused.value == true) return

        // Record the current activity (work/rest) before pausing
        recordAndAddActivity()

        // Set session to paused, but don't log it yet
        _isPaused.value = true

        saveIsPausedState(true)

        // Set pause start time
        activityStartTime = System.currentTimeMillis()
    }

    fun resumeTimer() {
        if (_isPaused.value == false) return // Already running

        // Log the pause activity before resuming
        val endTime = System.currentTimeMillis()
        addSessionActivity(activityId = -1, startTime = activityStartTime, endTime = endTime)

        // Set session back to active
        _isPaused.value = false

        saveIsPausedState(false)

        // Set new start time for tracking resumed activities
        activityStartTime = System.currentTimeMillis()
    }

    private suspend fun endSession() {
        val userPrefs = repository.getUserPreferencesOnce()
        stopTimers()

        // If the session ends while paused, log the pause activity
        if (_isPaused.value == true) {
            val endTime = System.currentTimeMillis()
            addSessionActivity(activityId = -1, startTime = activityStartTime, endTime = endTime)
        }

        recordAndAddActivity()
        saveSessionData()

        viewModelScope.launch {
            repository.resetRestStore(userPrefs.carryOverPercentage)
            repository.updateWorkTime(0L)
        }

        resetSessionState()
    }

    // Session Data Handling

    private fun saveSessionData() {
        val startTime = currentSessionStartTime ?: return

        viewModelScope.launch {
            val restStore = repository.getRestStoreOnce()
            val history = History(
                date = getCurrentDate(),
                dayStartTime = startTime,
                totalTimeWorked = workTime.value ?: 0L,
                restStoreAccumulated = restStore.restStoreAccumulated,
                restStoreUsed = restStore.restStoreUsed,
                sessionActivities = _sessionActivities.value ?: emptyList()
            )
            repository.insertHistory(history)
        }
    }

    private fun recordAndAddActivity() {
        val startTime = activityStartTime ?: return
        val endTime = System.currentTimeMillis()

        // Determine the current activity (work or rest)
        val currentActivity = if (_isPaused.value == false) {
            if (_isWorking.value == true) currentWorkActivity else currentRestActivity
        } else {
            null
        }

        // Add the current activity (if there is one) to the session log
        currentActivity?.let { activity ->
            addSessionActivity(activity.id, startTime, endTime)
        }

        // Update the start time for the next activity or pause
        activityStartTime = System.currentTimeMillis()
    }

    private fun addSessionActivity(
        activityId: Int,
        startTime: Long? = activityStartTime,
        endTime: Long = System.currentTimeMillis()
    ) {
        // Ensure startTime is not null before proceeding
        startTime?.let {
            val newActivity =
                SessionActivity(activityId = activityId, startTime = it, endTime = endTime, date = getCurrentDate())

            viewModelScope.launch {
                repository.insertSessionActivity(newActivity)
            }

            // Append new activity to the sessionActivities list
            val updatedActivities = _sessionActivities.value.orEmpty().toMutableList()
            updatedActivities.add(newActivity)
            _sessionActivities.value = updatedActivities
        }
    }

    private fun resetSessionState() {
        currentActivity = null
        currentSessionStartTime = null
        activityStartTime = null
        (workTime as MutableLiveData).postValue(0L)

        _sessionActivities.value = emptyList()

        _isSessionActive.value = false
        _isWorking.value = false
        _isPaused.value = false
        saveIsSessionActiveState(false)
        saveIsWorkingState(false)
        saveIsPausedState(false)
    }

    // https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
    companion object {
        fun provideFactory(
            repository: TimerRepository,
            dataStore: DataStore<Preferences>
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TimerViewModel(repository, dataStore)
            }
        }
    }
}