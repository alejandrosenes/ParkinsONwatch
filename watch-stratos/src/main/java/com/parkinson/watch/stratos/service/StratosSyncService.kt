package com.parkinson.watch.stratos.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import com.parkinson.shared.domain.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.net.ServerSocket
import java.text.SimpleDateFormat
import java.util.*

class StratosSyncService : Service() {
    
    companion object {
        private const val TAG = "StratosSyncService"
        private const val SYNC_PORT = 7878
        private const val DATA_DIR = "parkinson_data"
    }
    
    private var serverThread: Thread? = null
    private var isRunning = false
    private lateinit var dataDirectory: File
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Creating StratosSyncService")
        dataDirectory = File(filesDir, DATA_DIR)
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs()
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting sync server on port $SYNC_PORT")
        startForeground(2, createNotification())
        startSyncServer()
        return START_STICKY
    }
    
    private fun createNotification(): android.app.Notification {
        val channelId = "sync_service_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Servicio de Sincronización",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        return android.app.Notification.Builder(this, channelId)
            .setContentTitle("ParkinsON")
            .setContentText("Servidor de sincronización activo")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .build()
    }
    
    private fun startSyncServer() {
        isRunning = true
        serverThread = Thread {
            try {
                val serverSocket = ServerSocket(SYNC_PORT)
                Log.d(TAG, "Sync server started, waiting for connections...")
                
                while (isRunning) {
                    try {
                        val clientSocket = serverSocket.accept()
                        Log.d(TAG, "Client connected: ${clientSocket.inetAddress}")
                        
                        // Leer petición
                        val reader = java.io.BufferedReader(
                            java.io.InputStreamReader(clientSocket.getInputStream())
                        )
                        val request = reader.readLine() ?: ""
                        
                        Log.d(TAG, "Received request: $request")
                        
                        // Procesar petición
                        val response = when {
                            request.contains("/sync") -> handleSyncRequest()
                            request.contains("/status") -> handleStatusRequest()
                            request.contains("/tremor") -> handleTremorDataRequest()
                            request.contains("/heartrate") -> handleHeartRateDataRequest()
                            else -> createErrorResponse("Unknown endpoint")
                        }
                        
                        // Enviar respuesta
                        val writer = FileWriter(clientSocket.getOutputStream())
                        writer.write(response)
                        writer.flush()
                        writer.close()
                        
                        clientSocket.close()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling client connection", e)
                    }
                }
                
                serverSocket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Sync server error", e)
                if (isRunning) {
                    // Reintentar después de 5 segundos
                    Thread.sleep(5000)
                    startSyncServer()
                }
            }
        }
        serverThread?.start()
    }
    
    private fun handleSyncRequest(): String {
        Log.d(TAG, "Handling sync request")
        
        try {
            // Recopilar todos los datos
            val tremorReadings = loadTremorData()
            val heartRateReadings = loadHeartRateData()
            val sleepSessions = loadSleepData()
            val gaitMetrics = loadGaitData()
            
            // Crear payload de sincronización
            val watchId = getWatchId()
            val payload = SyncPayload(
                watchId = watchId,
                exportTime = System.currentTimeMillis(),
                tremorReadings = tremorReadings,
                heartRateReadings = heartRateReadings,
                sleepSessions = sleepSessions,
                gaitMetrics = gaitMetrics
            )
            
            // Convertir a JSON
            val jsonResponse = JSONObject().apply {
                put("success", true)
                put("message", "Sincronización completada")
                put("watchId", watchId)
                put("exportTime", payload.exportTime)
                put("tremorCount", payload.tremorReadings.size)
                put("heartRateCount", payload.heartRateReadings.size)
                put("sleepCount", payload.sleepSessions.size)
                put("gaitCount", payload.gaitMetrics.size)
                put("data", JSONObject().apply {
                    put("tremorReadings", JSONArray().apply {
                        payload.tremorReadings.forEach { reading ->
                            put(JSONObject().apply {
                                put("timestamp", reading.timestamp)
                                put("tpiScore", reading.tpiScore)
                                put("dominantFrequency", reading.dominantFrequency)
                                put("bandPower3_7", reading.bandPower3_7)
                                put("bandPower8_12", reading.bandPower8_12)
                                put("rmsAmplitude", reading.rmsAmplitude)
                            })
                        }
                    })
                    put("heartRateReadings", JSONArray().apply {
                        payload.heartRateReadings.forEach { reading ->
                            put(JSONObject().apply {
                                put("timestamp", reading.timestamp)
                                put("bpm", reading.bpm)
                                put("rmssd", reading.rmssd)
                                put("sdnn", reading.sdnn)
                                put("lfHfRatio", reading.lfHfRatio)
                            })
                        }
                    })
                })
            }
            
            Log.d(TAG, "Sync completed: ${payload.tremorReadings.size} tremor readings, ${payload.heartRateReadings.size} HR readings")
            
            // Eliminar datos ya sincronizados
            clearSyncedData()
            
            return "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n${jsonResponse.toString()}"
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync", e)
            return createErrorResponse("Sync failed: ${e.message}")
        }
    }
    
    private fun handleStatusRequest(): String {
        val status = JSONObject().apply {
            put("success", true)
            put("batteryLevel", getBatteryLevel())
            put("storageUsed", getStorageUsed())
            put("lastSensorReading", getLastSensorReadingTime())
            put("isCollecting", true)
        }
        return "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n${status.toString()}"
    }
    
    private fun handleTremorDataRequest(): String {
        val readings = loadTremorData()
        val response = JSONObject().apply {
            put("success", true)
            put("count", readings.size)
            put("data", JSONArray().apply {
                readings.forEach { reading ->
                    put(JSONObject().apply {
                        put("timestamp", reading.timestamp)
                        put("tpiScore", reading.tpiScore)
                        put("dominantFrequency", reading.dominantFrequency)
                        put("bandPower3_7", reading.bandPower3_7)
                        put("bandPower8_12", reading.bandPower8_12)
                        put("rmsAmplitude", reading.rmsAmplitude)
                    })
                }
            })
        }
        return "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n${response.toString()}"
    }
    
    private fun handleHeartRateDataRequest(): String {
        val readings = loadHeartRateData()
        val response = JSONObject().apply {
            put("success", true)
            put("count", readings.size)
            put("data", JSONArray().apply {
                readings.forEach { reading ->
                    put(JSONObject().apply {
                        put("timestamp", reading.timestamp)
                        put("bpm", reading.bpm)
                        put("rmssd", reading.rmssd)
                        put("sdnn", reading.sdnn)
                        put("lfHfRatio", reading.lfHfRatio)
                    })
                }
            })
        }
        return "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n${response.toString()}"
    }
    
    private fun createErrorResponse(message: String): String {
        val error = JSONObject().apply {
            put("success", false)
            put("error", message)
        }
        return "HTTP/1.1 400 Bad Request\r\nContent-Type: application/json\r\n\r\n${error.toString()}"
    }
    
    private fun loadTremorData(): List<TremorReadingExport> {
        val readings = mutableListOf<TremorReadingExport>()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val file = File(dataDirectory, "tremor_$today.csv")
        
        if (file.exists()) {
            try {
                file.forEachLine { line ->
                    if (!line.startsWith("timestamp")) {
                        val parts = line.split(",")
                        if (parts.size >= 6) {
                            readings.add(TremorReadingExport(
                                timestamp = parts[0].toLongOrNull() ?: System.currentTimeMillis(),
                                tpiScore = parts[1].toFloatOrNull() ?: 0f,
                                dominantFrequency = parts[2].toFloatOrNull() ?: 0f,
                                bandPower3_7 = parts[3].toFloatOrNull() ?: 0f,
                                bandPower8_12 = parts[4].toFloatOrNull() ?: 0f,
                                rmsAmplitude = parts[5].toFloatOrNull() ?: 0f
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading tremor data", e)
            }
        }
        
        // También cargar días anteriores (últimos 7 días)
        for (i in 1..7) {
            val pastDate = Date(System.currentTimeMillis() - i * 24 * 60 * 60 * 1000)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(pastDate)
            val pastFile = File(dataDirectory, "tremor_$dateStr.csv")
            
            if (pastFile.exists()) {
                try {
                    pastFile.forEachLine { line ->
                        if (!line.startsWith("timestamp")) {
                            val parts = line.split(",")
                            if (parts.size >= 6) {
                                readings.add(TremorReadingExport(
                                    timestamp = parts[0].toLongOrNull() ?: System.currentTimeMillis(),
                                    tpiScore = parts[1].toFloatOrNull() ?: 0f,
                                    dominantFrequency = parts[2].toFloatOrNull() ?: 0f,
                                    bandPower3_7 = parts[3].toFloatOrNull() ?: 0f,
                                    bandPower8_12 = parts[4].toFloatOrNull() ?: 0f,
                                    rmsAmplitude = parts[5].toFloatOrNull() ?: 0f
                                ))
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading past tremor data", e)
                }
            }
        }
        
        return readings
    }
    
    private fun loadHeartRateData(): List<HeartRateReadingExport> {
        // Simulación de datos de frecuencia cardíaca (se implementará con sensor real)
        val readings = mutableListOf<HeartRateReadingExport>()
        val now = System.currentTimeMillis()
        
        // Generar lecturas de ejemplo basadas en tiempo real
        for (i in 0 until 24) {
            val timestamp = now - i * 60 * 60 * 1000
            readings.add(HeartRateReadingExport(
                timestamp = timestamp,
                bpm = (60 + Math.random() * 40).toInt(),
                rmssd = (30 + Math.random() * 20).toFloat(),
                sdnn = (40 + Math.random() * 30).toFloat(),
                lfHfRatio = (1.0 + Math.random() * 2).toFloat()
            ))
        }
        
        return readings
    }
    
    private fun loadSleepData(): List<SleepSessionExport> {
        // Datos de sueño simulados (se implementará con seguimiento real)
        return listOf()
    }
    
    private fun loadGaitData(): List<GaitMetricExport> {
        // Datos de marcha simulados (se implementará con acelerómetro)
        return listOf()
    }
    
    private fun getWatchId(): String {
        return android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "stratos_unknown"
    }
    
    private fun getBatteryLevel(): Int {
        // Implementación básica de nivel de batería
        return 85 // Valor por defecto
    }
    
    private fun getStorageUsed(): Long {
        return dataDirectory.walk().filter { it.isFile }.map { it.length() }.sum()
    }
    
    private fun getLastSensorReadingTime(): Long {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val file = File(dataDirectory, "tremor_$today.csv")
        return if (file.exists()) file.lastModified() else 0L
    }
    
    private fun clearSyncedData() {
        // Eliminar archivos de datos antiguos (más de 7 días)
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        
        dataDirectory.listFiles()?.forEach { file ->
            if (file.lastModified() < sevenDaysAgo) {
                file.delete()
                Log.d(TAG, "Deleted old data file: ${file.name}")
            }
        }
    }
    
    override fun onDestroy() {
        Log.d(TAG, "Stopping sync server")
        isRunning = false
        serverThread?.interrupt()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
