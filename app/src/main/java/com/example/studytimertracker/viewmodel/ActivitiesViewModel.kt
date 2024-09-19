package com.example.studytimertracker.viewmodel

import androidx.lifecycle.LiveData
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
import kotlinx.coroutines.launch

class ActivitiesViewModel(
    private val repository: TimerRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // LiveData for observing activities
    val activities: LiveData<List<Activity>> = repository.getAllActivities().asLiveData()

    // Function to add an activity
    fun addActivity(name: String, multiplier: Float, type: ActivityType) {
        viewModelScope.launch {
            val activity = Activity(name = name, multiplier = multiplier, type = type)
            repository.insertOrUpdateActivity(activity)
        }
    }

    // Function to update an activity
    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            repository.insertOrUpdateActivity(activity)
        }
    }

    // Function to delete an activity
    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
        }
    }

    // https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val timerRepository =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StudyTimerTrackerApp).timerRepository
                ActivitiesViewModel(
                    repository = timerRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}
