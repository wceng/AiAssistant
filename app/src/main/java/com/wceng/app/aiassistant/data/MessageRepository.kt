package com.wceng.app.aiassistant.data

import com.wceng.app.aiassistant.data.source.local.dao2.ChatDao
import com.wceng.app.aiassistant.data.source.local.dao2.PromptDao
import com.wceng.app.aiassistant.data.source.local.model2.ConversationTitleSource
import com.wceng.app.aiassistant.data.source.remote.ChatApi
import com.wceng.app.aiassistant.data.source.remote.model.ChatFinishReason
import com.wceng.app.aiassistant.data.source.remote.model.asNetwork
import com.wceng.app.aiassistant.domain.model.BubbleToMessages
import com.wceng.app.aiassistant.domain.model.MessageStatus
import com.wceng.app.aiassistant.domain.model.asBubbleToMessages
import com.wceng.app.aiassistant.util.Constant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface MessageRepository {
    fun getBubbleToMessages(
        convId: Long,
        isDescending: Boolean = false
    ): Flow<List<BubbleToMessages>>

    fun isMessageActiveInConversation(convId: Long): Flow<Boolean>

    suspend fun sendMessage(
        convId: Long,
        content: String,
        imageUrl: String? = null,
        prompt: String? = null,
        enableWebSearch: Boolean = false
    )

    suspend fun retrySendUserMessage(
        convId: Long,
        currentVersionMessageId: Long,
        newVersionMessageContent: String,
    )

    suspend fun retryResponseAssistantMessage(convId: Long, currentVersionMessageId: Long)

    suspend fun toggleMessage(convId: Long, targetMessageId: Long)

    suspend fun cancelReceiveMessage(convId: Long)
}

class DefaultMessageRepository(
    private val remote: ChatApi,
    private val chatDao: ChatDao,
    private val promptDao: PromptDao
) : MessageRepository {

    override fun getBubbleToMessages(
        convId: Long,
        isDescending: Boolean
    ): Flow<List<BubbleToMessages>> =
        chatDao.getBubbleWithMessagesFlow(convId, isDescending).map {
            it.asBubbleToMessages()
        }

    override fun isMessageActiveInConversation(convId: Long): Flow<Boolean> {
        return chatDao.isMessageActiveInConversation(convId)
    }

    override suspend fun sendMessage(
        convId: Long,
        content: String,
        imageUrl: String?,
        prompt: String?,
        enableWebSearch: Boolean
    ) {
        runCatching {
            if (!chatDao.existConversation(convId)) {
                throw IllegalStateException("要发送消息使用的会话ID不存在")
            }

            updateConversationTimestamp(convId)

            chatDao.continueMessage(
                convId = convId,
                sender = "user",
                status = MessageStatus.NORMAL.value,
                content = content,
                imageUrl = imageUrl
            )

            //创建ai消息占位
            val aiMessageId = chatDao.continueMessage(
                convId = convId,
                sender = "ai",
                status = MessageStatus.LOADING.value,
                content = ""
            ).second

            executeSendMessageRequest(convId, aiMessageId)

            if (chatDao.getConversationTitleSource(convId) == ConversationTitleSource.Default.value) {
                chatDao.updateConversationTitle(convId, content, ConversationTitleSource.Ai.value)
            }
        }
    }

    override suspend fun retrySendUserMessage(
        convId: Long,
        currentVersionMessageId: Long,
        newVersionMessageContent: String
    ) {
        chatDao.branchMessage(
            convId,
            currentVersionMessageId,
            newVersionMessageContent
        )

        updateConversationTimestamp(convId)

        //创建ai消息占位
        val aiMessageId = chatDao.continueMessage(
            convId = convId,
            sender = "ai",
            status = MessageStatus.LOADING.value,
            content = ""
        ).second

        executeSendMessageRequest(convId, aiMessageId)
    }

    override suspend fun retryResponseAssistantMessage(
        convId: Long,
        currentVersionMessageId: Long
    ) {
        updateConversationTimestamp(convId)

        val aiMessageId = chatDao.branchMessage(
            convId,
            currentVersionMessageId,
            "",
            newVersionMessageStatus = MessageStatus.LOADING.value
        ).second

        executeSendMessageRequest(convId, aiMessageId)
    }

    override suspend fun toggleMessage(convId: Long, targetMessageId: Long) {
        updateConversationTimestamp(convId)
        chatDao.changeMessageVersionWith(convId, targetMessageId)
    }

    override suspend fun cancelReceiveMessage(convId: Long) {
        val messageEntity = chatDao.getActiveMessageInConversation(convId)
        messageEntity ?: return
        chatDao.updateMessageStatus(messageEntity.id, MessageStatus.CANCELED.value)
    }

    private suspend fun executeSendMessageRequest(convId: Long, responseMessageId: Long) {
        val historyMessages = chatDao.getCurrentVersionMessages(convId)
            .filterNot {
                it.status == MessageStatus.LOADING.value ||
                        it.status == MessageStatus.FAILED.value
            }
            .map {
                val sender = chatDao.getMessageSender(it.id) ?: ""
                it.asNetwork(sender = sender)
            }
            .filter { it.sender.isNotEmpty() }

        val convPrompt = promptDao.getPromptContentByConvId(convId) ?: Constant.DEFAULT_PROMPT

        var accumulatedContent = ""
        var isFirstChunk = true
        remote.receiveResponseMessageWithFlow(historyMessages, prompt = convPrompt)
            .catch {
                chatDao.updateMessageStatus(responseMessageId, MessageStatus.FAILED.value)
                chatDao.updateMessageContent(responseMessageId, it.message ?: "")
                println("network error: ${it.printStackTrace()}")
            }
            .collect { chatStreamResponse ->
                if (isFirstChunk) {
                    chatDao.updateMessageStatus(responseMessageId, MessageStatus.GENERATING.value)
                    isFirstChunk = false
                }

                chatStreamResponse.deltaContent?.let {
                    accumulatedContent += it
                    //Update local message
                    chatDao.updateMessageContent(
                        responseMessageId,
                        accumulatedContent
                    )
                }

                chatStreamResponse.chatFinishReason?.let {
                    if (it == ChatFinishReason.Stop) {
                        chatDao.updateMessageStatus(responseMessageId, MessageStatus.STOPPED.value)
                    } else {
                        chatDao.updateMessageStatus(responseMessageId, MessageStatus.FAILED.value)
                    }
                }
            }
    }

    private suspend fun updateConversationTimestamp(conversationId: Long) {
        chatDao.updateConversationTime(conversationId)
    }
}
