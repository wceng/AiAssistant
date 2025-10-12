@file:OptIn(ExperimentalCoroutinesApi::class)

package com.wceng.app.aiassistant.di

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.RetryStrategy
import com.wceng.app.aiassistant.data.source.datastore.UserSettingsDataSource
import com.wceng.app.aiassistant.data.source.local.dao2.AiProviderDao
import com.wceng.app.aiassistant.domain.model.ChatConfig
import com.wceng.app.aiassistant.domain.model.asChatConfig
import com.wceng.app.aiassistant.domain.model.asExternalModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class OpenAiProvider constructor(
    private val userSettingsDataSource: UserSettingsDataSource,
    private val aiProviderDao: AiProviderDao
) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            getChatConfigFow().collect(::updateConfig)
        }
    }

    private fun getChatConfigFow() =
        userSettingsDataSource.useSetting
            .map { it.selectedAiProviderId }
            .flatMapLatest {
                val selectedAiProviderIdOrNull = it ?: aiProviderDao.getAllProviders().first().firstOrNull()?.id
                selectedAiProviderIdOrNull?.let{selectedAiProviderId ->
                    aiProviderDao.getProviderById(selectedAiProviderId)
                } ?: flowOf(null)
            }
            .distinctUntilChanged()
            .filterNotNull()
            .map { it.asExternalModel().asChatConfig() }


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

        val config = getChatConfig()
        create(config)
        return openAI!!
    }

    suspend fun getConfig(): ChatConfig =
        if (cacheConfig == null) {
            getChatConfig()
        } else {
            cacheConfig!!
        }

    private suspend fun getChatConfig() = getChatConfigFow().first()

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