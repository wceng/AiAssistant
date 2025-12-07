package com.wceng.app.aiassistant.data.source.local.model2

import androidx.room.Embedded
import androidx.room.Relation

data class ConversationWithPrompt(
    @Embedded
    val conversationEntity: ConversationEntity,

    @Relation(
        parentColumn = "prompt_id",
        entityColumn = "id"
    )
    val promptEntity: PromptEntity?
)
