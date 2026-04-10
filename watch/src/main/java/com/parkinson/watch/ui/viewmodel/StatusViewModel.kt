package com.parkinson.watch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkinson.shared.domain.model.TremorReading
import com.parkinson.shared.domain.model.HeartRateReading
import com.parkinson.shared.domain.model.SleepSession
import com.parkinson.watch.domain.usecase.GetLatestTremorUseCase
import com.parkinson.watch.domain.usecase.GetLatestHeartRateUseCase
import com.parkinson.watch.domain.usecase.GetTodaySleepUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StatusUiState(
    val currentTime: String = "",
    val currentDate: String = "",
    val tpiScore: Float = 0f,
    val tremorSeverity: Float = 0f,
    val heartRate: Int = 0,
    val heartRateStatus: String = "normal",
    val sleepScore: Int = 0,
    val sleepStatus: String = "good",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val getLatestTremorUseCase: GetLatestTremorUseCase,
    private val getLatestHeartRateUseCase: GetLatestHeartRateUseCase,
    private val getTodaySleepUseCase: GetTodaySleepUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatusUiState())
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM")

    init {
        loadData()
        startTimeUpdates()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                getLatestTremorUseCase().collect { tremor ->
                    tremor?.let {
                        _uiState.update { state ->
                            state.copy(
                                tpiScore = it.tpiScore,
                                tremorSeverity = it.severityLevel,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }

        viewModelScope.launch {
            try {
                getLatestHeartRateUseCase().collect { hr ->
                    hr?.let {
                        _uiState.update { state ->
                            state.copy(
                                heartRate = it.bpm,
                                heartRateStatus = getHeartRateStatus(it.bpm)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle silently
            }
        }

        viewModelScope.launch {
            try {
                getTodaySleepUseCase().collect { sleep ->
                    sleep?.let {
                        _uiState.update { state ->
                            state.copy(
                                sleepScore = it.efficiencyPercent,
                                sleepStatus = getSleepStatus(it.efficiencyPercent)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }

    private fun startTimeUpdates() {
        viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                _uiState.update { state ->
                    state.copy(
                        currentTime = now.format(timeFormatter),
                        currentDate = now.format(dateFormatter)
                    )
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun getHeartRateStatus(bpm: Int): String {
        return when {
            bpm < 60 -> "low"
            bpm > 100 -> "high"
            else -> "normal"
        }
    }

    private fun getSleepStatus(efficiency: Int): String {
        return when {
            efficiency >= 85 -> "good"
            efficiency >= 70 -> "warning"
            else -> "alert"
        }
    }
}
