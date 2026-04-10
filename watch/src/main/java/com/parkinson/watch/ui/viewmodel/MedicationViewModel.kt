package com.parkinson.watch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkinson.watch.domain.model.MedicationDose
import com.parkinson.watch.domain.model.MedicationEntry
import com.parkinson.watch.domain.model.MedicationSchedule
import com.parkinson.watch.domain.usecase.GetMedicationsUseCase
import com.parkinson.watch.domain.usecase.GetTodayMedicationLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class MedicationUiState(
    val medications: List<MedicationSchedule> = emptyList(),
    val todayLog: List<MedicationEntry> = emptyList(),
    val nextDose: MedicationDose? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val getTodayMedicationLogUseCase: GetTodayMedicationLogUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()

    init {
        loadMedications()
    }

    private fun loadMedications() {
        viewModelScope.launch {
            try {
                getMedicationsUseCase().collect { medications ->
                    _uiState.update { it.copy(medications = medications) }
                    calculateNextDose(medications)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }

        viewModelScope.launch {
            try {
                getTodayMedicationLogUseCase().collect { log ->
                    _uiState.update { it.copy(todayLog = log) }
                }
            } catch (e: Exception) {
                // Handle silently
            }
        }

        _uiState.update { it.copy(isLoading = false) }
    }

    private fun calculateNextDose(medications: List<MedicationSchedule>) {
        val now = LocalDateTime.now()
        val currentMinutes = now.hour * 60 + now.minute

        val next = medications
            .flatMap { schedule ->
                schedule.scheduleTimes.map { time ->
                    MedicationDose(
                        name = schedule.name,
                        dose = schedule.dose,
                        scheduledTime = time,
                        minutesUntil = time.hour * 60 + time.minute - currentMinutes
                    )
                }
            }
            .filter { it.minutesUntil > 0 }
            .minByOrNull { it.minutesUntil }

        _uiState.update { it.copy(nextDose = next) }
    }
}
