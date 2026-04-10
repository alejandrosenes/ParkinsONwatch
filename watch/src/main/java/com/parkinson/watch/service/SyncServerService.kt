package com.parkinson.watch.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.parkinson.watch.ParkinsONWatchApp
import com.parkinson.watch.data.local.ParkinsONDatabase
import com.parkinson.watch.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SyncServerService : Service() {

    @Inject
    lateinit var database: ParkinsONDatabase

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    inner class LocalBinder : Binder() {
        fun getService(): SyncServerService = this@SyncServerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    companion object {
        const val ACTION_START = "com.parkinson.watch.START_SYNC_SERVER"
        const val ACTION_STOP = "com.parkinson.watch.STOP_SYNC_SERVER"
        const val PORT = 7878
        private const val NOTIFICATION_ID = 1002
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startServer()
            ACTION_STOP -> stopServer()
        }
        return START_STICKY
    }

    private fun startServer() {
        startForeground(NOTIFICATION_ID, createNotification())

        serviceScope.launch {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = intToIp(wifiManager.connectionInfo.ipAddress)
        }
    }

    private fun stopServer() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun getExportData(): Map<String, Any> {
        return mapOf(
            "exportTime" to System.currentTimeMillis(),
            "status" to "ready"
        )
    }

    private fun intToIp(ip: Int): String {
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ParkinsONWatchApp.CHANNEL_SYNC)
            .setContentTitle("ParkinsON Watch")
            .setContentText("Servidor de sincronizacion activo")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@kotlinx.serialization.Serializable
data class ExportPayload(
    val exportTime: Long,
    val tremorReadings: List<TremorReadingExport>,
    val heartRateReadings: List<HeartRateReadingExport>,
    val sleepSessions: List<SleepSessionExport>,
    val medicationLog: List<MedicationLogExport>
)

@kotlinx.serialization.Serializable
data class TremorReadingExport(
    val timestamp: Long,
    val tpiScore: Float,
    val dominantFrequency: Float,
    val bandPower3_7: Float,
    val bandPower8_12: Float,
    val rmsAmplitude: Float
)

@kotlinx.serialization.Serializable
data class HeartRateReadingExport(
    val timestamp: Long,
    val bpm: Int,
    val rmssd: Float,
    val sdnn: Float,
    val lfHfRatio: Float
)

@kotlinx.serialization.Serializable
data class SleepSessionExport(
    val startTimestamp: Long,
    val endTimestamp: Long,
    val efficiencyPercent: Int,
    val rbdProxyScore: Float,
    val remMinutes: Int,
    val n1Minutes: Int,
    val n2Minutes: Int,
    val n3Minutes: Int,
    val awakeMinutes: Int,
    val awakenings: Int,
    val latencyMinutes: Int
)

@kotlinx.serialization.Serializable
data class MedicationLogExport(
    val timestamp: Long,
    val medicationName: String,
    val dose: String,
    val withFood: Boolean
)
