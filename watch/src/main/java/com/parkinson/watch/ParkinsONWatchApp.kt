package com.parkinson.watch

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ParkinsONWatchApp : Application() {

    companion object {
        const val CHANNEL_SENSORS = "sensor_monitoring"
        const val CHANNEL_ALERTS = "alerts"
        const val CHANNEL_SYNC = "sync_service"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val sensorChannel = NotificationChannel(
                CHANNEL_SENSORS,
                "Monitorización de Sensores",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitoreo continuo de síntomas de Parkinson"
                setShowBadge(false)
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Alertas y Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de medicación y detección de síntomas"
                enableVibration(true)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Sincronización",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sincronización de datos con la aplicación del teléfono"
            }

            notificationManager.createNotificationChannels(
                listOf(sensorChannel, alertChannel, syncChannel)
            )
        }
    }
}
