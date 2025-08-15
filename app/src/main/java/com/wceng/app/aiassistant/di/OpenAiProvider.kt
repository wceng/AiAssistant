package com.wceng.app.aiassistant.di

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.RetryStrategy
import com.wceng.app.aiassistant.data.source.datastore.UserSettingsDataSource
import com.wceng.app.aiassistant.domain.model.ChatConfig
import com.wceng.app.aiassistant.domain.model.asChatConfig
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class OpenAiProvider(
    private val userSettingsDataSource: UserSettingsDataSource,
) {

    private var openAI: OpenAI? = null
    private var cacheConfig: ChatConfig? = null

    fun updateConfig(config: ChatConfig) {
        if (cacheConfig == config) {
            return
        }
        create(config)
    }


    suspend fun getInstant(): OpenAI {
        if (openAI != null) {
            return openAI!!
        }

        val config = getUserChatConfig()
        create(config)
        return openAI!!
    }


    suspend fun getConfig(): ChatConfig =
        if (cacheConfig == null) {
            getUserChatConfig()
        } else {
            cacheConfig!!
        }

    private suspend fun getUserChatConfig() =
        userSettingsDataSource.useSetting.first().asChatConfig()

    private fun create(config: ChatConfig) {
        this.openAI = OpenAI(
            OpenAIConfig(
                token = config.apiKey,
                host = OpenAIHost(config.baseUrl),
                timeout = Timeout(request = null, connect = 30.seconds, socket = 2.minutes),
                retry = RetryStrategy(maxRetries = 0)
            )
        )
        this.cacheConfig = config
    }
}