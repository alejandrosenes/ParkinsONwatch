package com.parkinson.watch.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.parkinson.watch.ParkinsONWatchApp
import com.parkinson.watch.R
import com.parkinson.watch.data.analysis.TremorAnalyzer
import com.parkinson.watch.data.local.SensorDao
import com.parkinson.watch.data.local.entity.GaitMetricEntity
import com.parkinson.watch.data.local.entity.HeartRateEntity
import com.parkinson.watch.data.local.entity.TremorEntity
import com.parkinson.watch.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SensorService : Service(), SensorEventListener {

    @Inject
    lateinit var sensorDao: SensorDao

    @Inject
    lateinit var tremorAnalyzer: TremorAnalyzer

    private lateinit var sensorManager: SensorManager
    private var wakeLock: PowerManager.WakeLock? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val accelerometerBuffer = mutableListOf<FloatArray>()
    private val gyroBuffer = mutableListOf<FloatArray>()

    private var lastStepTime = 0L
    private var stepCount = 0
    private var fogEpisodes = 0
    private var lastFogDetection = 0L

    private var monitoringMode = MonitoringMode.NORMAL
    private var processingJob: Job? = null

    companion object {
        const val ACTION_START = "com.parkinson.watch.START_MONITORING"
        const val ACTION_STOP = "com.parkinson.watch.STOP_MONITORING"
        const val ACTION_SET_MODE = "com.parkinson.watch.SET_MODE"

        const val MODE_NORMAL = "normal"
        const val MODE_HIGH_FREQUENCY = "high_frequency"
        const val MODE_SLEEP = "sleep"

        private const val NOTIFICATION_ID = 1001
        private const val ACCEL_BUFFER_SIZE = 256
        private const val GYRO_BUFFER_SIZE = 64
        private const val FOG_THRESHOLD_HZ = 8.0f
        private const val MIN_STEP_INTERVAL_MS = 250L
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
            ACTION_SET_MODE -> {
                val mode = intent.getStringExtra(MODE_NORMAL) ?: MODE_NORMAL
                setMonitoringMode(mode)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startMonitoring() {
        startForeground(NOTIFICATION_ID, createNotification())
        registerSensors()
        startProcessingLoop()
    }

    private fun stopMonitoring() {
        unregisterSensors()
        processingJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun setMonitoringMode(mode: String) {
        monitoringMode = when (mode) {
            MODE_HIGH_FREQUENCY -> MonitoringMode.HIGH_FREQUENCY
            MODE_SLEEP -> MonitoringMode.SLEEP
            else -> MonitoringMode.NORMAL
        }
        updateSensorDelays()
    }

    private fun registerSensors() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { accel ->
            sensorManager.registerListener(
                this,
                accel,
                getSensorDelay()
            )
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let { gyro ->
            sensorManager.registerListener(
                this,
                gyro,
                getSensorDelay()
            )
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)?.let { hr ->
            sensorManager.registerListener(
                this,
                hr,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.let { steps ->
            sensorManager.registerListener(
                this,
                steps,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }

    private fun getSensorDelay(): Int = when (monitoringMode) {
        MonitoringMode.HIGH_FREQUENCY -> SensorManager.SENSOR_DELAY_FASTEST
        MonitoringMode.SLEEP -> SensorManager.SENSOR_DELAY_UI
        MonitoringMode.NORMAL -> SensorManager.SENSOR_DELAY_GAME
    }

    private fun updateSensorDelays() {
        unregisterSensors()
        registerSensors()
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event)
            Sensor.TYPE_GYROSCOPE -> handleGyroscope(event)
            Sensor.TYPE_HEART_RATE -> handleHeartRate(event)
            Sensor.TYPE_STEP_COUNTER -> handleStepCounter(event)
        }
    }

    private val tempAccelValues = FloatArray(3)
    private val tempGyroValues = FloatArray(3)

    private fun handleAccelerometer(event: SensorEvent) {
        synchronized(accelerometerBuffer) {
            // Reuse array instead of cloning
            System.arraycopy(event.values, 0, tempAccelValues, 0, 3)
            accelerometerBuffer.add(tempAccelValues.copyOf())
            
            // Use more efficient removal for circular buffer
            if (accelerometerBuffer.size > ACCEL_BUFFER_SIZE) {
                accelerometerBuffer.removeAt(0)
            }
        }
    }

    private fun handleGyroscope(event: SensorEvent) {
        synchronized(gyroBuffer) {
            // Reuse array instead of cloning
            System.arraycopy(event.values, 0, tempGyroValues, 0, 3)
            gyroBuffer.add(tempGyroValues.copyOf())
            
            if (gyroBuffer.size > GYRO_BUFFER_SIZE) {
                gyroBuffer.removeAt(0)
            }
        }
    }

    private fun handleHeartRate(event: SensorEvent) {
        val bpm = event.values[0].toInt()
        if (bpm > 0 && bpm < 250) { // Validate heart rate range
            serviceScope.launch {
                sensorDao.insertHeartRate(
                    HeartRateEntity(
                        timestamp = System.currentTimeMillis(),
                        bpm = bpm,
                        rmssd = 0f,
                        sdnn = 0f,
                        lfHfRatio = 0f
                    )
                )
            }
        }
    }

    private fun handleStepCounter(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastStepTime > MIN_STEP_INTERVAL_MS) {
            stepCount++
            lastStepTime = currentTime
            detectFOG()
        }
    }

    private fun detectFOG() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFogDetection > 30000) {
            val dominantFreq = tremorAnalyzer.getDominantFrequency()
            if (dominantFreq > FOG_THRESHOLD_HZ && stepCount == 0) {
                fogEpisodes++
                lastFogDetection = currentTime
            }
        }
    }

    private fun startProcessingLoop() {
        processingJob = serviceScope.launch {
            while (isActive) {
                processBuffers()
                delay(1000)
            }
        }
    }

    private suspend fun processBuffers() {
        val accelData: List<FloatArray>
        synchronized(accelerometerBuffer) {
            if (accelerometerBuffer.size < 128) return
            accelData = accelerometerBuffer.takeLast(128)
        }

        val analysis = tremorAnalyzer.analyze(accelData)

        sensorDao.insertTremor(
            TremorEntity(
                timestamp = System.currentTimeMillis(),
                tpiScore = analysis.tpiScore,
                dominantFrequency = analysis.dominantFrequency,
                bandPower3_7 = analysis.bandPower3_7,
                bandPower8_12 = analysis.bandPower8_12,
                rmsAmplitude = analysis.rmsAmplitude
            )
        )

        if (stepCount > 0) {
            sensorDao.insertGait(
                GaitMetricEntity(
                    timestamp = System.currentTimeMillis(),
                    cadence = calculateCadence(),
                    stepCount = stepCount,
                    fogEpisodes = fogEpisodes,
                    asymmetryPercent = 0f
                )
            )
        }
    }

    private fun calculateCadence(): Int {
        if (lastStepTime == 0L || stepCount == 0) return 0
        val elapsedMinutes = (System.currentTimeMillis() - lastStepTime) / 60000f
        return if (elapsedMinutes > 0) {
            (stepCount / elapsedMinutes).toInt()
        } else 0
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ParkinsONWatchApp.CHANNEL_SENSORS)
            .setContentTitle("ParkinsON Watch")
            .setContentText("Monitorizando síntomas")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ParkinsON::SensorServiceWakeLock"
        )
        wakeLock?.acquire(8 * 60 * 60 * 1000L)
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
        serviceScope.cancel()
    }

    enum class MonitoringMode {
        NORMAL, HIGH_FREQUENCY, SLEEP
    }
}
