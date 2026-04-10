package com.parkinson.hub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkinson.hub.domain.usecase.GetTrendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrendsUiState(
    val motorTrend: String = "Estable",
    val motorTrendDescription: String = "Sin cambios significativos",
    val motorData: List<Float> = emptyList(),
    val cardioTrend: String = "Mejorando",
    val cardioTrendDescription: String = "HRV mejorado un 8% esta semana",
    val cardioData: List<Float> = emptyList(),
    val sleepTrend: String = "Estable",
    val sleepTrendDescription: String = "Eficiencia del 82%",
    val sleepData: List<Float> = emptyList(),
    val exerciseTrend: String = "Mejorando",
    val exerciseTrendDescription: String = "+15 min de ejercicio diario vs semana pasada",
    val exerciseData: List<Float> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val getTrendsUseCase: GetTrendsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrendsUiState())
    val uiState: StateFlow<TrendsUiState> = _uiState.asStateFlow()

    init {
        loadTrends()
    }

    private fun loadTrends() {
        viewModelScope.launch {
            try {
                getTrendsUseCase().collect { trends ->
                    _uiState.update {
                        it.copy(
                            motorTrend = trends.motorTrend,
                            motorTrendDescription = trends.motorTrendDescription,
                            motorData = trends.motorData,
                            cardioTrend = trends.cardioTrend,
                            cardioTrendDescription = trends.cardioTrendDescription,
                            cardioData = trends.cardioData,
                            sleepTrend = trends.sleepTrend,
                            sleepTrendDescription = trends.sleepTrendDescription,
                            sleepData = trends.sleepData,
                            exerciseTrend = trends.exerciseTrend,
                            exerciseTrendDescription = trends.exerciseTrendDescription,
                            exerciseData = trends.exerciseData,
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
