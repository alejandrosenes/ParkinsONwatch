package com.parkinson.watch.data.repository

import com.parkinson.shared.domain.model.HeartRateReading
import com.parkinson.shared.domain.model.TremorReading
import com.parkinson.watch.data.local.SensorDao
import com.parkinson.watch.data.local.entity.TremorEntity
import com.parkinson.watch.data.local.entity.HeartRateEntity
import com.parkinson.watch.domain.model.RealTimeMetrics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepository @Inject constructor(
    private val sensorDao: SensorDao
) {
    fun getLatestTremorReading(): Flow<TremorReading?> = sensorDao.getLatestTremor().map { entity ->
        entity?.toDomainModel()
    }

    fun getLatestHeartRateReading(): Flow<HeartRateReading?> = sensorDao.getLatestHeartRate().map { entity ->
        entity?.toDomainModel()
    }

    suspend fun saveTremorReading(reading: TremorReading) {
        sensorDao.insertTremor(reading.toEntity())
    }

    suspend fun saveHeartRateReading(reading: HeartRateReading) {
        sensorDao.insertHeartRate(reading.toEntity())
    }

    fun getRealTimeMetrics(): Flow<RealTimeMetrics> = sensorDao.getLatestMetrics().map { metrics ->
        RealTimeMetrics(
            tremorAmplitude = metrics?.tremorAmplitude ?: 0f,
            heartRate = metrics?.heartRate ?: 0,
            activityLevel = metrics?.activityLevel ?: 0f,
            fftSpectrum = metrics?.fftSpectrum ?: emptyList(),
            dominantFrequency = metrics?.dominantFrequency ?: 0f,
            cadence = metrics?.cadence ?: 0,
            totalSteps = metrics?.totalSteps ?: 0,
            fogEpisodes = metrics?.fogEpisodes ?: 0
        )
    }

    suspend fun startHighFrequencyMonitoring() {
        // Implementation handled by SensorService
    }

    suspend fun stopHighFrequencyMonitoring() {
        // Implementation handled by SensorService
    }

    suspend fun startExerciseSession() {
        // Implementation handled by SensorService
    }

    suspend fun startVoiceRecording() {
        // Implementation handled by SensorService
    }

    private fun TremorEntity.toDomainModel(): TremorReading {
        val severityLvl = (tpiScore / 10f).toInt().coerceIn(0, 4)
        return TremorReading(
            timestamp = LocalDateTime.now(),
            tpiScore = tpiScore,
            severityLevel = tpiScore / 10f,
            dominantFrequency = dominantFrequency,
            bandPower3_7 = bandPower3_7,
            bandPower8_12 = bandPower8_12,
            rmsAmplitude = rmsAmplitude,
            severity = com.parkinson.shared.domain.model.TremorSeverity.entries[severityLvl]
        )
    }

    private fun TremorReading.toEntity() = TremorEntity(
        timestamp = System.currentTimeMillis(),
        tpiScore = tpiScore,
        dominantFrequency = dominantFrequency,
        bandPower3_7 = bandPower3_7,
        bandPower8_12 = bandPower8_12,
        rmsAmplitude = rmsAmplitude,
        synced = false
    )

    private fun HeartRateEntity.toDomainModel() = HeartRateReading(
        timestamp = LocalDateTime.now(),
        bpm = bpm,
        hrv = null
    )

    private fun HeartRateReading.toEntity() = HeartRateEntity(
        timestamp = System.currentTimeMillis(),
        bpm = bpm,
        rmssd = hrv?.rmssd ?: 0f,
        sdnn = hrv?.sdnn ?: 0f,
        lfHfRatio = hrv?.lfHfRatio ?: 0f,
        synced = false
    )
}
