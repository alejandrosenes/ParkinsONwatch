package com.parkinson.watch.stratos.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.parkinson.shared.models.EventType
import com.parkinson.shared.models.MedicalEvent
import com.parkinson.shared.models.SensorContext
import kotlin.math.sqrt

/**
 * Servicio de detección de temblores y estados ON/OFF usando acelerómetro y giroscopio.
 */
class TremorDetectionService(private val context: Context) : SensorEventListener {

    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var lastAccelValues = floatArrayOf(0f, 0f, 0f)
    private var lastGyroValues = floatArrayOf(0f, 0f, 0f)
    
    // Ventana de tiempo para análisis (ms)
    private val windowSize = 2000L
    private val eventBuffer = mutableListOf<SensorSample>()
    
    private var isDetecting = false
    private var onDetectionListener: ((MedicalEvent) -> Unit)? = null

    interface TremorListener {
        fun onTremorDetected(event: MedicalEvent)
        fun onStateChanged(eventType: EventType)
    }

    fun startDetection(listener: (MedicalEvent) -> Unit) {
        onDetectionListener = listener
        isDetecting = true
        
        accelerometer?.let { 
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) 
        }
        gyroscope?.let { 
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) 
        }
    }

    fun stopDetection() {
        isDetecting = false
        sensorManager.unregisterListener(this)
        eventBuffer.clear()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isDetecting) return

        val timestamp = System.currentTimeMillis()
        
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                lastAccelValues = event.values.clone()
            }
            Sensor.TYPE_GYROSCOPE -> {
                lastGyroValues = event.values.clone()
            }
        }

        // Añadir muestra a la ventana deslizante
        eventBuffer.add(
            SensorSample(
                timestamp = timestamp,
                accelMagnitude = calculateMagnitude(lastAccelValues),
                gyroMagnitude = calculateMagnitude(lastGyroValues),
                accelRaw = lastAccelValues,
                gyroRaw = lastGyroValues
            )
        )

        // Mantener solo la ventana de tiempo definida
        val cutoff = timestamp - windowSize
        eventBuffer.removeAll { it.timestamp < cutoff }

        // Analizar si hay suficientes datos
        if (eventBuffer.size > 50) { // ~1 segundo a 50Hz
            analyzeWindow(timestamp)
        }
    }

    private fun analyzeWindow(currentTime: Long) {
        val samples = eventBuffer.toList()
        
        // Calcular estadísticas
        val avgAccel = samples.map { it.accelMagnitude }.average()
        val stdAccel = calculateStdDev(samples.map { it.accelMagnitude })
        val avgGyro = samples.map { it.gyroMagnitude }.average()
        val stdGyro = calculateStdDev(samples.map { it.gyroMagnitude })

        // Frecuencia dominante (simplificada)
        val dominantFrequency = estimateDominantFrequency(samples)

        // Detectar tipo de evento
        val detectedType = detectEventType(avgAccel, stdAccel, avgGyro, stdGyro, dominantFrequency)
        
        if (detectedType != null) {
            val severity = calculateSeverity(stdAccel, stdGyro)
            
            val event = MedicalEvent(
                timestamp = currentTime,
                type = detectedType,
                severity = severity,
                durationSeconds = (windowSize / 1000).toInt(),
                context = SensorContext(
                    avgHeartRate = getCurrentHeartRate(), // Implementar según hardware
                    activityLevel = inferActivityLevel(avgAccel, avgGyro),
                    sleepQualityScore = null, // Se cruza después con datos de sueño
                    lastMealTime = null, // Se cruza después con datos de nutrición
                    lastMedicationTime = null // Se cruza después con registro de medicación
                )
            )
            
            onDetectionListener?.invoke(event)
        }
    }

    private fun detectEventType(
        avgAccel: Double,
        stdAccel: Double,
        avgGyro: Double,
        stdGyro: Double,
        frequency: Float
    ): EventType? {
        // Umbrales empíricos para Parkinson (ajustables por paciente)
        return when {
            // Temblor en reposo: 4-6 Hz, baja actividad general pero oscilación constante
            frequency in 3.5..6.5 && stdAccel > 0.5 && avgAccel < 2.0 -> 
                EventType.TREMOR_REST
            
            // Temblor de acción: ocurre con movimiento voluntario
            frequency in 3.5..6.5 && avgAccel > 2.0 && stdAccel > 0.8 -> 
                EventType.TREMOR_ACTION
            
            // Estado OFF: rigidez, movimientos lentos, poca variabilidad
            stdAccel < 0.3 && stdGyro < 0.2 && avgAccel < 1.0 -> 
                EventType.OFF_STATE
            
            // Estado ON: movimiento fluido, variabilidad normal
            stdAccel in 0.4..1.5 && avgAccel in 1.0..3.0 -> 
                EventType.ON_STATE
            
            // Freezing: intento de movimiento pero sin desplazamiento (patrón específico)
            stdAccel > 1.5 && avgAccel < 0.5 -> 
                EventType.FREEZING
            
            else -> null
        }
    }

    private fun calculateSeverity(stdAccel: Double, stdGyro: Double): Float {
        // Normalizar severidad entre 0.0 y 1.0
        val combined = (stdAccel * 0.6 + stdGyro * 0.4)
        return (combined / 3.0).toFloat().coerceIn(0.0f, 1.0f)
    }

    private fun estimateDominantFrequency(samples: List<SensorSample>): Float {
        // Estimación simplificada usando cruces por cero
        if (samples.size < 10) return 0f
        
        val zeroCrossings = samples.windowed(2).count { pair ->
            (pair[0].accelMagnitude - 9.81) * (pair[1].accelMagnitude - 9.81) < 0
        }
        
        val durationSeconds = (samples.last().timestamp - samples.first().timestamp) / 1000f
        return if (durationSeconds > 0) (zeroCrossings / 2f) / durationSeconds else 0f
    }

    private fun inferActivityLevel(avgAccel: Double, avgGyro: Double): String {
        return when {
            avgAccel < 1.5 -> "SEDENTARY"
            avgAccel < 3.0 -> "WALKING"
            else -> "RUNNING"
        }
    }

    private fun getCurrentHeartRate(): Int {
        // TODO: Integrar con sensor de frecuencia cardíaca del reloj
        return 0
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private data class SensorSample(
        val timestamp: Long,
        val accelMagnitude: Float,
        val gyroMagnitude: Float,
        val accelRaw: FloatArray,
        val gyroRaw: FloatArray
    )

    private fun calculateMagnitude(values: FloatArray): Float {
        return sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
    }

    private fun calculateStdDev(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }
}
