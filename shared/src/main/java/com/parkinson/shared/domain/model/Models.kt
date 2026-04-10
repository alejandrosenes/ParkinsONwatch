package com.parkinson.shared.domain.model

import java.time.LocalDateTime

data class TremorReading(
    val timestamp: LocalDateTime,
    val tpiScore: Float,
    val severityLevel: Float,
    val dominantFrequency: Float,
    val bandPower3_7: Float,
    val bandPower8_12: Float,
    val rmsAmplitude: Float,
    val severity: TremorSeverity = TremorSeverity.NONE
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

data class GaitMetrics(
    val timestamp: LocalDateTime,
    val cadence: Int,
    val stepCount: Int,
    val fogEpisodes: Int,
    val asymmetryPercent: Float
)

data class SyncPayload(
    val watchId: String,
    val exportTime: Long,
    val tremorReadings: List<TremorReadingExport>,
    val heartRateReadings: List<HeartRateReadingExport>,
    val sleepSessions: List<SleepSessionExport>,
    val gaitMetrics: List<GaitMetricExport>
)

data class TremorReadingExport(
    val timestamp: Long,
    val tpiScore: Float,
    val dominantFrequency: Float,
    val bandPower3_7: Float,
    val bandPower8_12: Float,
    val rmsAmplitude: Float
)

data class HeartRateReadingExport(
    val timestamp: Long,
    val bpm: Int,
    val rmssd: Float,
    val sdnn: Float,
    val lfHfRatio: Float
)

data class SleepSessionExport(
    val startTimestamp: Long,
    val endTimestamp: Long,
    val efficiencyPercent: Int,
    val rbdProxyScore: Float,
    val remMinutes: Int,
    val n1Minutes: Int,
    val n2Minutes: Int,
    val n3Minutes: Int,
    val awakeMinutes: Int,
    val awakenings: Int,
    val latencyMinutes: Int
)

data class GaitMetricExport(
    val timestamp: Long,
    val cadence: Int,
    val stepCount: Int,
    val fogEpisodes: Int,
    val asymmetryPercent: Float
)

object BleConstants {
    const val SERVICE_UUID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    const val TREMOR_CHAR_UUID = "a1b2c3d4-e5f6-7890-abcd-ef1234567891"
    const val HEART_RATE_CHAR_UUID = "a1b2c3d4-e5f6-7890-abcd-ef1234567892"
    const val COMMAND_CHAR_UUID = "a1b2c3d4-e5f6-7890-abcd-ef1234567893"
}

object SyncConstants {
    const val SYNC_PORT = 7878
    const val SYNC_HOST = "192.168.1.100"
    const val MDNS_SERVICE = "_parkinson._tcp"
}
