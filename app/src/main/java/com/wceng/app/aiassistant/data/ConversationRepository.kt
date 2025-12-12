package com.wceng.app.aiassistant.data

import com.wceng.app.aiassistant.data.source.local.dao2.ChatDao
import com.wceng.app.aiassistant.data.source.local.dao2.PromptDao
import com.wceng.app.aiassistant.data.source.local.model2.ConversationEntity
import com.wceng.app.aiassistant.data.source.local.model2.ConversationTitleSource
import com.wceng.app.aiassistant.domain.model.Conversation
import com.wceng.app.aiassistant.domain.model.ConversationWithPromptInfo
import com.wceng.app.aiassistant.domain.model.asConversationWithPromptInfo
import com.wceng.app.aiassistant.domain.model.asExternalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ConversationRepository {
    fun getMostActiveConversations(): Flow<List<Conversation>>
    fun getConversationFlow(convId: Long): Flow<Conversation?>
    fun getConversationWithPromptFlow(convId: Long): Flow<ConversationWithPromptInfo?>
    suspend fun createNewConversation(
        title: String,
        promptId: Long? = null,
        titleSource: ConversationTitleSource = ConversationTitleSource.Default
    ): Long

    suspend fun deleteConversation(id: Long)
    suspend fun deleteConversations(ids: Set<Long>)
    suspend fun getConversation(id: Long): Conversation?
    suspend fun updateConversationTitle(
        id: Long,
        newTitle: String,
        titleSource: ConversationTitleSource
    )
    
    suspend fun clearAllBubbleAndMessages(conversationId: Long)
}

class DefaultConversationRepository(
    private val chatDao: ChatDao,
    private val promptDao: PromptDao
) : ConversationRepository {

    override fun getMostActiveConversations(): Flow<List<Conversation>> =
        chatDao.getConversationsByLastUpdated().map {
            it.map(ConversationEntity::asExternalModel)
        }

    override fun getConversationFlow(convId: Long): Flow<Conversation?> {
        return chatDao.getConversationFlowById(convId)
            .map { it?.let(ConversationEntity::asExternalModel) }
    }

    override fun getConversationWithPromptFlow(convId: Long): Flow<ConversationWithPromptInfo?> {
        return chatDao.getConversationWithPromptFlow(convId)
            .map { it?.asConversationWithPromptInfo() }
    }

    override suspend fun createNewConversation(
        title: String,
        promptId: Long?,
        titleSource: ConversationTitleSource
    ): Long {
        return chatDao.insert(
            ConversationEntity(
                title = title, promptId = promptId, titleSource = titleSource.value
            )
        )
    }

    override suspend fun deleteConversation(id: Long) {
        chatDao.deleteConversation(id)
    }

    override suspend fun deleteConversations(ids: Set<Long>) {
        chatDao.deleteConversations(ids)
    }

    override suspend fun getConversation(id: Long): Conversation? =
        chatDao.getConversationById(id)?.asExternalModel()

    override suspend fun updateConversationTitle(
        id: Long,
        newTitle: String,
        titleSource: ConversationTitleSource
    ) {
        updateConversationTimestamp(id)
        chatDao.updateConversationTitle(id, newTitle, titleSource.value)
    }
    
    override suspend fun clearAllBubbleAndMessages(conversationId: Long) {
        updateConversationTimestamp(conversationId)
        chatDao.clearAllBubble(conversationId)
    }

    private suspend fun updateConversationTimestamp(conversationId: Long) {
        chatDao.updateConversationTime(conversationId)
    }
}
