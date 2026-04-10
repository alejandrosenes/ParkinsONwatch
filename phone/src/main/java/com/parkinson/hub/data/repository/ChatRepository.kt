package com.parkinson.hub.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor() {

    suspend fun sendMessage(message: String): String {
        return when {
            message.contains("empeoraron", ignoreCase = true) -> 
                "Basándome en tus registros, identifico varios factores que pueden haber contribuido al empeoramiento de síntomas esta semana:\n\n• **Sueño:** La eficiencia de sueño del lunes fue solo del 65%, significativamente menor que tu media del 82%.\n\n• **Ejercicio:** Solo realizaste 3 sesiones de ejercicio esta semana, cuando tu media es de 5-6 sesiones.\n\n• **Medicación:** Detecté que tomaste levodopa con comida alta en proteína el martes, lo cual puede reducir su absorción hasta en un 50%.\n\n**Recomendaciones:**\n1. Intenta mejorar la higiene del sueño\n2. Mantén al menos 30 min de separación entre proteína y medicación\n3. Considera añadir una sesión corta de LSVT BIG aunque sea de 15 min"
            
            message.contains("ejercicio", ignoreCase = true) ->
                "Según tu historial de los últimos 3 meses, los ejercicios que mejor han respondido a tus síntomas son:\n\n🥇 **1. Nordic Walking (45 min)** - Reducción promedio del temblor: 25%\n   Mejor momento: Mañana, tras medicación\n\n🥈 **2. LSVT BIG (30 min)** - Reducción promedio: 22%\n   Mejor momento: Antes de medicación para máxima activación\n\n🥉 **3. Tai Chi (40 min)** - Reducción promedio: 18%\n  特别好 para equilibrio y rigidez\n\n**Consejo:** La consistencia importa más que la intensidad. 5 sesiones cortas superan a 2 largas."
            
            message.contains("informe", ignoreCase = true) || message.contains("neurólogo", ignoreCase = true) ->
                "Voy a generar tu informe para la consulta neurológica. Incluirá:\n\n📊 **Resumen de 4 semanas:**\n• IDS promedio: 68/100 (tendencia estable)\n• Temblor TPI: 3.4 (rango 2.1 - 5.2)\n• Episodios FOG: 3 (vs 5 en periodo anterior)\n• Calidad de sueño: 80% eficiencia\n\n💊 **Medicación:**\n• Adherencia: 94%\n• Síntomas OFF: concentrados a las 16:00-18:00\n\n📈 **Correlaciones detectadas:**\n• Buena calidad de sueño → 20% mejoría en síntomas diurnos\n• Ejercicio aeróbico > 30 min → reducción significativa de rigidez matutina\n\n¿Deseas que añada alguna observación específica o exporte el PDF?"
            
            message.contains("correlación", ignoreCase = true) || message.contains("FOG", ignoreCase = true) ->
                "He analizado la correlación entre tu sueño y los episodios de FOG:\n\n🔍 **Hallazgo principal:**\nExiste una correlación moderada (r=0.65) entre noches con eficiencia de sueño <75% y episodios de FOG al día siguiente.\n\n📊 **Datos específicos:**\n• De 4 noches con eficiencia <75%, 3 fueron seguidas de episodios FOG\n• La latencia de sueño >30 min también muestra correlación\n\n💡 **Hipótesis:** La privación de sueño puede exacerbar la disfunción de ganglios basales, aumentando la probabilidad de FOG.\n\n**Recomendación:** Priorizar higiene del sueño podría reducir episodios de FOG."
            
            else ->
                "Gracias por tu mensaje. Para darte la mejor información, ¿podrías ser más específico?\n\nPuedo ayudarte con:\n• 📊 Análisis de tendencias de síntomas\n• 💊 Optimización de horarios de medicación\n• 🏃 Recomendaciones de ejercicio personalizadas\n• 😴 Correlación entre sueño y síntomas\n• 📋 Generación de informes para tu neurólogo\n\n¿En qué tema específico te gustaría profundizar?"
        }
    }
}
