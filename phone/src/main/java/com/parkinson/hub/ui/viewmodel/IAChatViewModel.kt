package com.parkinson.hub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkinson.hub.domain.usecase.SendChatMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ChatMessage(
    val id: Long,
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
    private val sendChatMessageUseCase: SendChatMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(IAChatUiState(
        suggestions = listOf(
            "¿Por qué empeoraron mis síntomas esta semana?",
            "¿Qué ejercicio me ha ayudado más?",
            "Genera mi informe para el neurólogo"
        )
    ))
    val uiState: StateFlow<IAChatUiState> = _uiState.asStateFlow()

    fun sendMessage(message: String) {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val userMessage = ChatMessage(
            id = System.currentTimeMillis(),
            content = message,
            isFromUser = true,
            timestamp = LocalTime.now().format(timeFormatter)
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                delay(1500)

                val response = sendChatMessageUseCase(message)

                val assistantMessage = ChatMessage(
                    id = System.currentTimeMillis() + 1,
                    content = response,
                    isFromUser = false,
                    timestamp = LocalTime.now().format(timeFormatter)
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
                        error = e.message
                    )
                }
            }
        }
    }
}
