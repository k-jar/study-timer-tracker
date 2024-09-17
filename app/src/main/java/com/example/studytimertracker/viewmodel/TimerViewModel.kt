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
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // LiveData for observing data changes in the UI
    val restStore: LiveData<RestStore> = repository.getRestStore().asLiveData()
    private val userPreferences: LiveData<UserPreferences> =
        repository.getUserPreferences().asLiveData()
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
    private var activityStartTime: Long? = null
    private val sessionActivities = mutableListOf<SessionActivity>()

    // Activity variables
    private var currentWorkActivity: Activity? = null
    private var currentRestActivity: Activity? = null


    init {
        // Check and reset the rest store when the ViewModel is initialized
        viewModelScope.launch { checkAndResetRestStoreIfNeeded() }
        // Schedule automatic session end check
        scheduleDayStartCheck()
    }

    private fun scheduleDayStartCheck() {
        viewModelScope.launch {
            while (true) {
                delay(1000L) // Check every second
                checkDayStart()
            }
        }
    }

    private fun checkDayStart() {
        val userPrefs = userPreferences.value ?: return
        val dayStartTime = userPrefs.dayStartTime

        val currentTime = LocalTime.now()
        val startTime = LocalTime.parse(dayStartTime)

        if (currentTime.isAfter(startTime) && _isSessionActive.value == true) {
            endSession()
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

        activityStartTime = System.currentTimeMillis()

        startWorkTimer()
        startRestTimer()
    }

    fun switchMode(workActivity: Activity?, restActivity: Activity?, switch: Boolean = true) {
        if (workActivity != null) currentWorkActivity = workActivity
        if (restActivity != null) currentRestActivity = restActivity

        recordCurrentActivityTime()
        if (switch) {
            _isWorking.value = !_isWorking.value!!
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
        _isPaused.value = true
    }

    fun resumeTimer() {
        _isPaused.value = false
    }

    private fun endSession() {
        stopTimers()
        recordCurrentActivityTime()
        saveSessionData()
        resetSessionState()
    }

    // Session Data Handling

    private fun saveSessionData() {
        val startTime = currentSessionStartTime ?: return
        val endTime = System.currentTimeMillis()

        viewModelScope.launch {
            val restStore = repository.getRestStoreOnce()
            val history = History(
                date = getCurrentDate(),
                sessionStartTime = startTime,
                sessionEndTime = endTime,
                totalTimeWorked = workTime.value ?: 0L,
                restStoreAccumulated = restStore.restStoreAccumulated,
                restStoreUsed = restStore.restStoreUsed,
                sessionActivities = sessionActivities
            )
            repository.insertHistory(history)
            sessionActivities.clear()

//            handleWorkOrRestTime(activity, sessionDuration)
        }
    }

    private fun recordCurrentActivityTime() {
        val startTime = activityStartTime ?: return
        val endTime = System.currentTimeMillis()
        currentActivity?.let { activity ->
            sessionActivities.add(SessionActivity(activity.id, startTime, endTime))
        }
        // Update the start time for the next activity
        activityStartTime = System.currentTimeMillis()
    }
//    private fun handleWorkOrRestTime(activity: Activity, sessionDuration: Long) {
//        viewModelScope.launch {
//            if (activity.type == ActivityType.WORK) {
//                accumulateRest(sessionDuration, activity.multiplier)
////                updateDailyWorkTime()
//            } else if (activity.type == ActivityType.REST) {
//                consumeRest(sessionDuration, activity.multiplier)
//            }
//        }
//    }

    // Rest Store Handling

//    private suspend fun accumulateRest(sessionDuration: Long, multiplier: Float) {
//        val restEarned = (sessionDuration * multiplier).toLong()
//
//        val restStore = repository.getRestStoreOnce()
//        val updatedRestStore = restStore.copy(
//            restTimeLeft = restStore.restTimeLeft + restEarned
//        )
//        repository.updateRestStore(updatedRestStore)
//    }
//
//    private suspend fun consumeRest(sessionDuration: Long, multiplier: Float) {
//        val restConsumed = (sessionDuration * multiplier).toLong()
//
//        val restStore = repository.getRestStoreOnce()
//        val newTotalRestTime = (restStore.restTimeLeft - restConsumed).coerceAtLeast(0L)
//        val updatedRestStore = restStore.copy(
//            restTimeLeft = newTotalRestTime
//        )
//        repository.updateRestStore(updatedRestStore)
//    }

    private suspend fun checkAndResetRestStoreIfNeeded() {
        repository.resetRestStoreForNewDay()
    }

    // Helper Methods

    private fun resetSessionState() {
        currentActivity = null
        currentSessionStartTime = null
        activityStartTime = null
        _isSessionActive.value = false
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