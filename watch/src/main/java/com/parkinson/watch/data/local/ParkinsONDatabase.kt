package com.parkinson.watch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.parkinson.watch.data.local.entity.ExerciseSessionEntity
import com.parkinson.watch.data.local.entity.GaitMetricEntity
import com.parkinson.watch.data.local.entity.HeartRateEntity
import com.parkinson.watch.data.local.entity.MedicationEntryEntity
import com.parkinson.watch.data.local.entity.MedicationScheduleEntity
import com.parkinson.watch.data.local.entity.SleepSessionEntity
import com.parkinson.watch.data.local.entity.TremorEntity
import com.parkinson.watch.data.local.entity.VoiceNoteEntity
import com.parkinson.watch.data.local.entity.WellbeingEntity

@Database(
    entities = [
        TremorEntity::class,
        HeartRateEntity::class,
        SleepSessionEntity::class,
        MedicationScheduleEntity::class,
        MedicationEntryEntity::class,
        WellbeingEntity::class,
        ExerciseSessionEntity::class,
        VoiceNoteEntity::class,
        GaitMetricEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ParkinsONDatabase : RoomDatabase() {
    abstract fun sensorDao(): SensorDao
    abstract fun healthDao(): HealthDao
    abstract fun sleepDao(): SleepDao
}
