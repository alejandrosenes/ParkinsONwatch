package com.parkinson.watch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkinson.watch.domain.model.Alert
import com.parkinson.watch.domain.model.HourlySummary
import com.parkinson.watch.domain.usecase.GetTodaySummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class DashboardUiState(
    val avgTremorToday: Float = 0f,
    val avgHeartRateToday: Int = 0,
    val sleepQualityScore: Int = 0,
    val lastMedicationTime: String = "--:--",
    val hourlySummary: List<HourlySummary> = emptyList(),
    val alerts: List<Alert> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getTodaySummaryUseCase: GetTodaySummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            try {
                getTodaySummaryUseCase().collect { summary ->
                    _uiState.update {
                        it.copy(
                            avgTremorToday = summary.avgTremor,
                            avgHeartRateToday = summary.avgHeartRate,
                            sleepQualityScore = summary.sleepEfficiency,
                            lastMedicationTime = summary.lastMedicationTime,
                            hourlySummary = summary.hourlySummary,
                            alerts = summary.alerts,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
