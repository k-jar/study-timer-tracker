package com.example.studytimertracker.viewmodel

import androidx.lifecycle.LiveData
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

class HistoryViewModel (
    private val repository: TimerRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val allHistories: LiveData<List<History>> = repository.getAllHistories().asLiveData()

    fun getHistoryByDate(date: String): LiveData<List<History>> {
        return repository.getHistoryByDate(date).asLiveData()
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