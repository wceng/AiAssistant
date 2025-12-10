package com.wceng.app.aiassistant.data.source.remote

import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatDelta
import com.aallam.openai.api.chat.Choice
import com.aallam.openai.api.core.FinishReason
import com.aallam.openai.client.OpenAI
import com.wceng.app.aiassistant.BuildConfig
import com.wceng.app.aiassistant.data.model.OpenAiConfig
import com.wceng.app.aiassistant.di.OpenAiProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenAIChatApiTest {

    private val openAiProvider: OpenAiProvider = mockk()
    private val openAI: OpenAI = mockk()

    private val chatApi = OpenAIChatApi(openAiProvider)

    @Test
    fun receiveResponseMessageWithFlow() = runTest {
        val config = OpenAiConfig(model = "gpt-3.5-turbo")
        coEvery { openAiProvider.getConfig() } returns config
        coEvery { openAiProvider.getInstant() } returns openAI

        val responseChunk = ChatCompletionChunk(
            id = "1",
            created = 1,
            model = "gpt-3.5-turbo",
            choices = listOf(
                Choice(
                    index = 0,
                    delta = ChatDelta(content = "response"),
                    finishReason = FinishReason.Stop
                )
            )
        )
        coEvery { openAI.chatCompletions(any()) } returns flowOf(responseChunk)

        val result = chatApi.receiveResponseMessageWithFlow(emptyList(), "prompt").toList()

        assertEquals(1, result.size)
        assertEquals("response", result.first().content)
    }
}
