@file:OptIn(BetaOpenAI::class)

package com.wceng.app.aiassistant

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantRequest
import com.aallam.openai.api.assistant.AssistantTool
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.core.Status
import com.aallam.openai.api.message.MessageContent
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assume
import org.junit.Test

class OpenAITest {

    //证明1+1=2
    @Test
    fun testOnePlusOneEqualsTwo() = runBlocking {
        val sum = 1 + 1
        assertEquals(2, sum)
    }

    companion object {
        private const val POLL_DELAY_MS = 1500L
        private const val MAX_POLL_ATTEMPTS = 40
        private const val RUN_TIMEOUT_MS = 60_000L // 60s
    }

    private fun loadApiKey(): String? =
        System.getenv("OPENAI_API_KEY") ?: System.getProperty("openai.api.key")

    suspend fun runAssistantFlow(apiKey: String, host: String? = null) {

        val openAI = OpenAI(
            token = apiKey,
            host = host?.let { OpenAIHost(baseUrl = it) } ?: OpenAIHost(baseUrl = "https://api.chatanywhere.tech/v1")
        )

        val assistant = openAI.assistant(
            request = AssistantRequest(
                name = "Math Tutor",
                instructions = "You are a personal math tutor. Write and run code to answer math questions.",
                tools = listOf(AssistantTool.CodeInterpreter),
                model = ModelId("gpt-4.1-mini-ca")
            )
        )

        val thread = openAI.thread()

        openAI.message(
            threadId = thread.id,
            request = MessageRequest(
                role = Role.User,
                content = "I need to solve the equation `3x + 11 = 14`. Can you help me?"
            )
        )
        val messages = openAI.messages(thread.id)
        println("List of messages in the thread:")
        for (message in messages) {
            val textContent = message.content.firstOrNull() as? MessageContent.Text
            val textValue = textContent?.text?.value ?: "<non-text content>"
            println(textValue)
        }

        val run = openAI.createRun(
            thread.id,
            request = RunRequest(
                assistantId = assistant.id,
                instructions = "Please address the user as Jane Doe. The user has a premium account.",
            )
        )

        var completed = false
        for (attempt in 1..MAX_POLL_ATTEMPTS) {
            delay(POLL_DELAY_MS)
            val retrievedRun = openAI.getRun(threadId = thread.id, runId = run.id)
            if (retrievedRun.status == Status.Completed) {
                completed = true
                break
            }
        }

        if (!completed) error("Run did not complete within ${MAX_POLL_ATTEMPTS * POLL_DELAY_MS}ms")

        val assistantMessages = openAI.messages(thread.id)
        println("\nThe assistant's response:")
        for (message in assistantMessages) {
            val textContent = message.content.firstOrNull() as? MessageContent.Text
            val textValue = textContent?.text?.value ?: "<non-text content>"
            println(textValue)
        }
    }

    @Test
    fun testAssistantFlow() = runBlocking {
        val apiKey = loadApiKey()
        Assume.assumeTrue("No API key found - skipping integration test", !apiKey.isNullOrBlank())
        val host = System.getenv("OPENAI_API_HOST")
        withTimeout(RUN_TIMEOUT_MS) {
            runAssistantFlow(apiKey!!, host)
        }
    }
}