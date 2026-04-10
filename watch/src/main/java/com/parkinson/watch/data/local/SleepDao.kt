package com.parkinson.watch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parkinson.watch.data.local.entity.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_sessions WHERE date = :epochDay LIMIT 1")
    fun getSleepForDate(epochDay: Long): Flow<SleepSessionEntity?>

    @Query("SELECT * FROM sleep_sessions WHERE date >= :startDay AND date <= :endDay ORDER BY date DESC")
    fun getSleepSessionsInRange(startDay: Long, endDay: Long): Flow<List<SleepSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepSession(session: SleepSessionEntity)

    @Query("""
        SELECT AVG(efficiencyPercent) as avgEfficiency,
               AVG(rbdProxyScore) as avgRbd,
               SUM(remMinutes + n1Minutes + n2Minutes + n3Minutes) as totalSleepMinutes
        FROM sleep_sessions 
        WHERE date >= :startDay AND date <= :endDay
    """)
    suspend fun getSleepStats(startDay: Long, endDay: Long): SleepStats
}

data class SleepStats(
    val avgEfficiency: Float?,
    val avgRbd: Float?,
    val totalSleepMinutes: Int?
)
