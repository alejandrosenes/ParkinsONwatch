package com.parkinson.watch.amazfit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.parkinson.watch.amazfit.data.local.entity.TremorEntity
import com.parkinson.watch.amazfit.data.local.entity.HeartRateEntity
import com.parkinson.watch.amazfit.data.local.entity.GaitMetricEntity

@Dao
interface SensorDao {
    @Insert
    suspend fun insertTremor(tremor: TremorEntity)

    @Insert
    suspend fun insertHeartRate(heartRate: HeartRateEntity)

    @Insert
    suspend fun insertGait(gait: GaitMetricEntity)

    @Query("SELECT * FROM tremor_data ORDER BY timestamp DESC LIMIT 100")
    suspend fun getRecentTremorData(): List<TremorEntity>

    @Query("SELECT * FROM heart_rate_data ORDER BY timestamp DESC LIMIT 100")
    suspend fun getRecentHeartRateData(): List<HeartRateEntity>

    @Query("SELECT * FROM gait_metrics ORDER BY timestamp DESC LIMIT 100")
    suspend fun getRecentGaitData(): List<GaitMetricEntity>

    @Query("DELETE FROM tremor_data WHERE timestamp < :olderThan")
    suspend fun deleteOldTremorData(olderThan: Long)

    @Query("DELETE FROM heart_rate_data WHERE timestamp < :olderThan")
    suspend fun deleteOldHeartRateData(olderThan: Long)

    @Query("DELETE FROM gait_metrics WHERE timestamp < :olderThan")
    suspend fun deleteOldGaitData(olderThan: Long)
}
