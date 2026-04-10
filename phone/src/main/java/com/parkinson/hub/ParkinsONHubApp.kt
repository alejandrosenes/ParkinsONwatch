package com.parkinson.hub

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ParkinsONHubApp : Application() {

    companion object {
        const val CHANNEL_SYNC = "device_sync"
        const val CHANNEL_ALERTS = "alerts"
        const val CHANNEL_REMINDERS = "reminders"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Sincronización con Reloj",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sincronización de datos con ParkinsON Watch"
                setShowBadge(false)
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Alertas Clínicas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas sobre cambios en síntomas y patrones detectados"
                enableVibration(true)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Recordatorios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de medicación y ejercicios"
            }

            notificationManager.createNotificationChannels(
                listOf(syncChannel, alertChannel, reminderChannel)
            )
        }
    }
}
