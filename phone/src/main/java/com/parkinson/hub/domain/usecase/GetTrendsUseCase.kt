package com.parkinson.hub.domain.usecase

import com.parkinson.hub.data.repository.HealthRepository
import com.parkinson.hub.domain.model.TrendData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetTrendsUseCase @Inject constructor(
    private val healthRepository: HealthRepository
) {
    operator fun invoke(): Flow<TrendData> = flowOf(
        TrendData(
            motorTrend = "Mejorando",
            motorTrendDescription = "TPI promedio reducido un 12% esta semana",
            motorData = listOf(4.2f, 3.8f, 4.5f, 3.9f, 3.5f, 3.2f, 3.0f),
            cardioTrend = "Estable",
            cardioTrendDescription = "HRV media de 45ms",
            cardioData = listOf(42f, 48f, 45f, 50f, 43f, 47f, 44f),
            sleepTrend = "Mejorando",
            sleepTrendDescription = "Eficiencia del 85% (+5% vs semana pasada)",
            sleepData = listOf(75f, 80f, 82f, 78f, 85f, 88f, 85f),
            exerciseTrend = "Mejorando",
            exerciseTrendDescription = "45 min promedio diario (+10 min)",
            exerciseData = listOf(30f, 45f, 60f, 40f, 50f, 55f, 35f)
        )
    )
}
