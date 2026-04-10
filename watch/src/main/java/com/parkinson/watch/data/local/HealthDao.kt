package com.parkinson.watch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parkinson.watch.data.local.entity.ExerciseSessionEntity
import com.parkinson.watch.data.local.entity.MedicationEntryEntity
import com.parkinson.watch.data.local.entity.MedicationScheduleEntity
import com.parkinson.watch.data.local.entity.VoiceNoteEntity
import com.parkinson.watch.data.local.entity.WellbeingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM medication_schedules")
    fun getAllMedicationSchedules(): Flow<List<MedicationScheduleEntity>>

    @Query("SELECT * FROM medication_log WHERE timestamp >= :startTime AND timestamp < :endTime")
    fun getMedicationLogForDate(startTime: Long, endTime: Long): Flow<List<MedicationEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationSchedule(schedule: MedicationScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationLog(entry: MedicationEntryEntity)

    @Query("SELECT * FROM wellbeing_log WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    fun getWellbeingForDate(startTime: Long, endTime: Long): Flow<List<WellbeingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWellbeing(entry: WellbeingEntity)

    @Query("SELECT * FROM exercise_sessions ORDER BY startTimestamp DESC LIMIT 10")
    fun getRecentExerciseSessions(): Flow<List<ExerciseSessionEntity>>

    @Query("SELECT * FROM exercise_sessions WHERE startTimestamp >= :startTime AND endTimestamp IS NULL LIMIT 1")
    suspend fun getActiveExerciseSession(): ExerciseSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSession(session: ExerciseSessionEntity): Long

    @Query("UPDATE exercise_sessions SET endTimestamp = :endTime, durationMinutes = :duration WHERE id = :sessionId")
    suspend fun endExerciseSession(sessionId: Long, endTime: Long, duration: Int)

    @Query("SELECT * FROM voice_notes ORDER BY timestamp DESC")
    fun getAllVoiceNotes(): Flow<List<VoiceNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceNote(note: VoiceNoteEntity)

    suspend fun getDailySummary(startOfDay: Long, endOfDay: Long): DailySummaryData {
        val tremorByHour = getTremorByHour(startOfDay, endOfDay).associate { it }
        val meds = getMedicationLogForDate(startOfDay, endOfDay)
        val medsByHour = mutableListOf<Int>()
        
        return DailySummaryData(
            avgTremor = 0f,
            avgHeartRate = 0,
            sleepEfficiency = 0,
            lastMedicationTime = null,
            tremorByHour = tremorByHour,
            hrByHour = null,
            medsByHour = medsByHour
        )
    }

    @Query("""
        SELECT tpi_score, (timestamp / 3600000) as hour
        FROM tremor_readings 
        WHERE timestamp >= :startOfDay AND timestamp < :endOfDay
        GROUP BY hour
    """)
    suspend fun getTremorByHour(startOfDay: Long, endOfDay: Long): List<Pair<Int, Float>>
}

data class DailySummaryData(
    val avgTremor: Float,
    val avgHeartRate: Int,
    val sleepEfficiency: Int,
    val lastMedicationTime: String?,
    val tremorByHour: Map<Int, Float>?,
    val hrByHour: Map<Int, Int>?,
    val medsByHour: List<Int>?
)
