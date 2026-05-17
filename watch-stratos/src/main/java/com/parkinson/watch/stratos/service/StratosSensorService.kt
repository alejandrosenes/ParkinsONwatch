package com.parkinson.watch.stratos.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.parkinson.shared.domain.model.TremorReadingExport
import com.parkinson.shared.domain.model.HeartRateReadingExport
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class StratosSensorService : Service(), SensorEventListener {
    
    companion object {
        private const val TAG = "StratosSensorService"
        private const val SAMPLING_INTERVAL_MS = 50L // 20Hz
        private const val WINDOW_SIZE_SECONDS = 5
        private const val DATA_DIR = "parkinson_data"
        
        // Frecuencias de bandas para análisis de temblor (Hz)
        private const val BAND_LOW_MIN = 3.0f
        private const val BAND_LOW_MAX = 7.0f
        private const val BAND_HIGH_MIN = 8.0f
        private const val BAND_HIGH_MAX = 12.0f
    }
    
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    // Buffer de datos para análisis
    private val accelerometerBuffer = mutableListOf<FloatArray>()
    private val gyroscopeBuffer = mutableListOf<FloatArray>()
    private var lastSampleTime = 0L
    
    // Almacenamiento local
    private lateinit var dataDirectory: File
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Creating StratosSensorService")
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        
        // Configurar directorio de almacenamiento
        dataDirectory = File(filesDir, DATA_DIR)
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs()
        }
        
        // Adquirir wake lock parcial para mantener el servicio activo
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ParkinsON::SensorService"
        )
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting sensor collection")
        startForeground(1, createNotification())
        registerSensors()
        wakeLock?.acquire(10*60*1000L /*10 minutes*/)
        return START_STICKY
    }
    
    private fun createNotification(): android.app.Notification {
        val channelId = "sensor_service_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Servicio de Sensores",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        return android.app.Notification.Builder(this, channelId)
            .setContentTitle("ParkinsON")
            .setContentText("Monitoreo de sensores activo")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
    }
    
    private fun registerSensors() {
        accelerometer?.let {
            sensorManager?.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
            Log.d(TAG, "Accelerometer registered")
        }
        
        gyroscope?.let {
            sensorManager?.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
            Log.d(TAG, "Gyroscope registered")
        }
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        
        // Limitar frecuencia de muestreo
        if (currentTime - lastSampleTime < SAMPLING_INTERVAL_MS) {
            return
        }
        lastSampleTime = currentTime
        
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerBuffer.add(event.values.clone())
                if (accelerometerBuffer.size > WINDOW_SIZE_SECONDS * (1000 / SAMPLING_INTERVAL_MS).toInt()) {
                    accelerometerBuffer.removeAt(0)
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroscopeBuffer.add(event.values.clone())
                if (gyroscopeBuffer.size > WINDOW_SIZE_SECONDS * (1000 / SAMPLING_INTERVAL_MS).toInt()) {
                    gyroscopeBuffer.removeAt(0)
                }
            }
        }
        
        // Analizar datos cuando tenemos suficiente información
        if (accelerometerBuffer.size >= 50 && gyroscopeBuffer.size >= 50) {
            analyzeAndStoreData(currentTime)
        }
    }
    
    private fun analyzeAndStoreData(timestamp: Long) {
        try {
            // Calcular RMS de aceleración
            val rmsAmplitude = calculateRMS(accelerometerBuffer)
            
            // Calcular potencia de bandas mediante FFT simplificado
            val bandPower3_7 = calculateBandPower(accelerometerBuffer, BAND_LOW_MIN, BAND_LOW_MAX)
            val bandPower8_12 = calculateBandPower(accelerometerBuffer, BAND_HIGH_MIN, BAND_HIGH_MAX)
            
            // Calcular frecuencia dominante
            val dominantFrequency = estimateDominantFrequency(accelerometerBuffer)
            
            // Calcular TPI Score (Tremor Severity Index)
            val tpiScore = calculateTPIScore(rmsAmplitude, bandPower3_7, bandPower8_12, dominantFrequency)
            
            // Crear export de datos
            val tremorReading = TremorReadingExport(
                timestamp = timestamp,
                tpiScore = tpiScore,
                dominantFrequency = dominantFrequency,
                bandPower3_7 = bandPower3_7,
                bandPower8_12 = bandPower8_12,
                rmsAmplitude = rmsAmplitude
            )
            
            // Guardar en archivo local
            saveTremorReading(tremorReading)
            
            // Limpiar buffers parcialmente para mantener ventana deslizante
            val removeCount = accelerometerBuffer.size / 2
            repeat(removeCount) {
                if (accelerometerBuffer.isNotEmpty()) accelerometerBuffer.removeAt(0)
                if (gyroscopeBuffer.isNotEmpty()) gyroscopeBuffer.removeAt(0)
            }
            
            Log.d(TAG, "Stored tremor reading: TPI=$tpiScore, Freq=$dominantFrequency")
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing data", e)
        }
    }
    
    private fun calculateRMS(data: List<FloatArray>): Float {
        var sumSquared = 0.0
        var count = 0
        
        for (sample in data) {
            for (value in sample) {
                sumSquared += value * value
                count++
            }
        }
        
        return if (count > 0) sqrt(sumSquared / count).toFloat() else 0f
    }
    
    private fun calculateBandPower(data: List<FloatArray>, minFreq: Float, maxFreq: Float): Float {
        // Implementación simplificada de estimación de potencia espectral
        // En producción, usar FFT completo
        val samplingRate = 1000.0 / SAMPLING_INTERVAL_MS
        val nyquist = samplingRate / 2.0
        
        // Contar cruces por cero para estimar frecuencia predominante
        var zeroCrossings = 0
        var prevValue = 0.0
        
        for (sample in data) {
            val magnitude = sqrt(sample[0] * sample[0] + sample[1] * sample[1] + sample[2] * sample[2])
            if ((prevValue < 0 && magnitude >= 0) || (prevValue >= 0 && magnitude < 0)) {
                zeroCrossings++
            }
            prevValue = magnitude
        }
        
        val estimatedFreq = (zeroCrossings / 2.0) * (samplingRate / data.size)
        
        // Retornar potencia basada en qué tan cerca está de la banda objetivo
        return if (estimatedFreq >= minFreq && estimatedFreq <= maxFreq) {
            1.0f
        } else {
            0.5f / (1.0f + kotlin.math.abs(estimatedFreq - (minFreq + maxFreq) / 2))
        }
    }
    
    private fun estimateDominantFrequency(data: List<FloatArray>): Float {
        val samplingRate = 1000.0 / SAMPLING_INTERVAL_MS
        var zeroCrossings = 0
        var prevValue = 0.0
        
        for (sample in data) {
            val magnitude = sqrt(sample[0] * sample[0] + sample[1] * sample[1] + sample[2] * sample[2])
            if ((prevValue < 0 && magnitude >= 0) || (prevValue >= 0 && magnitude < 0)) {
                zeroCrossings++
            }
            prevValue = magnitude
        }
        
        return (zeroCrossings / 2.0) * (samplingRate / data.size)
    }
    
    private fun calculateTPIScore(rms: Float, bandLow: Float, bandHigh: Float, freq: Float): Float {
        // Algoritmo simplificado de TPI Score
        // Peso mayor a banda 3-7 Hz (temblor parkinsoniano típico)
        val bandWeight = if (freq >= 3.0 && freq <= 7.0) 1.5f else 0.8f
        return (rms * 10.0f * bandWeight + bandLow * 5.0f).coerceIn(0.0f, 100.0f)
    }
    
    private fun saveTremorReading(reading: TremorReadingExport) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = dateFormat.format(Date())
            val file = File(dataDirectory, "tremor_$today.csv")
            
            val exists = file.exists()
            val writer = FileWriter(file, true)
            
            if (!exists) {
                writer.append("timestamp,tpiScore,dominantFrequency,bandPower3_7,bandPower8_12,rmsAmplitude\n")
            }
            
            writer.append("${reading.timestamp},")
                .append("${reading.tpiScore},")
                .append("${reading.dominantFrequency},")
                .append("${reading.bandPower3_7},")
                .append("${reading.bandPower8_12},")
                .append("${reading.rmsAmplitude}\n")
            
            writer.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving tremor reading", e)
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }
    
    override fun onDestroy() {
        Log.d(TAG, "Destroying StratosSensorService")
        sensorManager?.unregisterListener(this)
        wakeLock?.release()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
