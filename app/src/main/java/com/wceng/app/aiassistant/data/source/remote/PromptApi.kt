package com.wceng.app.aiassistant.data.source.remote

import com.wceng.app.aiassistant.data.source.remote.model.NetworkPrompt
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.utils.io.*

interface PromptApi {
    suspend fun getPrompts(): List<NetworkPrompt>
}

class KtorPromptApi(private val client: HttpClient) : PromptApi {
    companion object {
        private const val API_URL =
            "https://raw.githubusercontent.com/PlexPt/awesome-chatgpt-prompts-zh/refs/heads/main/prompts-zh.json"
    }

    override suspend fun getPrompts(): List<NetworkPrompt> {
        return try {
            client.get(API_URL).body()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()

            emptyList()
        }
    }
}