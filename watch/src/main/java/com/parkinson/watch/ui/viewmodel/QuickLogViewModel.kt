package com.parkinson.watch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkinson.watch.domain.usecase.LogMedicationUseCase
import com.parkinson.watch.domain.usecase.LogWellbeingUseCase
import com.parkinson.watch.domain.usecase.StartExerciseUseCase
import com.parkinson.watch.domain.usecase.StartVoiceNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickLogUiState(
    val isLoading: Boolean = false,
    val loggedSuccessfully: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class QuickLogViewModel @Inject constructor(
    private val logWellbeingUseCase: LogWellbeingUseCase,
    private val logMedicationUseCase: LogMedicationUseCase,
    private val startExerciseUseCase: StartExerciseUseCase,
    private val startVoiceNoteUseCase: StartVoiceNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickLogUiState())
    val uiState: StateFlow<QuickLogUiState> = _uiState.asStateFlow()

    fun logWellbeing(wellbeing: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                logWellbeingUseCase(wellbeing)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loggedSuccessfully = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun logMedicationTaken() {
        viewModelScope.launch {
            try {
                logMedicationUseCase()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun startExercise() {
        viewModelScope.launch {
            try {
                startExerciseUseCase()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun startVoiceNote() {
        viewModelScope.launch {
            try {
                startVoiceNoteUseCase()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
