package com.wceng.app.aiassistant.data.source.datastore

import androidx.datastore.core.DataStore
import com.wceng.app.aiassistant.domain.model.AiProviderConfigInfo
import com.wceng.app.aiassistant.domain.model.AiProviderInfo
import com.wceng.app.aiassistant.domain.model.DEFAULT_AI_PROVIDER_CONFIG_INFO
import com.wceng.app.aiassistant.domain.model.DarkModeInfo
import com.wceng.app.aiassistant.domain.model.ThemeSchemeInfo
import com.wceng.app.aiassistant.domain.model.UserSettingInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UserSettingsDataSource(
    private val datastore: DataStore<UserPreferences>,
) {

    //init default data
    init {
        CoroutineScope(Dispatchers.IO).launch {
            datastore.data.first()
                .apply {
                    if (selectedAiProviderName.isEmpty() && aiProvidersMap.isEmpty()) {
                        datastore.updateData { preferences ->
                            val initAiProviders = DEFAULT_AI_PROVIDER_CONFIG_INFO.aiProviderInfos
                                .map { it.asProto() }
                                .associateBy { it.name }

                            preferences.toBuilder()
                                .setSelectedAiProviderName(DEFAULT_AI_PROVIDER_CONFIG_INFO.selectedAiProviderName)
                                .putAllAiProviders(initAiProviders)
                                .build()
                        }
                    }
                }
        }
    }

    val useSetting: Flow<UserSettingInfo> = datastore.data
        .map { preferences ->
            val selectedAiProviderName = preferences.selectedAiProviderName
                .ifEmpty { DEFAULT_AI_PROVIDER_CONFIG_INFO.selectedAiProviderName }

            val aiProviderInfos = preferences.aiProvidersMap.values.toList()
                .map { it.asModel() }
                .let {
                    it.ifEmpty { DEFAULT_AI_PROVIDER_CONFIG_INFO.aiProviderInfos }
                }

            UserSettingInfo(
                aiProviderConfigInfo = AiProviderConfigInfo(
                    selectedAiProviderName = selectedAiProviderName,
                    aiProviderInfos = aiProviderInfos
                ),
                themeSchemeInfo = when (preferences.themeScheme) {
                    ThemeScheme.THEME_UNSPECIFIED,
                    ThemeScheme.DEFAULT,
                    ThemeScheme.UNRECOGNIZED -> ThemeSchemeInfo.Default

                    ThemeScheme.DYNAMIC -> ThemeSchemeInfo.Dynamic
                },
                darkModeInfo = when (preferences.darkMode) {
                    DarkMode.SYSTEM,
                    DarkMode.UNRECOGNIZED,
                    DarkMode.DARK_MODE_UNSPECIFIED -> DarkModeInfo.System

                    DarkMode.LIGHT -> DarkModeInfo.Light
                    DarkMode.DARK -> DarkModeInfo.Dark
                }
            )
        }

    suspend fun setSelectedAiProvider(aiProviderName: String) {
        datastore.updateData { userPreferences ->
            userPreferences.toBuilder()
                .setSelectedAiProviderName(aiProviderName)
                .build()
        }
    }

    suspend fun setAiProvider(aiProviderInfo: AiProviderInfo) {
        datastore.updateData { userPreferences ->
            userPreferences.toBuilder()
                .putAiProviders(aiProviderInfo.name, aiProviderInfo.asProto())
                .build()
        }
    }

    suspend fun removeAiProvider(providerName: String) {
        datastore.updateData { userPreferences ->
            val build = userPreferences.toBuilder()
            build.removeAiProviders(providerName)

            if (providerName == userPreferences.selectedAiProviderName) {
                build.setSelectedAiProviderName("")
            }

            build.build()
        }
    }

    suspend fun setSelectedAiProviderApiKey(apiKey: String) {
        datastore.updateData { userPreferences ->
            val updatedAiProvider = useSetting.first()
                .aiProviderConfigInfo
                .selectedAiProviderInfo
                .copy(apiKey = apiKey)

            userPreferences.toBuilder()
                .putAiProviders(updatedAiProvider.name, updatedAiProvider.asProto())
                .build()
        }
    }

    suspend fun setSelectedAiProviderBaseUrl(baseUrl: String) {
        datastore.updateData { userPreferences ->
            val updatedAiProvider = useSetting.first()
                .aiProviderConfigInfo
                .selectedAiProviderInfo
                .copy(baseUrl = baseUrl)

            userPreferences.toBuilder()
                .putAiProviders(updatedAiProvider.name, updatedAiProvider.asProto())
                .build()
        }
    }

    suspend fun setSelectedAiProviderSelectedModel(model: String) {
        datastore.updateData { userPreferences ->
            val updatedAiProvider = useSetting.first()
                .aiProviderConfigInfo
                .selectedAiProviderInfo
                .copy(selectedModel = model)

            userPreferences.toBuilder()
                .putAiProviders(updatedAiProvider.name, updatedAiProvider.asProto())
                .build()
        }
    }

    suspend fun setSelectedAiProviderModels(models: List<String>) {
        datastore.updateData { userPreferences ->
            val updatedAiProvider = useSetting.first()
                .aiProviderConfigInfo
                .selectedAiProviderInfo
                .copy(models = models)

            userPreferences.toBuilder()
                .putAiProviders(updatedAiProvider.name, updatedAiProvider.asProto())
                .build()
        }
    }

    suspend fun setThemeScheme(themeSchemeInfo: ThemeSchemeInfo) {
        datastore.updateData { userPreferences ->
            userPreferences
                .toBuilder()
                .setThemeScheme(
                    when (themeSchemeInfo) {
                        ThemeSchemeInfo.Default -> ThemeScheme.DEFAULT
                        ThemeSchemeInfo.Dynamic -> ThemeScheme.DYNAMIC
                    }
                )
                .build()
        }
    }

    suspend fun setDarkModel(darkModeInfo: DarkModeInfo) {
        datastore.updateData { userPreferences ->
            userPreferences.toBuilder()
                .setDarkMode(
                    when (darkModeInfo) {
                        DarkModeInfo.Light -> DarkMode.LIGHT
                        DarkModeInfo.Dark -> DarkMode.DARK
                        DarkModeInfo.System -> DarkMode.SYSTEM
                    }
                )
                .build()
        }
    }

}

private fun AiProvider.asModel() = AiProviderInfo(
    name = name,
    apiKey = apiKey,
    baseUrl = baseUrl,
    models = modelsList,
    selectedModel = selectedModel
)

private fun AiProviderInfo.asProto() = AiProvider.newBuilder()
    .setName(name)
    .setApiKey(apiKey)
    .setBaseUrl(baseUrl)
    .clearModels()
    .addAllModels(models)
    .setSelectedModel(selectedModel)
    .build()