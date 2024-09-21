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
import com.example.studytimertracker.model.UserPreferences
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: TimerRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _userPreferences = MutableLiveData<UserPreferences?>()
    val userPreferences: LiveData<UserPreferences?> get() = _userPreferences

    init {
        viewModelScope.launch {
            // Fetch user preferences from the repository
            _userPreferences.value = repository.getUserPreferencesOnce() ?: UserPreferences()
        }
    }

    fun updateCarryOverPercentage(percentage: Int) {
        viewModelScope.launch {
            val updatedPreferences = userPreferences.value?.copy(carryOverPercentage = percentage)
            if (updatedPreferences != null) {
                _userPreferences.value = updatedPreferences
                repository.updateUserPreferences(updatedPreferences)
            }
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            repository.updateUserPreferences(UserPreferences())
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val timerRepository =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StudyTimerTrackerApp).timerRepository
                SettingsViewModel(
                    repository = timerRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}
