package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable
import com.wceng.app.aiassistant.data.source.local.model2.ConversationWithPrompt

@Immutable
data class ConversationWithPromptInfo(
    val conversation: Conversation,
    val prompt: Prompt?
)

fun ConversationWithPrompt.asConversationWithPromptInfo() =
    ConversationWithPromptInfo(
        conversation = conversationEntity.asExternalModel(),
        prompt = promptEntity?.asExternalModel()
    )
