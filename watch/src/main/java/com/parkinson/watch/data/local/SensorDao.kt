package com.parkinson.watch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parkinson.watch.data.local.entity.GaitMetricEntity
import com.parkinson.watch.data.local.entity.HeartRateEntity
import com.parkinson.watch.data.local.entity.TremorEntity
import kotlinx.coroutines.flow.Flow

data class RealTimeMetricsData(
    val tremorAmplitude: Float?,
    val heartRate: Int?,
    val activityLevel: Float?,
    val fftSpectrum: List<Float>?,
    val dominantFrequency: Float?,
    val cadence: Int?,
    val totalSteps: Int?,
    val fogEpisodes: Int?
)

data class DailySummaryData(
    val avgTremor: Float?,
    val avgHeartRate: Int?,
    val sleepEfficiency: Int?,
    val lastMedicationTime: String?
)

data class TremorByHourData(
    val tpiScore: Float,
    val hour: Int
)

@Dao
interface SensorDao {
    @Query("SELECT * FROM tremor_readings ORDER BY timestamp DESC LIMIT 1")
    fun getLatestTremor(): Flow<TremorEntity?>

    @Query("SELECT * FROM heart_rate_readings ORDER BY timestamp DESC LIMIT 1")
    fun getLatestHeartRate(): Flow<HeartRateEntity?>

    @Query("SELECT * FROM tremor_readings ORDER BY timestamp DESC LIMIT 50")
    fun getRecentTremorReadings(): Flow<List<TremorEntity>>

    @Query("SELECT * FROM heart_rate_readings WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getHeartRateReadingsSince(startTime: Long): Flow<List<HeartRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTremor(reading: TremorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeartRate(reading: HeartRateEntity)

    @Query("SELECT * FROM gait_metrics ORDER BY timestamp DESC LIMIT 1")
    fun getLatestGait(): Flow<GaitMetricEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGait(metric: GaitMetricEntity)

    @Query("""
        SELECT 
            (SELECT AVG(tpiScore) FROM tremor_readings WHERE timestamp >= :startOfDay AND timestamp < :endOfDay) as avgTremor,
            (SELECT AVG(bpm) FROM heart_rate_readings WHERE timestamp >= :startOfDay AND timestamp < :endOfDay) as avgHeartRate,
            (SELECT efficiencyPercent FROM sleep_sessions WHERE date = :date LIMIT 1) as sleepEfficiency,
            (SELECT strftime('%H:%M', datetime(timestamp / 1000, 'unixepoch')) FROM medication_log WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC LIMIT 1) as lastMedicationTime
    """)
    suspend fun getDailySummary(startOfDay: Long, endOfDay: Long, date: Long): DailySummaryData

    @Query("""
        SELECT tpiScore as tpiScore, (timestamp / 3600000) as hour
        FROM tremor_readings 
        WHERE timestamp >= :startOfDay AND timestamp < :endOfDay
        GROUP BY hour
    """)
    suspend fun getTremorByHour(startOfDay: Long, endOfDay: Long): List<TremorByHourData>

    fun getLatestMetrics(): Flow<RealTimeMetricsData?> {
        return kotlinx.coroutines.flow.combine(
            getLatestTremor(),
            getLatestHeartRate(),
            getLatestGait()
        ) { tremor, hr, gait ->
            if (tremor != null || hr != null || gait != null) {
                RealTimeMetricsData(
                    tremorAmplitude = tremor?.rmsAmplitude,
                    heartRate = hr?.bpm,
                    activityLevel = calculateActivityLevel(tremor),
                    fftSpectrum = null,
                    dominantFrequency = tremor?.dominantFrequency,
                    cadence = gait?.cadence,
                    totalSteps = gait?.stepCount,
                    fogEpisodes = gait?.fogEpisodes
                )
            } else null
        }
    }

    private fun calculateActivityLevel(tremor: TremorEntity?): Float {
        return if (tremor != null) {
            (tremor.rmsAmplitude / 10f).coerceIn(0f, 1f)
        } else 0f
    }
}
