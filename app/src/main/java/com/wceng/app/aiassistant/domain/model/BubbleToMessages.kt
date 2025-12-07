package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable
import com.wceng.app.aiassistant.data.source.local.model2.BubbleWithMessages

@Immutable
data class BubbleToMessages(
    val bubble: Bubble,
    val messages: List<Message>,
    val currentVersionMessage: Message?
) {
    val hasOnlyOneMessage: Boolean = messages.size == 1 && messages.contains(currentVersionMessage)

    val currentMessageIndex: Int
        get() {
            currentVersionMessage ?: return -1
            return messages.indexOf(currentVersionMessage)
        }

    val totalMessageNumber: Int = messages.size

    val hasPreviousMessage = currentMessageIndex > 0

    val hasNextMessage = currentMessageIndex < totalMessageNumber - 1

    val previousMessage: Message? =
        if (hasPreviousMessage) messages[currentMessageIndex - 1] else null

    val nextMessage: Message? = if (hasNextMessage) messages[currentMessageIndex + 1] else null
}

fun BubbleWithMessages.asBubbleToMessages() = BubbleToMessages(
    bubble = bubbleEntity.asExternalModel(),
    messages = messages.asExternalModels(),
    currentVersionMessage = currentVersionMessage?.asExternalModel()
)

fun List<BubbleWithMessages>.asBubbleToMessages() = map {
    it.asBubbleToMessages()
}