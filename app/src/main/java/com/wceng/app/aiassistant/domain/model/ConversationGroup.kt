package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class ConversationGroup(
    val title: String,
    val conversations: List<Conversation>
)

enum class GroupType {
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    BY_YEAR_MONTH
}