package com.parkinson.watch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tremor_readings")
data class TremorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val tpiScore: Float,
    val dominantFrequency: Float,
    val bandPower3_7: Float,
    val bandPower8_12: Float,
    val rmsAmplitude: Float,
    val synced: Boolean = false
)

@Entity(tableName = "heart_rate_readings")
data class HeartRateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val bpm: Int,
    val rmssd: Float,
    val sdnn: Float,
    val lfHfRatio: Float,
    val synced: Boolean = false
)

@Entity(tableName = "sleep_sessions")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
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
    val latencyMinutes: Int,
    val synced: Boolean = false
)

@Entity(tableName = "medication_schedules")
data class MedicationScheduleEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val dose: String,
    val scheduleTimes: String,
    val lastTaken: String?
)

@Entity(tableName = "medication_log")
data class MedicationEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val medicationId: Long,
    val medicationName: String,
    val dose: String,
    val withFood: Boolean
)

@Entity(tableName = "wellbeing_log")
data class WellbeingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val level: String,
    val note: String?
)

@Entity(tableName = "exercise_sessions")
data class ExerciseSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimestamp: Long,
    val endTimestamp: Long?,
    val type: String,
    val durationMinutes: Int,
    val rpe: Int?,
    val tremorBefore: Float?,
    val tremorAfter: Float?,
    val synced: Boolean = false
)

@Entity(tableName = "voice_notes")
data class VoiceNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val filePath: String,
    val durationSeconds: Int,
    val synced: Boolean = false
)

@Entity(tableName = "gait_metrics")
data class GaitMetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val cadence: Int,
    val stepCount: Int,
    val fogEpisodes: Int,
    val asymmetryPercent: Float,
    val synced: Boolean = false
)
