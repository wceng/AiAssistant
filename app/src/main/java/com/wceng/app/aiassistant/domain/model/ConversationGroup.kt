package com.wceng.app.aiassistant.domain.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable


@Immutable
sealed class ConversationGroup {
    abstract val conversations: List<Conversation>

    data class SimpleGroup(
        @StringRes val titleRes: Int,
        override val conversations: List<Conversation>
    ) : ConversationGroup()

    data class YearMonthGroup(
        val year: Int,
        val month: Int,
        override val conversations: List<Conversation>
    ) : ConversationGroup()
}
