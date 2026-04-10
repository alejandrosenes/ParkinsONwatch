package com.parkinson.watch.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class TremorReading(
    val timestamp: LocalDateTime,
    val tpiScore: Float,
    val severityLevel: Float,
    val dominantFrequency: Float,
    val bandPower3_7: Float,
    val bandPower8_12: Float,
    val rmsAmplitude: Float,
    val severity: TremorSeverity
)

enum class TremorSeverity {
    NONE, MILD, MODERATE, SEVERE, VERY_SEVERE
}

data class HeartRateReading(
    val timestamp: LocalDateTime,
    val bpm: Int,
    val hrv: HrvMetrics? = null
)

data class HrvMetrics(
    val rmssd: Float,
    val sdnn: Float,
    val lfHfRatio: Float
)

data class SleepSession(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val efficiencyPercent: Int,
    val rbdProxyScore: Float,
    val phases: Map<SleepPhase, Int>,
    val awakenings: Int,
    val latencyMinutes: Int
)

enum class SleepPhase {
    AWAKE, REM, N1, N2, N3
}

data class MedicationSchedule(
    val id: Long,
    val name: String,
    val dose: String,
    val scheduleTimes: List<LocalTime>,
    val lastTaken: LocalDateTime?
) {
    val isTakenToday: Boolean
        get() = lastTaken?.toLocalDate() == LocalDate.now()
}

data class MedicationEntry(
    val id: Long,
    val medicationId: Long,
    val medicationName: String,
    val dose: String,
    val time: LocalDateTime,
    val withFood: Boolean
)

data class MedicationDose(
    val name: String,
    val dose: String,
    val scheduledTime: LocalTime,
    val minutesUntil: Int
)

data class WellbeingEntry(
    val timestamp: LocalDateTime,
    val level: WellbeingLevel
)

enum class WellbeingLevel {
    GOOD, FAIR, POOR
}

data class DailySummary(
    val avgTremor: Float,
    val avgHeartRate: Int,
    val sleepEfficiency: Int,
    val lastMedicationTime: String,
    val hourlySummary: List<HourlySummary>,
    val alerts: List<Alert>
)

data class HourlySummary(
    val hour: String,
    val tremorLevel: Float,
    val heartRate: Int,
    val medicationTaken: Boolean
)

data class Alert(
    val id: Long,
    val type: AlertType,
    val message: String,
    val timestamp: LocalDateTime
)

enum class AlertType {
    MEDICATION, TREMOR, HEART_RATE, SLEEP, FOG, POSTURE
}

data class RealTimeMetrics(
    val tremorAmplitude: Float,
    val heartRate: Int,
    val activityLevel: Float,
    val fftSpectrum: List<Float>,
    val dominantFrequency: Float,
    val cadence: Int,
    val totalSteps: Int,
    val fogEpisodes: Int
)

data class ConnectionStatus(
    val bleConnected: Boolean,
    val wifiConnected: Boolean,
    val lastSync: LocalDateTime?,
    val pendingRecords: Int
)

data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val appVersion: String,
    val hasGyroscope: Boolean,
    val hasHeartRate: Boolean,
    val hasSpO2: Boolean,
    val hasGps: Boolean
)
