package com.example.studytimertracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.studytimertracker.StudyTimerTrackerApp
import com.example.studytimertracker.data.TimerRepository
import com.example.studytimertracker.model.Activity
import com.example.studytimertracker.model.ActivityType
import com.example.studytimertracker.model.History
import com.example.studytimertracker.model.RestStore
import com.example.studytimertracker.model.UserPreferences
import com.example.studytimertracker.utils.DateUtils.getCurrentDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerViewModel(
    private val repository: TimerRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // LiveData for observing data changes in the UI
    val restStore: LiveData<RestStore> = repository.getRestStore().asLiveData()
    val userPreferences: LiveData<UserPreferences> = repository.getUserPreferences().asLiveData()
    val activities: LiveData<List<Activity>> = repository.getAllActivities().asLiveData()

    val workTime: LiveData<Long> = MutableLiveData(0L)
    private val restTime: LiveData<Long> = MutableLiveData(0L)

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


    init {
        // Check and reset the rest store when the ViewModel is initialized
        viewModelScope.launch { checkAndResetRestStoreIfNeeded() }
    }

    // Timer Logic

    fun startSession(activity: Activity, isWork: Boolean) {
        currentSessionStartTime = System.currentTimeMillis()
        currentActivity = activity
        _isWorking.value = isWork
        _isSessionActive.value = true
        _isPaused.value = false

        startWorkTimer()
        startRestTimer()
    }

    fun switchMode() {
        _isWorking.value = !_isWorking.value!!
        if (_isWorking.value == true) {
            // Resume work and accumulate rest
            startWorkTimer()
        } else {
            // Consume rest and stop work
            startRestTimer()
        }
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
                            accumulateRestTime(1000L)
                        } else {
                            consumeRestTime(1000L)
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

    private fun accumulateRestTime(timeIncrement: Long) {
        viewModelScope.launch {
            repository.accumulateRestTime(timeIncrement)
            (restTime as MutableLiveData).postValue(restTime.value!! + timeIncrement)
        }
    }

    private fun consumeRestTime(timeIncrement: Long) {
        viewModelScope.launch {
            repository.consumeRestTime(timeIncrement)
            (restTime as MutableLiveData).postValue(restTime.value!! - timeIncrement)
        }
    }

    fun pauseTimer() {
        _isPaused.value = true
    }

    fun resumeTimer() {
        _isPaused.value = false
    }

    fun endSession() {
        workTimerJob?.cancel()
        restTimerJob?.cancel()
        workTimerJob = null
        restTimerJob = null

        saveSessionData()
        resetSessionState()
    }

    // Session Data Handling

    private fun saveSessionData() {
        val activity = currentActivity ?: return
        val startTime = currentSessionStartTime ?: return
        val endTime = System.currentTimeMillis()
        val sessionDuration = endTime - startTime

        viewModelScope.launch {
            val restStore = repository.getRestStoreOnce()
            val history = History(
                date = getCurrentDate(),
                sessionStartTime = startTime,
                sessionEndTime = endTime,
                totalTimeWorked = if (activity.type == ActivityType.WORK) sessionDuration else 0L,
                restStoreAccumulated = restStore.totalRestTime,
                restStoreUsed = if (activity.type == ActivityType.REST) sessionDuration else 0L
            )
            repository.insertHistory(history)

            updateActivityTimeSpent(activity, sessionDuration)
            handleWorkOrRestTime(activity, sessionDuration)
        }
    }

    private fun updateActivityTimeSpent(activity: Activity, sessionDuration: Long) {
        viewModelScope.launch {
            val updatedActivity = activity.copy(
                totalTimeSpent = activity.totalTimeSpent + sessionDuration
            )
            repository.insertOrUpdateActivity(updatedActivity)
        }
    }

    private fun handleWorkOrRestTime(activity: Activity, sessionDuration: Long) {
        viewModelScope.launch {
            if (activity.type == ActivityType.WORK) {
                accumulateRest(sessionDuration, activity.multiplier)
//                updateDailyWorkTime()
            } else if (activity.type == ActivityType.REST) {
                consumeRest(sessionDuration, activity.multiplier)
            }
        }
    }

    // Rest Store Handling

    private suspend fun accumulateRest(sessionDuration: Long, multiplier: Float) {
        val restEarned = (sessionDuration * multiplier).toLong()

        val restStore = repository.getRestStoreOnce()
        val updatedRestStore = restStore.copy(
            totalRestTime = restStore.totalRestTime + restEarned
        )
        repository.updateRestStore(updatedRestStore)
    }

    private suspend fun consumeRest(sessionDuration: Long, multiplier: Float) {
        val restConsumed = (sessionDuration * multiplier).toLong()

        val restStore = repository.getRestStoreOnce()
        val newTotalRestTime = (restStore.totalRestTime - restConsumed).coerceAtLeast(0L)
        val updatedRestStore = restStore.copy(
            totalRestTime = newTotalRestTime
        )
        repository.updateRestStore(updatedRestStore)
    }

    private suspend fun checkAndResetRestStoreIfNeeded() {
        val currentDate = getCurrentDate()
        val restStore = repository.getRestStoreOnce()

        if (restStore.lastResetDate != currentDate) {
            val userPrefs = repository.getUserPreferencesOnce() ?: UserPreferences()
            val carryOverAmount = (restStore.totalRestTime * userPrefs.carryOverPercentage) / 100

            val updatedRestStore = restStore.copy(
                totalRestTime = carryOverAmount,
                lastResetDate = currentDate
            )
            repository.updateRestStore(updatedRestStore)
        }
    }

    private fun updateDailyWorkTime() {
        // TODO: Implement logic to update daily work time
    }

    // Helper Methods

    private fun resetSessionState() {
        currentActivity = null
        currentSessionStartTime = null
        _isSessionActive.value = false
    }

    // UI Interactions

    fun updateUserPreferences(
        carryOverPercentage: Int? = null,
        dayStartTime: String? = null,
        dayEndTime: String? = null
    ) = viewModelScope.launch {
        val currentPreferences = repository.getUserPreferencesOnce() ?: UserPreferences()

        val updatedPreferences = currentPreferences.copy(
            carryOverPercentage = carryOverPercentage ?: currentPreferences.carryOverPercentage,
            dayStartTime = dayStartTime ?: currentPreferences.dayStartTime,
            dayEndTime = dayEndTime ?: currentPreferences.dayEndTime
        )

        repository.updateUserPreferences(updatedPreferences)
    }

    fun addActivity(name: String, multiplier: Float, type: ActivityType) {
        viewModelScope.launch(Dispatchers.IO) {
            val newActivity = Activity(name = name, multiplier = multiplier, type = type)
            repository.insertOrUpdateActivity(newActivity)
        }
    }

    // https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val timerRepository =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StudyTimerTrackerApp).timerRepository
                TimerViewModel(
                    repository = timerRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}