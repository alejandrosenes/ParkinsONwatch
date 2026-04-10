package com.parkinson.hub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkinson.hub.domain.usecase.GetDailyDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class Alert(
    val type: String,
    val message: String,
    val timestamp: String
)

data class TodayUiState(
    val idsScore: Int = 0,
    val idsTrend: String = "stable",
    val idsTrendPercentage: Float = 0f,
    val avgTremor: Float = 0f,
    val tremorTrend: String = "stable",
    val avgHeartRate: Int = 0,
    val hrTrend: String = "stable",
    val sleepHours: Float = 0f,
    val sleepEfficiency: Int = 0,
    val sleepTrend: String = "stable",
    val lastMedication: String = "--:--",
    val nextMedication: String = "",
    val nextMedicationTime: String = "",
    val alerts: List<Alert> = emptyList(),
    val quickCorrelation: String = "",
    val isWatchConnected: Boolean = false,
    val lastSyncTime: String = "--:--",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getDailyDataUseCase: GetDailyDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadTodayData()
    }

    private fun loadTodayData() {
        viewModelScope.launch {
            try {
                getDailyDataUseCase().collect { data ->
                    _uiState.update {
                        it.copy(
                            idsScore = data.idsScore,
                            idsTrend = data.idsTrend,
                            idsTrendPercentage = data.idsTrendPercentage,
                            avgTremor = data.avgTremor,
                            tremorTrend = data.tremorTrend,
                            avgHeartRate = data.avgHeartRate,
                            hrTrend = data.hrTrend,
                            sleepHours = data.sleepHours,
                            sleepEfficiency = data.sleepEfficiency,
                            sleepTrend = data.sleepTrend,
                            lastMedication = data.lastMedication,
                            nextMedication = data.nextMedication,
                            nextMedicationTime = data.nextMedicationTime,
                            alerts = data.alerts.map { alert ->
                                Alert(
                                    type = alert.type,
                                    message = alert.message,
                                    timestamp = alert.timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))
                                )
                            },
                            quickCorrelation = data.quickCorrelation,
                            isWatchConnected = data.isWatchConnected,
                            lastSyncTime = data.lastSyncTime.format(DateTimeFormatter.ofPattern("HH:mm")),
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
