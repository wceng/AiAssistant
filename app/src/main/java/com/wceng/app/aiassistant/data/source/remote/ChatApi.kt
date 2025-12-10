package com.wceng.app.aiassistant.data.source.remote

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ContentPart
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.core.FinishReason
import com.aallam.openai.api.model.ModelId
import com.wceng.app.aiassistant.data.source.remote.model.ChatFinishReason
import com.wceng.app.aiassistant.data.source.remote.model.ChatStreamResponse
import com.wceng.app.aiassistant.data.source.remote.model.NetworkMessage
import com.wceng.app.aiassistant.di.OpenAiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ChatApi {
    /**
     * @param history 携带的历史聊天
     * @return 以流式接受ai响应
     */
    suspend fun receiveResponseMessageWithFlow(
        history: List<NetworkMessage>,
        prompt: String? = null,
    ): Flow<ChatStreamResponse>

    suspend fun listModes(): Result<List<String>>
}

class OpenAIChatApi(
    private val openAiProvider: OpenAiProvider
) : ChatApi {

    private suspend fun buildRequest(
        history: List<NetworkMessage>,
        prompt: String?
    ): ChatCompletionRequest {
        val config = openAiProvider.getConfig()
        val chatMessages = ArrayList<ChatMessage>()

        prompt?.let {
            chatMessages.add(
                ChatMessage(
                    role = ChatRole.System,
                    content = it
                )
            )
        }

        chatMessages.addAll(
            history.map { networkMessage ->
                networkMessage.asOpenAIMessage()
            }
        )

        return ChatCompletionRequest(
            model = ModelId(config.model),
            messages = chatMessages
        )
    }

    override suspend fun receiveResponseMessageWithFlow(
        history: List<NetworkMessage>,
        prompt: String?
    ): Flow<ChatStreamResponse> {
        val openAI = openAiProvider.getInstant()
        val request = buildRequest(history, prompt)

//        println("Request: model: ${request.model} historyNumber: ${request.messages.size} messages: ${request.messages.map { it.content }}")

        return openAI.chatCompletions(request).map { chunk ->
            val content = chunk.choices.firstOrNull()?.delta?.content
            val finishReason = chunk.choices.firstOrNull()?.finishReason
            ChatStreamResponse(content, finishReason?.asChatFinishReason())
        }
    }

    private fun FinishReason.asChatFinishReason(): ChatFinishReason = when (this) {
        FinishReason.Stop -> ChatFinishReason.Stop
        FinishReason.Length -> ChatFinishReason.Length
        FinishReason.FunctionCall -> ChatFinishReason.FunctionCall
        FinishReason.ToolCalls -> ChatFinishReason.ToolCalls
        FinishReason.ContentFilter -> ChatFinishReason.ContentFilter
        else -> ChatFinishReason.UnKnown
    }

    private fun NetworkMessage.asOpenAIMessage(): ChatMessage {
        val contentParts = mutableListOf<ContentPart>(TextPart(this.content))
        this.imageUrl?.let { url ->
            contentParts.add(ImagePart(url = url))
        }
        return ChatMessage(
            role = when (this.sender) {
                "user" -> ChatRole.User
                "ai" -> ChatRole.Assistant
                else -> {
                    throw IllegalArgumentException("不认识的sender")
                }
            },
            content = contentParts
        )
    }

    override suspend fun listModes(): Result<List<String>> =
        runCatching {
            val openAI = openAiProvider.getInstant()
            openAI.models().map {
                it.id.id
            }
        }
}