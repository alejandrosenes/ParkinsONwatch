package com.parkinson.watch.amazfit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tremor_data")
data class TremorEntity(
    @PrimaryKey val timestamp: Long,
    val tpiScore: Float,
    val dominantFrequency: Float,
    val bandPower3_7: Float,
    val bandPower8_12: Float,
    val rmsAmplitude: Float
)

@Entity(tableName = "heart_rate_data")
data class HeartRateEntity(
    @PrimaryKey val timestamp: Long,
    val bpm: Int,
    val rmssd: Float,
    val sdnn: Float,
    val lfHfRatio: Float
)

@Entity(tableName = "gait_metrics")
data class GaitMetricEntity(
    @PrimaryKey val timestamp: Long,
    val cadence: Int,
    val stepCount: Int,
    val fogEpisodes: Int,
    val asymmetryPercent: Float
)
