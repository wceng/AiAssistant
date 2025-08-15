package com.wceng.app.aiassistant.data

import com.wceng.app.aiassistant.data.source.datastore.UserSettingsDataSource
import com.wceng.app.aiassistant.data.source.remote.ChatApi
import com.wceng.app.aiassistant.di.OpenAiProvider
import com.wceng.app.aiassistant.domain.model.AiProviderInfo
import com.wceng.app.aiassistant.domain.model.DarkModeInfo
import com.wceng.app.aiassistant.domain.model.ThemeSchemeInfo
import com.wceng.app.aiassistant.domain.model.UserSettingInfo
import com.wceng.app.aiassistant.domain.model.asChatConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface UserSettingsRepository {
    val userSettingInfo: Flow<UserSettingInfo>

    suspend fun setSelectedAiProvider(aiProviderName: String)
    suspend fun setSelectedAiProviderSelectedModel(model: String)
    suspend fun setSelectedAiProviderApiKey(apiKey: String)
    suspend fun setSelectedAiProviderBaseUrl(baseUrl: String)
    suspend fun updateAiProviderInfo(aiProviderInfo: AiProviderInfo)
    suspend fun refreshModels(): Boolean
    suspend fun setThemeScheme(themeSchemeInfo: ThemeSchemeInfo)
    suspend fun setDarkModel(darkModeInfo: DarkModeInfo)
}

class DefaultUserSettingsRepository(
    private val userSettingsDataSource: UserSettingsDataSource,
    private val openAiProvider: OpenAiProvider,
    private val chatApi: ChatApi
) : UserSettingsRepository {

    override val userSettingInfo: Flow<UserSettingInfo>
        get() = userSettingsDataSource.useSetting

    init {
        CoroutineScope(Dispatchers.IO).launch {
            userSettingInfo
                .map { it.asChatConfig() }
                .distinctUntilChanged()
                .collect {
                    openAiProvider.updateConfig(it)
                }
        }
    }

    override suspend fun setSelectedAiProvider(aiProviderName: String) {
        userSettingsDataSource.setSelectedAiProvider(aiProviderName)
    }

    override suspend fun setSelectedAiProviderSelectedModel(model: String) {
        userSettingsDataSource.setSelectedAiProviderSelectedModel(model)
    }

    override suspend fun setSelectedAiProviderApiKey(apiKey: String) {
        userSettingsDataSource.setSelectedAiProviderApiKey(apiKey)
    }

    override suspend fun setSelectedAiProviderBaseUrl(baseUrl: String) {
        userSettingsDataSource.setSelectedAiProviderBaseUrl(baseUrl)
    }


    override suspend fun updateAiProviderInfo(aiProviderInfo: AiProviderInfo) {
        userSettingsDataSource.updateAiProvider(aiProviderInfo)
    }

    override suspend fun setThemeScheme(themeSchemeInfo: ThemeSchemeInfo) {
        userSettingsDataSource.setThemeScheme(themeSchemeInfo)
    }

    override suspend fun setDarkModel(darkModeInfo: DarkModeInfo) {
        userSettingsDataSource.setDarkModel(darkModeInfo)
    }


    override suspend fun refreshModels(): Boolean {
        TODO()
    }
}