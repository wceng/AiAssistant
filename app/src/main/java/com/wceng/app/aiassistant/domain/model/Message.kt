package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable
import com.wceng.app.aiassistant.data.source.local.model2.MessageEntity

@Immutable
data class Message(
    val id: Long = 0,
    val content: String = "",
    //Base64 String
    val imageUrl: String? = null,
    val status: MessageStatus = MessageStatus.NORMAL,
)

fun MessageEntity.asExternalModel() = Message(
    id = id,
    content = content,
    imageUrl = imageUrl,
    status = MessageStatus.Companion.fromInt(status),
)

fun List<MessageEntity>.asExternalModels() = map { it.asExternalModel() }
