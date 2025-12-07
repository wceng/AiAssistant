package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable
import com.wceng.app.aiassistant.data.source.local.model2.BubbleEntity
import kotlinx.datetime.Instant

@Immutable
data class Bubble(
    val id: Long,
    val sender: Sender,
    val timestamp: Instant,
)

fun BubbleEntity.asExternalModel() = Bubble(
    id = id,
    sender = when (sender) {
        "user" -> Sender.User
        "ai" -> Sender.Ai
        else -> throw IllegalArgumentException("不认识的Sender")
    },
    timestamp = timestamp
)

enum class Sender {
    Ai, User
}