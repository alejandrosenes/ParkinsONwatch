package com.parkinson.watch.amazfit.di

import android.content.Context
import androidx.room.Room
import com.parkinson.watch.amazfit.data.local.ParkinsONAmazfitDatabase
import com.parkinson.watch.amazfit.data.local.SensorDao
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ParkinsONAmazfitDatabase {
        return Room.databaseBuilder(
            context,
            ParkinsONAmazfitDatabase::class.java,
            "parkinson_amazfit_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSensorDao(database: ParkinsONAmazfitDatabase): SensorDao {
        return database.sensorDao()
    }
}
