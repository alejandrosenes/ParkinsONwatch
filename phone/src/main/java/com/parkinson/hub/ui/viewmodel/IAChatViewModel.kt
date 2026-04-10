package com.parkinson.hub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkinson.hub.data.repository.ChatHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: String
)

data class IAChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class IAChatViewModel @Inject constructor(
    private val chatHistoryRepository: ChatHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IAChatUiState(
        suggestions = listOf(
            "¿Por qué empeoraron mis síntomas esta semana?",
            "¿Qué ejercicio me ha ayudado más?",
            "Genera mi informe para el neurólogo"
        )
    ))
    val uiState: StateFlow<IAChatUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    init {
        loadChatHistory()
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            try {
                val history = chatHistoryRepository.getChatHistory().first()
                val messages = history.map { msg ->
                    ChatMessage(
                        id = msg.id,
                        content = msg.content,
                        isFromUser = msg.isFromUser,
                        timestamp = msg.timestamp
                    )
                }
                _uiState.update { it.copy(messages = messages) }
            } catch (e: Exception) {
                // No history yet
            }
        }
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        val timeString = LocalTime.now().format(timeFormatter)
        val userMessage = ChatMessage(
            id = System.currentTimeMillis(),
            content = message,
            isFromUser = true,
            timestamp = timeString
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                // Save user message to history
                chatHistoryRepository.saveMessage(
                    content = message,
                    isFromUser = true,
                    timestamp = timeString
                )

                // Send to ParkiBot API
                val response = sendToParkiBot(message)

                val assistantMessage = ChatMessage(
                    id = System.currentTimeMillis() + 1,
                    content = response,
                    isFromUser = false,
                    timestamp = LocalTime.now().format(timeFormatter)
                )

                // Save assistant response to history
                chatHistoryRepository.saveMessage(
                    content = response,
                    isFromUser = false,
                    timestamp = assistantMessage.timestamp
                )

                _uiState.update { state ->
                    state.copy(
                        messages = state.messages + assistantMessage,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = "Error al conectar con ParkiBot: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun sendToParkiBot(message: String): String {
        return try {
            // Simulate API call to parkibot.com
            // In production, this would be an actual HTTP request
            delay(2000)

            // Generate contextual response based on user message
            val response = generateParkiBotResponse(message)
            response
        } catch (e: Exception) {
            "Lo siento, no pude conectar con ParkiBot en este momento. Por favor, inténtalo de nuevo más tarde."
        }
    }

    private fun generateParkiBotResponse(message: String): String {
        val lowerMessage = message.lowercase()

        return when {
            lowerMessage.contains("ejercicio") || lowerMessage.contains("ejercicios") ->
                "Basándome en tus registros de actividad, los ejercicios que mejor te han sentado son:\n\n• Tai Chi - Excelente para el equilibrio y la movilidad\n• Caminar - Actividad aeróbica suave que ayuda con la rigidez\n• Estiramientos - Mejora la flexibilidad muscular\n\nTe recomiendo realizar Tai Chi 3 veces por semana durante 20-30 minutos.\n\n¿Deseas que te genere una rutina personalizada?"

            lowerMessage.contains("informe") || lowerMessage.contains("neurólogo") || lowerMessage.contains("reporte") ->
                "Voy a generar tu informe para el neurólogo basado en tus datos de las últimas semanas:\n\n📊 RESUMEN PARKINS ON WATCH\n\n• Promedio TPI: 3.2 (leve)\n• FC promedio: 72 bpm (normal)\n• Calidad del sueño: 78% (buena)\n• Ejercicio semanal: 4.5 horas\n• Adherencia medicación: 95%\n\nEl informe está listo. ¿Quieres que lo exporte en PDF?"

            lowerMessage.contains("síntomas") || lowerMessage.contains("empeor") ->
                "He analizado tus datos de las últimas semanas y encontré que tus síntomas pueden estar relacionados con:\n\n• Variaciones en la medicación\n• Calidad del sueño (registré 2 noches con sueño fragmentado)\n• Estrés elevado según tus registros\n\nTe recomiendo:\n1. Revisar si tomaste las dosis a tiempo\n2. Mantener un horario de sueño regular\n3. Practicar técnicas de relajación\n\n¿Hay algo específico que quieras consultar?"

            lowerMessage.contains("medicación") || lowerMessage.contains("medicamento") ->
                "Según tus registros:\n\n• Levodopa: Último toma hace 4 horas\n• Siguiente dosis programada: en 4 horas\n• Adherencia esta semana: 95%\n\n⚠️ Recordatorio: Es importante tomar la medicación 30 minutos antes de las comidas para mejor absorción.\n\n¿Tienes alguna pregunta sobre tu medicación?"

            lowerMessage.contains("sueño") ->
                "Tu patrón de sueño muestra:\n\n• Horas dormidas promedio: 6.5h\n• Eficiencia: 75%\n• Problemas detectados: 3 noches con despertares\n\nLas noches con peor calidad coinciden con días de mayor estrés. Te recomiendo:\n• Mantener horario regular\n• Evitar pantallas 1h antes de dormir\n• Temperatura ambiente fresca (18-20°C)\n\n¿Deseas más consejos para mejorar tu sueño?"

            lowerMessage.contains("hola") || lowerMessage.contains("buenos días") ->
                "¡Hola! Soy tu Coach IA, estoy aquí para ayudarte con:\n\n📋 Gestion de sintomas\n🏃 Recomendaciones de ejercicio\n💊 Seguimiento de medicación\n😴 Analisis del sueño\n📊 Informes para tu neurólogo\n\n¿En qué puedo ayudarte hoy?"

            else ->
                "Gracias por tu mensaje. He registrado tu consulta.\n\nBasándome en tus datos de Parkins ON Watch, puedo ofrecerte información personalizada sobre tu salud.\n\nAlgunas sugerencias:\n• '¿Cómo están mis síntomas esta semana?'\n• '¿Qué ejercicio me recomienda?'\n• 'Genera mi informe mensual'\n\n¿En qué más puedo ayudarte?"
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatHistoryRepository.clearHistory()
            _uiState.update { it.copy(messages = emptyList()) }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
