package com.parkinson.hub.data.repository

import com.parkinson.hub.domain.model.AlertData
import com.parkinson.hub.domain.model.DailyData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepository @Inject constructor() {

    fun getTodayData(): Flow<DailyData> = flowOf(
        DailyData(
            date = LocalDateTime.now(),
            idsScore = 72,
            idsTrend = "up",
            idsTrendPercentage = 8.5f,
            avgTremor = 3.2f,
            tremorTrend = "down",
            avgHeartRate = 68,
            hrTrend = "stable",
            sleepHours = 7.5f,
            sleepEfficiency = 82,
            sleepTrend = "up",
            lastMedication = "08:30",
            nextMedication = "Levodopa 100mg",
            nextMedicationTime = "12:30",
            alerts = listOf(
                AlertData(
                    id = 1,
                    type = "fog",
                    message = "Detectado episodio de congelación de marcha a las 10:45",
                    timestamp = LocalDateTime.now().withHour(10).withMinute(45)
                )
            ),
            quickCorrelation = "Ayer dormiste 7.5h y entrenaste 45 min → Hoy tu temblor es 15% menor que tu media",
            isWatchConnected = true,
            lastSyncTime = LocalDateTime.now().minusMinutes(5)
        )
    )
}
