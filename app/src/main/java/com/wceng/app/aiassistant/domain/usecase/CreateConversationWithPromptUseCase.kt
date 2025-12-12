package com.wceng.app.aiassistant.domain.usecase

import com.wceng.app.aiassistant.data.DefaultConversationRepository
import com.wceng.app.aiassistant.data.PromptRepository
import com.wceng.app.aiassistant.data.source.local.model2.ConversationTitleSource
import com.wceng.app.aiassistant.util.Constant

class CreateConversationWithPromptUseCase(
    private val conversationRepository: DefaultConversationRepository,
    private val promptRepository: PromptRepository
) {
    suspend operator fun invoke(promptId: Long): Long {
        val convTitle = promptRepository.getPrompt(promptId)?.title
            ?: Constant.DEFAULT_NEW_CONVERSATION_TITLE
        return conversationRepository.createNewConversation(
            convTitle, promptId, ConversationTitleSource.Prompt
        )
    }
}