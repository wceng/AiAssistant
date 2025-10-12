package com.wceng.app.aiassistant.data.source.local.dao2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wceng.app.aiassistant.data.source.local.model2.BubbleEntity
import com.wceng.app.aiassistant.data.source.local.model2.BubbleWithMessages
import com.wceng.app.aiassistant.data.source.local.model2.ConversationEntity
import com.wceng.app.aiassistant.data.source.local.model2.ConversationWithPrompt
import com.wceng.app.aiassistant.data.source.local.model2.MessageEntity
import com.wceng.app.aiassistant.data.source.local.model2.MessageVersionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(conversationEntity: ConversationEntity): Long

    @Query("select * from conversation where id = :conversationId")
    suspend fun getConversationById(conversationId: Long): ConversationEntity?

    @Query("delete from conversation where id = :conversationId")
    suspend fun deleteConversation(conversationId: Long)

    @Query("delete from conversation where id in (:conversationIds)")
    suspend fun deleteConversations(conversationIds: Set<Long>)

    @Query("select * from conversation where id = :conversationId")
    fun getConversationFlowById(conversationId: Long): Flow<ConversationEntity?>

    @Query("update conversation set title = :newTitle,title_source = :titleSource where id = :conversationId")
    suspend fun updateConversationTitle(
        conversationId: Long,
        newTitle: String,
        titleSource: Int = 0
    )

    @Query("select conversation.title_source from conversation where id = :conversationId")
    suspend fun getConversationTitleSource(conversationId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM conversation WHERE id = :convId)")
    suspend fun existConversation(convId: Long): Boolean

    @Transaction
    @Query("select * from conversation where id = :convId")
    fun getConversationWithPromptFlow(convId: Long): Flow<ConversationWithPrompt?>

    @Transaction
    @Query(
        """
    SELECT conversation.* 
    FROM conversation
    LEFT JOIN bubble ON conversation.id = bubble.conversation_id
    GROUP BY conversation.id
    ORDER BY conversation.last_updated_time DESC
"""
    )
    fun getConversationsByLastUpdated(): Flow<List<ConversationEntity>>

    @Query("UPDATE conversation SET last_updated_time = :updateTime WHERE id = :conversationId")
    suspend fun updateConversationTime(conversationId: Long, updateTime: Instant = Clock.System.now())

    @Transaction
    @Query(
        """
        SELECT COUNT(*) >= 1 
        FROM message 
        INNER JOIN bubble ON bubble.id = message.bubble_id 
        INNER JOIN conversation ON conversation.id = bubble.conversation_id
        WHERE conversation.id = :convId 
        AND message.status IN (1, 4)
        """
    )
    fun isMessageActiveInConversation(convId: Long): Flow<Boolean>

    @Transaction
    @Query(
        """
        SELECT * 
        FROM message 
        INNER JOIN bubble ON bubble.id = message.bubble_id 
        INNER JOIN conversation ON conversation.id = bubble.conversation_id
        WHERE conversation.id = :convId 
        AND message.status IN (1, 4)
        LIMIT 1
        """
    )
    suspend fun getActiveMessageInConversation(convId: Long): MessageEntity?

    @Query(
        """
        update conversation 
        set current_message_version = :newVersion
        where id = :convId
    """
    )
    suspend fun changeMessageVersion(convId: Long, newVersion: Long)

    @Transaction
    @Query(
        """
        update conversation set current_message_version = 
        (select message_version.version from message_version
            where message_version.message_id = :messageId
            order by message_version.last_view_time desc limit 1
        )        
        where id = :convId
    """
    )
    suspend fun changeMessageVersionWith(convId: Long, messageId: Long)

    @Query(
        """
        select conversation.current_message_version from conversation
        where conversation.id = :convId
    """
    )
    suspend fun getCurrentMessageVersion(convId: Long): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bubbleEntity: BubbleEntity): Long

    @Query(
        """
        delete from bubble 
        where bubble.conversation_id = :convId
    """
    )
    suspend fun clearAllBubble(convId: Long)

    @Transaction
    @Query(
        """
    SELECT
        bubble.*,
        message.id AS current_version_message_id,
        message.bubble_id AS current_version_message_bubble_id,
        message.content AS current_version_message_content,
        message.timestamp AS current_version_message_timestamp,
        message.status AS current_version_message_status
    FROM bubble
    INNER JOIN conversation ON conversation.id = bubble.conversation_id
    INNER JOIN message ON message.bubble_id = bubble.id
    INNER JOIN message_version ON
        message_version.message_id = message.id AND
        message_version.version = conversation.current_message_version
    WHERE bubble.conversation_id = :convId
    ORDER BY bubble.timestamp ASC
"""
    )
    suspend fun getBubbleWithMessages(convId: Long): List<BubbleWithMessages>

    @Transaction
    @Query(
        """
    SELECT
        bubble.*,
        message.id AS current_version_message_id,
        message.bubble_id AS current_version_message_bubble_id,
        message.content AS current_version_message_content,
        message.timestamp AS current_version_message_timestamp,
        message.status AS current_version_message_status
    FROM bubble
    INNER JOIN conversation ON conversation.id = bubble.conversation_id
    INNER JOIN message ON message.bubble_id = bubble.id
    INNER JOIN message_version ON
        message_version.message_id = message.id AND
        message_version.version = conversation.current_message_version
    WHERE bubble.conversation_id = :convId
    ORDER BY 
        CASE WHEN :isDescending = 1 THEN bubble.timestamp END DESC,
        CASE WHEN :isDescending = 0 THEN bubble.timestamp END ASC
"""
    )
    fun getBubbleWithMessagesFlow(
        convId: Long,
        isDescending: Boolean = false
    ): Flow<List<BubbleWithMessages>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(messageEntity: MessageEntity): Long

    @Transaction
    @Query(
        """
        select message.* from message 
        inner join message_version on message_version.message_id = message.id
        where message_version.version = :version and message.timestamp < :timestamp
    """
    )
    suspend fun getMessagesOlderThan(version: Long, timestamp: Instant): List<MessageEntity>

    @Transaction
    @Query(
        """
        SELECT message.* FROM message
        INNER JOIN message_version ON message_version.message_id = message.id
        INNER JOIN conversation ON conversation.current_message_version = message_version.version
        INNER JOIN bubble ON bubble.conversation_id = conversation.id and message.bubble_id = bubble.id
        WHERE conversation.id = :convId
        ORDER BY 
            CASE WHEN :isDescending = 0 THEN message.timestamp END ASC, 
            CASE WHEN :isDescending = 1 THEN message.timestamp END DESC
    """
    )
    suspend fun getCurrentVersionMessages(
        convId: Long,
        isDescending: Boolean = false
    ): List<MessageEntity>

    @Query("select * from message where id = :messageId")
    suspend fun getMessage(messageId: Long): MessageEntity?

    @Query("select * from message where bubble_id = :bubbleId")
    suspend fun getMessageByBubbleId(bubbleId: Long): List<MessageEntity>

    @Query(
        """
    SELECT bubble.sender FROM message
    INNER JOIN bubble ON bubble.id = message.bubble_id
    WHERE message.id = :messageId
"""
    )
    suspend fun getMessageSender(messageId: Long): String?

    @Query("update message set status = :status where id = :messageId")
    suspend fun updateMessageStatus(messageId: Long, status: Int)

    @Query("update message set content = :content where id = :messageId")
    suspend fun updateMessageContent(messageId: Long, content: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(messageVersionEntity: MessageVersionEntity)

    @Query("select count(*) from conversation")
    suspend fun countConversation(): Long

    @Query("select count(*) from bubble")
    suspend fun countBubble(): Long

    @Query("select count(*) from message")
    suspend fun countMessage(): Long

    @Query("select count(*) from message_version")
    suspend fun countMessageVersion(): Long

    @Query("SELECT COALESCE(MAX(version), 0) + 1 FROM message_version")
    suspend fun getNewMessageVersion(): Long


    /**@param convId 会话Id
     * @return 新生成的Bubble的id和返回新插入消息的ID
     */
    @Transaction
    suspend fun continueMessage(
        convId: Long,
        sender: String,
        status: Int = 0,
        content: String
    ): Pair<Long, Long> {
        val bubbleId = insert(BubbleEntity(conversationId = convId, sender = sender))
        val currentVersion = getCurrentMessageVersion(convId) ?: return -1L to -1L

        return bubbleId to insert(
            MessageEntity(
                bubbleId = bubbleId,
                content = content,
                status = status
            )
        ).also { messageId ->
            insert(
                MessageVersionEntity(
                    version = currentVersion,
                    messageId = messageId,
                    bubbleId = bubbleId
                )
            )
        }
    }

    /**
     * @param convId 会话Id
     * @param currentVersionMessageId 当前气泡中的正在显示的消息的ID
     * @param newVersionMessageContent 分支新版本的消息的内容
     * @return 新版本号和新版本消息ID
     */
    @Transaction
    suspend fun branchMessage(
        convId: Long,
        currentVersionMessageId: Long,
        newVersionMessageContent: String,
        newVersionMessageStatus: Int = 0,
    ): Pair<Long, Long> {
        val currentVersion = getCurrentMessageVersion(convId) ?: return -1L to -1L
        val newVersion = getNewMessageVersion()
        val currentMessage = getMessage(currentVersionMessageId) ?: return -1L to -1L

        getMessagesOlderThan(currentVersion, currentMessage.timestamp).forEach {
            insert(
                MessageVersionEntity(
                    version = newVersion,
                    messageId = it.id,
                    bubbleId = it.bubbleId
                )
            )
        }

        val messageId = insert(
            MessageEntity(
                bubbleId = currentMessage.bubbleId,
                content = newVersionMessageContent,
                status = newVersionMessageStatus
            )
        ).also {
            insert(
                MessageVersionEntity(
                    version = newVersion,
                    messageId = it,
                    bubbleId = currentMessage.bubbleId
                )
            )
        }

        changeMessageVersion(convId, newVersion)
        return newVersion to messageId
    }
}