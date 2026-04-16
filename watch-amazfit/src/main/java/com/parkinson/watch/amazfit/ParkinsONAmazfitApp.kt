package com.parkinson.watch.amazfit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ParkinsONAmazfitApp : Application() {

    companion object {
        const val CHANNEL_SENSORS = "sensor_monitoring"
        const val CHANNEL_SYNC = "data_sync"
        const val CHANNEL_ALERTS = "health_alerts"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val sensorChannel = NotificationChannel(
                CHANNEL_SENSORS,
                "Monitorización de Sensores",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones sobre la monitorización continua de sensores"
                enableLights(false)
                enableVibration(false)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Sincronización de Datos",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones sobre sincronización con el teléfono"
                enableLights(false)
                enableVibration(false)
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Alertas de Salud",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas importantes sobre síntomas de Parkinson"
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(sensorChannel, syncChannel, alertsChannel)
            )
        }
    }
}
