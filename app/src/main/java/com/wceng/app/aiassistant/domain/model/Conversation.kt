package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable
import com.wceng.app.aiassistant.data.source.local.model2.ConversationEntity
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Immutable
data class Conversation(
    val id: Long = 0,
    val title: String = "",
    val createTime: Instant = Clock.System.now(),
    val lastUpdatedTime: Instant = createTime
)

fun ConversationEntity.asExternalModel() = Conversation(
    id = id,
    title = title,
    createTime = createTime,
    lastUpdatedTime = lastUpdatedTime,
)

