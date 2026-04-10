package com.parkinson.watch.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.parkinson.watch.ParkinsONWatchApp
import com.parkinson.watch.data.local.ParkinsONDatabase
import com.parkinson.watch.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.http.ContentType
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class SyncServerService : LifecycleService() {

    @Inject
    lateinit var database: ParkinsONDatabase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var server: io.ktor.server.embeddedServer.EmbeddedServer<*, *>? = null

    companion object {
        const val ACTION_START = "com.parkinson.watch.START_SYNC_SERVER"
        const val ACTION_STOP = "com.parkinson.watch.STOP_SYNC_SERVER"
        const val PORT = 7878
        private const val NOTIFICATION_ID = 1002
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
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

            server = embeddedServer(Netty, PORT) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }

                routing {
                    get("/status") {
                        call.respondText(
                            "ParkinsON Watch Sync Server - Running",
                            ContentType.Text.Plain
                        )
                    }

                    get("/export") {
                        try {
                            val payload = collectExportData()
                            call.respondText(
                                kotlinx.serialization.json.Json.encodeToString(
                                    kotlinx.serialization.serializer(),
                                    payload
                                ),
                                ContentType.Application.Json
                            )
                        } catch (e: Exception) {
                            call.respondText(
                                "Error: ${e.message}",
                                ContentType.Text.Plain
                            )
                        }
                    }
                }
            }.start(wait = false)
        }
    }

    private fun stopServer() {
        server?.stop()
        server = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun collectExportData(): ExportPayload {
        val tremorDao = database.sensorDao()

        return ExportPayload(
            exportTime = System.currentTimeMillis(),
            tremorReadings = emptyList(),
            heartRateReadings = emptyList(),
            sleepSessions = emptyList(),
            medicationLog = emptyList()
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
            .setContentText("Servidor de sincronización activo")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
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

abstract class LifecycleService : android.app.Service() {
    // Placeholder for lifecycle awareness
}
