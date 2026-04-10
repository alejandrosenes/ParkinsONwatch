package com.parkinson.hub.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class ChatMessageEntity(
    val id: Long,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: String
)

@Singleton
class ChatHistoryRepository @Inject constructor() {

    private val messages = mutableListOf<ChatMessageEntity>()

    fun getChatHistory(): Flow<List<ChatMessageEntity>> = flow {
        emit(messages.toList())
    }

    suspend fun saveMessage(content: String, isFromUser: Boolean, timestamp: String) {
        val message = ChatMessageEntity(
            id = System.currentTimeMillis(),
            content = content,
            isFromUser = isFromUser,
            timestamp = timestamp
        )
        messages.add(message)
    }

    suspend fun clearHistory() {
        messages.clear()
    }
}
