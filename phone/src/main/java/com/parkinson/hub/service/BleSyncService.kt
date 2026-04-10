package com.parkinson.hub.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.parkinson.hub.ParkinsONHubApp
import com.parkinson.hub.R
import com.parkinson.hub.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class BleSyncService : Service() {

    @Inject
    lateinit var bleManager: BleManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverSocket: BluetoothServerSocket? = null
    private var isRunning = false

    companion object {
        const val ACTION_START = "com.parkinson.hub.START_BLE_SYNC"
        const val ACTION_STOP = "com.parkinson.hub.STOP_BLE_SYNC"
        private const val NOTIFICATION_ID = 2001
        private val SERVICE_UUID: UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startServer()
            ACTION_STOP -> stopServer()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startServer() {
        startForeground(NOTIFICATION_ID, createNotification())
        isRunning = true

        serviceScope.launch {
            try {
                val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val bluetoothAdapter = bluetoothManager.adapter

                if (bluetoothAdapter == null) {
                    stopSelf()
                    return@launch
                }

                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                    "ParkinsON Watch",
                    SERVICE_UUID
                )

                while (isRunning) {
                    try {
                        val socket = serverSocket?.accept()
                        if (socket != null) {
                            handleConnection(socket)
                        }
                    } catch (e: IOException) {
                        if (isRunning) {
                            // Retry connection
                        }
                    }
                }
            } catch (e: SecurityException) {
                stopSelf()
            }
        }
    }

    private fun handleConnection(socket: BluetoothSocket) {
        serviceScope.launch {
            try {
                bleManager.handleConnection(socket)
            } catch (e: Exception) {
                socket.close()
            }
        }
    }

    private fun stopServer() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            // Ignore
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ParkinsONHubApp.CHANNEL_SYNC)
            .setContentTitle("ParkinsON Hub")
            .setContentText("Esperando conexión del reloj...")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            // Ignore
        }
    }
}

class BleManager @Inject constructor() {
    suspend fun handleConnection(socket: BluetoothSocket) {
        // Handle incoming BLE connection from watch
        // Parse data, store in database, send to UI
    }
}
