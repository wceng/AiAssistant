package com.wceng.app.aiassistant.domain.usecase

import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.data.ChatRepository
import com.wceng.app.aiassistant.domain.model.Conversation
import com.wceng.app.aiassistant.domain.model.ConversationGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

class GetGroupedConversationsUseCase(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(): Flow<List<ConversationGroup>> {
        return chatRepository.getLatestConversations()
            .map { conversations ->
                groupConversations(conversations)
            }
    }

    private fun groupConversations(conversations: List<Conversation>): List<ConversationGroup> {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayConversations = mutableListOf<Conversation>()
        val last7DaysConversations = mutableListOf<Conversation>()
        val last30DaysConversations = mutableListOf<Conversation>()
        val otherConversations = mutableListOf<Conversation>()

        conversations.sortedByDescending { it.lastUpdatedTime }.forEach { conv ->
            val convDate = conv.lastUpdatedTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val daysBetween = today.daysUntil(convDate)

            when (daysBetween) {
                0 -> todayConversations.add(conv)
                in -7..-1 -> last7DaysConversations.add(conv)
                in -30..-8 -> last30DaysConversations.add(conv)
                else -> otherConversations.add(conv)
            }
        }

        val result = mutableListOf<ConversationGroup>()

        if (todayConversations.isNotEmpty()) {
            result.add(ConversationGroup.SimpleGroup(
                titleRes = R.string.group_today,
                conversations = todayConversations
            ))
        }
        if (last7DaysConversations.isNotEmpty()) {
            result.add(ConversationGroup.SimpleGroup(
                titleRes = R.string.group_last_7_days,
                conversations = last7DaysConversations
            ))
        }
        if (last30DaysConversations.isNotEmpty()) {
            result.add(ConversationGroup.SimpleGroup(
                titleRes = R.string.group_last_30_days,
                conversations = last30DaysConversations
            ))
        }

        otherConversations
            .groupBy { conv ->
                conv.lastUpdatedTime.toLocalDateTime(TimeZone.currentSystemDefault()).run {
                    Pair(year, monthNumber)
                }
            }
            .entries
            .sortedByDescending { (_, convs) ->
                convs.maxOfOrNull { it.lastUpdatedTime } ?: Instant.DISTANT_PAST
            }
            .forEach { (yearMonth, convs) ->
                result.add(ConversationGroup.YearMonthGroup(
                    year = yearMonth.first,
                    month = yearMonth.second,
                    conversations = convs
                ))
            }

        return result
    }
}