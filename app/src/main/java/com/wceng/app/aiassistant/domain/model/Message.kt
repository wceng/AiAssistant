package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable
import com.wceng.app.aiassistant.data.source.local.model2.MessageEntity

@Immutable
data class Message(
    val id: Long = 0,
    val content: String = "",
    val status: MessageStatus = MessageStatus.NORMAL,
)

fun MessageEntity.asExternalModel() = Message(
    id = id,
    content = content,
    status = MessageStatus.Companion.fromInt(status),
)

fun List<MessageEntity>.asExternalModels() = map { it.asExternalModel() }
