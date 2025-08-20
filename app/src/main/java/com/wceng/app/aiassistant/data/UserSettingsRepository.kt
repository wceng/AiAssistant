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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface UserSettingsRepository {
    val userSettingInfo: Flow<UserSettingInfo>

    suspend fun setSelectedAiProvider(aiProviderName: String)
    suspend fun setSelectedAiProviderSelectedModel(model: String)
    suspend fun setSelectedAiProviderApiKey(apiKey: String)
    suspend fun setSelectedAiProviderBaseUrl(baseUrl: String)
    suspend fun setSelectedAiProviderModels(models: List<String>)
    suspend fun addSelectedAiProviderModel(model: String)
    suspend fun existSelectedAiProviderModel(model: String): Boolean
    suspend fun deleteSelectedAiProviderModel(model: String)
    suspend fun setAiProviderInfo(aiProviderInfo: AiProviderInfo)
    suspend fun existServiceProviderName(name: String): Boolean
    suspend fun addAiProviderInfo(aiProviderInfoName: String)
    suspend fun refreshSelectedAiProviderModels(): Boolean
    suspend fun setThemeScheme(themeSchemeInfo: ThemeSchemeInfo)
    suspend fun setDarkModel(darkModeInfo: DarkModeInfo)
    suspend fun deleteServiceProvider(providerName: String)
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

    override suspend fun setSelectedAiProviderModels(models: List<String>) {
        userSettingsDataSource.setSelectedAiProviderModels(models)
    }

    override suspend fun addSelectedAiProviderModel(model: String) {
        val newModels = userSettingsDataSource.useSetting.first()
            .aiProviderConfigInfo
            .selectedAiProviderInfo
            .models
            .toMutableList()
        newModels.add(model)

        userSettingsDataSource.setSelectedAiProviderModels(newModels)
    }

    override suspend fun existSelectedAiProviderModel(model: String): Boolean {
        return userSettingsDataSource.useSetting.first()
            .aiProviderConfigInfo
            .selectedAiProviderInfo
            .models
            .contains(model)
    }

    override suspend fun deleteSelectedAiProviderModel(model: String) {
        val selectedAiProviderInfo = userSettingsDataSource.useSetting.first()
            .aiProviderConfigInfo
            .selectedAiProviderInfo

        if (selectedAiProviderInfo.selectedModel == model) {
            userSettingsDataSource.setAiProvider(
                selectedAiProviderInfo.copy(selectedModel = "")
            )
        }
        val newModels = selectedAiProviderInfo
            .models
            .toMutableList()
        newModels.remove(model)
        userSettingsDataSource.setSelectedAiProviderModels(newModels)

    }

    override suspend fun setAiProviderInfo(aiProviderInfo: AiProviderInfo) {
        userSettingsDataSource.setAiProvider(aiProviderInfo)
    }

    override suspend fun existServiceProviderName(name: String): Boolean {
        return userSettingsDataSource
            .useSetting
            .first()
            .aiProviderConfigInfo
            .aiProviderInfos
            .firstOrNull { name == it.name }
            ?.let { true } ?: false
    }

    override suspend fun addAiProviderInfo(aiProviderInfoName: String) {
        val aiProviderInfo = AiProviderInfo(
            name = aiProviderInfoName,
            apiKey = "",
            baseUrl = "",
            models = listOf(),
            selectedModel = ""
        )
        setAiProviderInfo(aiProviderInfo)
    }

    override suspend fun setThemeScheme(themeSchemeInfo: ThemeSchemeInfo) {
        userSettingsDataSource.setThemeScheme(themeSchemeInfo)
    }

    override suspend fun setDarkModel(darkModeInfo: DarkModeInfo) {
        userSettingsDataSource.setDarkModel(darkModeInfo)
    }

    override suspend fun deleteServiceProvider(providerName: String) {
        userSettingsDataSource.removeAiProvider(providerName)
    }

    override suspend fun refreshSelectedAiProviderModels(): Boolean {
        chatApi.listModes().onSuccess {
            val newModelsList = userSettingsDataSource
                .useSetting
                .first()
                .aiProviderConfigInfo
                .selectedAiProviderInfo
                .models
                .toMutableList()
            newModelsList.addAll(it)
            userSettingsDataSource.setSelectedAiProviderModels(newModelsList.distinct())
            return true
        }
            .onFailure {
                return false
            }
        return false
    }
}