package com.parkinson.hub.domain.usecase

import com.parkinson.hub.data.repository.ChatRepository
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(message: String): String {
        return chatRepository.sendMessage(message)
    }
}
