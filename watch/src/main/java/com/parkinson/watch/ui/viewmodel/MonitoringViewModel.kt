package com.parkinson.watch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkinson.watch.domain.usecase.GetRealTimeMetricsUseCase
import com.parkinson.watch.domain.usecase.StartMonitoringUseCase
import com.parkinson.watch.domain.usecase.StopMonitoringUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MonitoringUiState(
    val isMonitoring: Boolean = false,
    val monitoringDuration: String = "00:00:00",
    val samplesCollected: Int = 0,
    val tremorData: List<Float> = emptyList(),
    val currentHeartRate: Int = 0,
    val activityLevel: Float = 0f,
    val fftData: List<Float> = emptyList(),
    val dominantFrequency: Float = 0f,
    val cadence: Int = 0,
    val totalSteps: Int = 0,
    val fogEpisodes: Int = 0,
    val error: String? = null
)

@HiltViewModel
class MonitoringViewModel @Inject constructor(
    private val startMonitoringUseCase: StartMonitoringUseCase,
    private val stopMonitoringUseCase: StopMonitoringUseCase,
    private val getRealTimeMetricsUseCase: GetRealTimeMetricsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonitoringUiState())
    val uiState: StateFlow<MonitoringUiState> = _uiState.asStateFlow()

    private var monitoringJob: Job? = null
    private var timerJob: Job? = null
    private var startTime: Long = 0

    fun toggleMonitoring() {
        if (_uiState.value.isMonitoring) {
            stopMonitoring()
        } else {
            startMonitoring()
        }
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            try {
                startMonitoringUseCase()
                _uiState.update { it.copy(isMonitoring = true) }
                startTime = System.currentTimeMillis()
                startMetricsCollection()
                startTimer()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun stopMonitoring() {
        viewModelScope.launch {
            try {
                stopMonitoringUseCase()
                monitoringJob?.cancel()
                timerJob?.cancel()
                _uiState.update { it.copy(isMonitoring = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun startMetricsCollection() {
        monitoringJob = viewModelScope.launch {
            while (isActive) {
                try {
                    getRealTimeMetricsUseCase().collect { metrics ->
                        _uiState.update { state ->
                            val newTremorData = (state.tremorData + metrics.tremorAmplitude).takeLast(50)
                            state.copy(
                                samplesCollected = state.samplesCollected + 1,
                                tremorData = newTremorData,
                                currentHeartRate = metrics.heartRate,
                                activityLevel = metrics.activityLevel,
                                fftData = metrics.fftSpectrum,
                                dominantFrequency = metrics.dominantFrequency,
                                cadence = metrics.cadence,
                                totalSteps = metrics.totalSteps,
                                fogEpisodes = metrics.fogEpisodes
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Continue monitoring
                }
                delay(1000)
            }
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                val seconds = (elapsed / 1000) % 60
                val minutes = (elapsed / (1000 * 60)) % 60
                val hours = elapsed / (1000 * 60 * 60)
                val duration = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                _uiState.update { it.copy(monitoringDuration = duration) }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        monitoringJob?.cancel()
        timerJob?.cancel()
    }
}
