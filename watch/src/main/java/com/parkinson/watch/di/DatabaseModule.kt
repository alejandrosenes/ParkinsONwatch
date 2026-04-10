package com.parkinson.watch.di

import android.content.Context
import androidx.room.Room
import com.parkinson.watch.data.local.HealthDao
import com.parkinson.watch.data.local.ParkinsONDatabase
import com.parkinson.watch.data.local.SensorDao
import com.parkinson.watch.data.local.SleepDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ParkinsONDatabase {
        return Room.databaseBuilder(
            context,
            ParkinsONDatabase::class.java,
            "parkinson_watch_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideSensorDao(database: ParkinsONDatabase): SensorDao = database.sensorDao()

    @Provides
    fun provideHealthDao(database: ParkinsONDatabase): HealthDao = database.healthDao()

    @Provides
    fun provideSleepDao(database: ParkinsONDatabase): SleepDao = database.sleepDao()
}
