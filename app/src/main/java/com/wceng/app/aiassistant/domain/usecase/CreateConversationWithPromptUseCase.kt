package com.wceng.app.aiassistant.domain.usecase

import com.wceng.app.aiassistant.data.ChatRepository
import com.wceng.app.aiassistant.data.PromptRepository
import com.wceng.app.aiassistant.util.Constant

class CreateConversationWithPromptUseCase(
    private val chatRepository: ChatRepository,
    private val promptRepository: PromptRepository
) {
    suspend operator fun invoke(promptId: Long): Long {
        val convTitle = promptRepository.getPrompt(promptId)?.title
            ?: Constant.DEFAULT_NEW_CONVERSATION_TITLE
        return chatRepository.createNewConversation(convTitle, promptId)
    }
}