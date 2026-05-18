package com.parkinson.shared.ai

import com.parkinson.shared.models.*

/**
 * Algoritmo de cruce de datos para detectar correlaciones entre:
 * - Temblores y estados ON/OFF (sensores del reloj)
 * - Actividad física (Strava)
 * - Sueño
 * - Alimentación
 */
object HealthCorrelationEngine {

    /**
     * Analiza una lista de eventos médicos y actividades para generar insights.
     */
    fun analyze(
        medicalEvents: List<MedicalEvent>,
        stravaActivities: List<StravaActivitySummary>,
        sleepData: List<SleepSession>,
        nutritionLog: List<NutritionEntry>
    ): List<HealthInsight> {
        
        val insights = mutableListOf<HealthInsight>()

        // 1. Correlacionar episodios OFF con falta de sueño
        medicalEvents.filter { it.type == EventType.OFF_STATE }
            .forEach { offEvent ->
                val recentSleep = sleepData.lastOrNull { 
                    it.endTime > (offEvent.timestamp - 24 * 60 * 60 * 1000L) 
                }
                
                if (recentSleep != null && recentSleep.qualityScore < 0.6f) {
                    insights.add(
                        createInsight(
                            timestamp = offEvent.timestamp,
                            summary = "Episodio OFF detectado tras noche de sueño pobre",
                            factor = "Sueño insuficiente (< ${recentSleep.durationMinutes} min)",
                            impact = "NEGATIVO",
                            confidence = 0.85f,
                            recommendation = "Priorizar higiene del sueño. Considerar siesta reparadora de 20 min."
                        )
                    )
                }
            }

        // 2. Correlacionar actividad física intensa con temblores posteriores
        stravaActivities.forEach { activity ->
            val postActivityTremors = medicalEvents.filter { event ->
                event.type in listOf(EventType.TREMOR_REST, EventType.TREMOR_ACTION) &&
                event.timestamp > activity.endTime &&
                event.timestamp < (activity.endTime + 2 * 60 * 60 * 1000L) // 2 horas después
            }

            if (postActivityTremors.isNotEmpty() && activity.intensityLevel == "HIGH") {
                insights.add(
                    createInsight(
                        timestamp = activity.endTime,
                        summary = "Aumento de temblores tras ejercicio de alta intensidad",
                        factor = "Ejercicio intenso (${activity.type})",
                        impact = "NEGATIVO",
                        confidence = 0.75f,
                        recommendation = "Reducir intensidad o aumentar periodo de enfriamiento. Monitorizar hidratación."
                    )
                )
            } else if (postActivityTremors.isEmpty() && activity.intensityLevel == "MODERATE") {
                insights.add(
                    createInsight(
                        timestamp = activity.endTime,
                        summary = "Ejercicio moderado sin empeoramiento de síntomas",
                        factor = "Actividad física regulada",
                        impact = "POSITIVO",
                        confidence = 0.80f,
                        recommendation = "Mantener este nivel de actividad. Beneficioso para movilidad."
                    )
                )
            }
        }

        // 3. Correlacionar ayuno prolongado con episodios OFF
        nutritionLog.filter { it.type == NutritionType.MEAL }
            .sortedByDescending { it.timestamp }
            .zipWithNext { current, next ->
                val gapHours = (current.timestamp - next.timestamp) / (1000 * 60 * 60)
                if (gapHours > 5) {
                    val offDuringGap = medicalEvents.any { 
                        it.type == EventType.OFF_STATE && 
                        it.timestamp > next.timestamp && 
                        it.timestamp < current.timestamp
                    }
                    if (offDuringGap) {
                        insights.add(
                            createInsight(
                                timestamp = next.timestamp,
                                summary = "Episodio OFF asociado a ayuno prolongado",
                                factor = "Ayuno > ${gapHours.toInt()} horas",
                                impact = "NEGATIVO",
                                confidence = 0.90f,
                                recommendation = "Introducir snacks proteicos cada 3-4 horas. Evitar hipoglucemia."
                            )
                        )
                    }
                }
            }

        // 4. Correlacionar estado ON con ejercicio regular
        val onStates = medicalEvents.filter { it.type == EventType.ON_STATE }
        val weeklyExerciseMinutes = stravaActivities
            .filter { it.timestamp > (System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L) }
            .sumOf { it.durationMinutes }

        if (weeklyExerciseMinutes > 150 && onStates.size > 10) {
            insights.add(
                createInsight(
                    timestamp = System.currentTimeMillis(),
                    summary = "Buena respuesta a medicación correlacionada con ejercicio semanal",
                    factor = "Ejercicio semanal: ${weeklyExerciseMinutes} min",
                    impact = "POSITIVO",
                    confidence = 0.88f,
                    recommendation = "Continuar con rutina de ejercicio. Óptimo para prolongar estados ON."
                )
            )
        }

        return insights
    }

    private fun createInsight(
        timestamp: Long,
        summary: String,
        factor: String,
        impact: String,
        confidence: Float,
        recommendation: String
    ): HealthInsight {
        return HealthInsight(
            id = "insight_${timestamp}_${factor.hashCode()}",
            timestamp = timestamp,
            summary = summary,
            correlations = listOf(
                CorrelationFactor(factor = factor, impact = impact, confidence = confidence)
            ),
            recommendation = recommendation
        )
    }
}

// Modelos auxiliares para el análisis
data class StravaActivitySummary(
    val id: String,
    val timestamp: Long,
    val endTime: Long,
    val type: String, // Run, Ride, Walk
    val durationMinutes: Int,
    val intensityLevel: String // LOW, MODERATE, HIGH
)

data class SleepSession(
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val qualityScore: Float // 0.0 a 1.0
)

data class NutritionEntry(
    val timestamp: Long,
    val type: NutritionType,
    val calories: Int?,
    val proteinGrams: Int?
)

enum class NutritionType {
    MEAL, SNACK, MEDICATION
}
