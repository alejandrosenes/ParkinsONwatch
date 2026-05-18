package com.parkinson.shared.models

import java.io.Serializable

/**
 * Representa un evento médico detectado por el reloj.
 */
data class MedicalEvent(
    val timestamp: Long,
    val type: EventType, // TREMOR, ON_STATE, OFF_STATE
    val severity: Float, // 0.0 a 1.0
    val durationSeconds: Int,
    val context: SensorContext
) : Serializable

enum class EventType {
    TREMOR_REST,
    TREMOR_ACTION,
    ON_STATE,      // Movimiento fluido, medicación activa
    OFF_STATE,     // Rigidez, bradicinesia, sin medicación efectiva
    FREEZING       // Episodio de congelación de la marcha
}

data class SensorContext(
    val avgHeartRate: Int,
    val activityLevel: String, // SEDENTARY, WALKING, RUNNING
    val sleepQualityScore: Float?, // Si ocurre durante el sueño o tras él
    val lastMealTime: Long?, // Tiempo desde la última comida registrada
    val lastMedicationTime: Long? // Tiempo desde la última dosis registrada
) : Serializable

/**
 * Resultado del cruce de datos (Correlación)
 */
data class HealthInsight(
    val id: String,
    val timestamp: Long,
    val summary: String,
    val correlations: List<CorrelationFactor>,
    val recommendation: String
) : Serializable

data class CorrelationFactor(
    val factor: String, // "Sueño pobre", "Ayuno prolongado", "Alta actividad"
    val impact: String, // "POSITIVO", "NEGATIVO", "NEUTRO"
    val confidence: Float
) : Serializable
