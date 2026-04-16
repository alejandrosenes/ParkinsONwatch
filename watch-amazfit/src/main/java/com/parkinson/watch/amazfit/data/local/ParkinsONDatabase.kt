package com.parkinson.watch.amazfit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.parkinson.watch.amazfit.data.local.entity.TremorEntity
import com.parkinson.watch.amazfit.data.local.entity.HeartRateEntity
import com.parkinson.watch.amazfit.data.local.entity.GaitMetricEntity

@Database(
    entities = [
        TremorEntity::class,
        HeartRateEntity::class,
        GaitMetricEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ParkinsONAmazfitDatabase : RoomDatabase() {

    abstract fun sensorDao(): SensorDao

    companion object {
        @Volatile
        private var INSTANCE: ParkinsONAmazfitDatabase? = null

        fun getDatabase(context: Context): ParkinsONAmazfitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkinsONAmazfitDatabase::class.java,
                    "parkinson_amazfit_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
