package com.wceng.app.aiassistant

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.wceng.app.aiassistant.data.source.local.ChatDatabase
import com.wceng.app.aiassistant.data.source.local.dao2.ChatDao
import com.wceng.app.aiassistant.data.source.local.model2.ConversationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ChatTest {
    val conv = ConversationEntity(
        title = "new title",
    )

    val conv2 = ConversationEntity(
        title = "new title2",
    )

    private fun getDao(): ChatDao {
        val context = InstrumentationRegistry.getInstrumentation().context
        val db = Room.inMemoryDatabaseBuilder(
            context,
            ChatDatabase::class.java,
        ).build()
        return db.chatDao()
    }

    @Test
    fun testBranchMessage() = runTest {
        val dao = getDao()
        // 1. 创建新对话
        val convId = dao.insert(conv)

        //分支之前， 消息版本为1
        val version1 = dao.getConversationById(convId)?.currentMessageVersion!!
        assertThat(version1).isEqualTo(1)

        // 2. 用户发送消息
        val userMessage = "Hello AI!"
        val (userBubbleId, userMessageId) =
            dao.continueMessage(convId = convId, content = userMessage, sender = "user")

        // 3. AI回复消息 (模拟AI响应)
        val aiResponse = "Hi human! How can I help you?"
        dao.continueMessage(convId = convId, content = aiResponse, sender = "ai")

        //用户对第一条消息编辑重新发送
        val version2 = dao.branchMessage(
            convId = convId,
            newVersionMessageContent = "Hello AI Again!",
            currentVersionMessageId = dao.getMessage(userMessageId)?.id!!,
        )

        val bubbleWithMessagesVersion2First = dao.getBwm(convId)
        assertThat(dao.getConversationById(convId)?.currentMessageVersion).isEqualTo(2)

        assertThat(bubbleWithMessagesVersion2First).isNotEmpty()
        assertThat(bubbleWithMessagesVersion2First).hasSize(1)
        assertThat(bubbleWithMessagesVersion2First.first().messages).hasSize(2)
        assertThat(bubbleWithMessagesVersion2First.first().messages[0].content).isEqualTo("Hello AI!")
        assertThat(bubbleWithMessagesVersion2First.first().messages[1].content).isEqualTo("Hello AI Again!")
        assertThat(bubbleWithMessagesVersion2First.first().currentVersionMessage?.content).isEqualTo(
            "Hello AI Again!"
        )

        //在版本2的ai回复
        val aiResponseAgain = "Hi human! How can I help you again?"
        val (aiBubbleId2, aiMessageId2) =
            dao.continueMessage(convId = convId, content = aiResponseAgain, sender = "ai")

        val bubbleWithMessagesVersion2Second = dao.getBwm(convId)
        assertThat(bubbleWithMessagesVersion2Second).isNotEmpty()
        assertThat(bubbleWithMessagesVersion2Second).hasSize(2)
        assertThat(bubbleWithMessagesVersion2Second.first().messages).hasSize(2)
        assertThat(bubbleWithMessagesVersion2Second.first().messages.map { it.id }).isEqualTo(
            listOf(
                1L,
                3L
            )
        )
        assertThat(bubbleWithMessagesVersion2Second.last().messages).hasSize(1)
        assertThat(bubbleWithMessagesVersion2Second.last().messages[0].id).isEqualTo(4L)
        assertThat(bubbleWithMessagesVersion2Second.last().currentVersionMessage?.content).isEqualTo(
            aiResponseAgain
        )

        //让ai重新回答
        val aiResponseAgainAndAgain = "Hi human! How can I help you again and again?"
        val version3 = dao.branchMessage(
            convId = convId,
            currentVersionMessageId = dao.getMessage(aiMessageId2)?.id!!,
            newVersionMessageContent = aiResponseAgainAndAgain
        )

        assertThat(dao.getConversationById(convId)?.currentMessageVersion).isEqualTo(3)
        val bubbleWithMessagesVersion3 = dao.getBwm(convId)
        assertThat(bubbleWithMessagesVersion3).isNotEmpty()
        assertThat(bubbleWithMessagesVersion3.size).isEqualTo(2)

        assertThat(bubbleWithMessagesVersion3.last().messages.size).isEqualTo(2)
        assertThat(bubbleWithMessagesVersion3.last().messages.map { it.id }).isEqualTo(
            listOf(
                4L,
                5L
            )
        )
        assertThat(bubbleWithMessagesVersion3.last().currentVersionMessage?.content).isEqualTo(
            aiResponseAgainAndAgain
        )

        //用户发送消息
        val (userBubbleId3, userMessageId3) = dao.continueMessage(
            convId = convId,
            "user",
            content = "What are you doing?"
        )

        assertThat(dao.getConversationById(convId)?.currentMessageVersion).isEqualTo(3)
        val bubbleWithMessagesVersion3Second = dao.getBwm(convId)
        assertThat(bubbleWithMessagesVersion3Second).isNotEmpty()
        assertThat(bubbleWithMessagesVersion3Second.size).isEqualTo(3)
        assertThat(bubbleWithMessagesVersion3Second.last().messages.size).isEqualTo(1)
        assertThat(bubbleWithMessagesVersion3Second.last().currentVersionMessage?.content).isEqualTo(
            "What are you doing?"
        )
        assertThat(bubbleWithMessagesVersion3Second.map { it.bubbleEntity.sender }).isEqualTo(
            listOf(
                "user",
                "ai",
                "user"
            )
        )

        //用户切换第一个气泡到第一个版本
//        dao.updateMessageVersion(convId, 1)
        dao.changeMessageVersionWith(convId, userMessageId)
        assertThat(dao.getBwm(convId)).hasSize(2)
        assertThat(dao.getBwm(convId)[0].messages.size).isEqualTo(2)
        assertThat(dao.getBwm(convId)[0].messages[0]).isEqualTo(
            dao.getBwm(convId)[0].currentVersionMessage
        )
        assertThat(dao.getBwm(convId)[1].messages).hasSize(1)
        assertThat(dao.getBwm(convId)[1].messages.first().content).isEqualTo(aiResponse)
    }

    @Test
    fun test() = runTest {
        val dao = getDao()
        val convId = dao.insert(conv)

        suspend fun cv() = dao.getCurrentMessageVersion(convId)!!

        "A1".let { content ->
            dao.continueMessage(
                convId = convId,
                sender = "user",
                content = content
            )
            assertThat(dao.getBwm(convId).first().messages.first().content).isEqualTo(content)
        }

        "B1".let { content ->
            dao.continueMessage(
                convId = convId,
                sender = "ai",
                content = content
            )
            assertThat(dao.getBwm(convId)[1].messages.first().content).isEqualTo(content)
        }


        "B2".let { content ->
            dao.branchMessage(
                convId,
                currentVersionMessageId = 2,
                newVersionMessageContent = content
            )
            assertThat(dao.getBwm(convId)[1].messages[1].content).isEqualTo(content)
        }

        "A2".let { content ->
            dao.branchMessage(
                convId = convId,
                currentVersionMessageId = 1,
                newVersionMessageContent = content
            )
            assertThat(dao.getBwm(convId)[0].messages[1].content).isEqualTo(content)
        }

        "B3".let { content ->
            dao.continueMessage(
                convId = convId,
                sender = "ai",
                content = content
            )

            assertThat(dao.getBwm(convId)[1].messages[0].content).isEqualTo(content)
        }

        dao.changeMessageVersionWith(convId, 1)

        assertThat(dao.getBwm(convId)[1].messages.map { it.id }).isEqualTo(listOf(2L, 3L))
        assertThat(dao.getBwm(convId)[1].currentVersionMessage?.id).isEqualTo(3L)

        dao.deleteConversation(convId)

        assertThat(dao.getBwm(convId)).hasSize(0)

        assertThat(dao.countConversation()).isEqualTo(0L)
        assertThat(dao.countBubble()).isEqualTo(0L)
        assertThat(dao.countMessage()).isEqualTo(0L)
        assertThat(dao.countMessageVersion()).isEqualTo(0L)
    }

    @Test
    fun getCurrentVersionMessagesByConvIdTest() = runTest {
        val dao = getDao()
        val convId = dao.insert(conv)
        //MessageID = 1
        "A1".let { content ->
            dao.continueMessage(
                convId = convId,
                sender = "user",
                content = content
            )
        }

        //MessageID = 2
        "B1".let { content ->
            dao.continueMessage(
                convId = convId,
                sender = "ai",
                content = content
            )
        }

        //MessageID = 3
        "B2".let { content ->
            dao.branchMessage(
                convId = convId,
                currentVersionMessageId = 2,
                newVersionMessageContent = content
            )
        }

        val bwm = dao.getBwm(convId)
        assertThat(bwm).hasSize(2)
        assertThat(bwm.first().messages.first().id).isEqualTo(1L)
        assertThat(bwm.last().messages.first().id).isEqualTo(2L)
        assertThat(bwm.last().currentVersionMessage?.id).isEqualTo(3L)

        assertThat(dao.getCurrentVersionMessages(convId).map { it.id }).isEqualTo(listOf(1L, 3L))
        assertThat(dao.getCurrentVersionMessages(convId, true).map { it.id }).isEqualTo(
            listOf(
                3L,
                1L
            )
        )

        //另一个会话的对话
        val convId2 = dao.insert(conv2)
        //MessageID = 4
        "A1".let { content ->
            dao.continueMessage(
                convId = convId2,
                sender = "user",
                content = content
            )
        }

        //MessageID = 5
        "B1".let { content ->
            dao.continueMessage(
                convId = convId2,
                sender = "ai",
                content = content
            )
        }

        assertThat(dao.getCurrentVersionMessages(convId2).map { it.id }).isEqualTo(listOf(4L, 5L))

    }
}

suspend fun ChatDao.getBwm(convId: Long) = getBubbleWithMessagesFlow(convId).first()
