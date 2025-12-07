package com.wceng.app.aiassistant.data.source.local.model2

import androidx.room.Embedded
import androidx.room.Relation

class BubbleWithMessages {

    @Embedded
    lateinit var bubbleEntity: BubbleEntity

    @Relation(
        parentColumn = "id",
        entityColumn = "bubble_id"
    )
    lateinit var messages: List<MessageEntity>

    @Embedded(prefix = "current_version_message_")
    var currentVersionMessage: MessageEntity? = null

    override fun toString(): String {
        return bubbleEntity.toString() + "\n" +
                messages.toString() + "\n" +
                currentVersionMessage.toString() + "\n\n"
    }
}