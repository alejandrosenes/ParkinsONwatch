package com.parkinson.hub.domain.model

import java.time.LocalDateTime

data class DailyData(
    val date: LocalDateTime,
    val idsScore: Int,
    val idsTrend: String,
    val idsTrendPercentage: Float,
    val avgTremor: Float,
    val tremorTrend: String,
    val avgHeartRate: Int,
    val hrTrend: String,
    val sleepHours: Float,
    val sleepEfficiency: Int,
    val sleepTrend: String,
    val lastMedication: String,
    val nextMedication: String,
    val nextMedicationTime: String,
    val alerts: List<AlertData>,
    val quickCorrelation: String,
    val isWatchConnected: Boolean,
    val lastSyncTime: LocalDateTime
)

data class AlertData(
    val id: Long,
    val type: String,
    val message: String,
    val timestamp: LocalDateTime
)

data class TrendData(
    val motorTrend: String,
    val motorTrendDescription: String,
    val motorData: List<Float>,
    val cardioTrend: String,
    val cardioTrendDescription: String,
    val cardioData: List<Float>,
    val sleepTrend: String,
    val sleepTrendDescription: String,
    val sleepData: List<Float>,
    val exerciseTrend: String,
    val exerciseTrendDescription: String,
    val exerciseData: List<Float>
)

data class MedicationLog(
    val id: Long,
    val medicationName: String,
    val dose: String,
    val timestamp: LocalDateTime,
    val withFood: Boolean
)

data class ExerciseLog(
    val id: Long,
    val type: String,
    val durationMinutes: Int,
    val rpe: Int,
    val tremorBefore: Float?,
    val tremorAfter: Float?,
    val timestamp: LocalDateTime
)
