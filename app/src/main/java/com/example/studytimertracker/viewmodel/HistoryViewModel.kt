package com.example.studytimertracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.studytimertracker.StudyTimerTrackerApp
import com.example.studytimertracker.data.TimerRepository
import com.example.studytimertracker.model.History
import com.example.studytimertracker.model.SessionActivity

class HistoryViewModel (
    private val repository: TimerRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Holds all history records
    val allHistories: LiveData<List<History>> = repository.getAllHistories().asLiveData()

    // Holds the session activities of a selected history
    private val _selectedHistoryActivities = MutableLiveData<List<SessionActivity>>()
    val selectedHistoryActivities: LiveData<List<SessionActivity>> = _selectedHistoryActivities

    private val _showActivityLogDialog = MutableLiveData(false)
    val showActivityLogDialog: LiveData<Boolean> = _showActivityLogDialog

    fun getHistoryByDate(date: String): LiveData<List<History>> {
        return repository.getHistoryByDate(date).asLiveData()
    }

    // Method to show activity log for a specific history
    fun showActivityLog(sessionActivities: List<SessionActivity>) {
        _selectedHistoryActivities.value = sessionActivities
        _showActivityLogDialog.value = true
    }

    // Method to dismiss activity log dialog
    fun dismissActivityLog() {
        _showActivityLogDialog.value = false
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val timerRepository =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StudyTimerTrackerApp).timerRepository
                HistoryViewModel(
                    repository = timerRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}