package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class UserSettingInfo(
    val aiProviderConfigInfo: AiProviderConfigInfo,
    val themeSchemeInfo: ThemeSchemeInfo,
    val darkModeInfo: DarkModeInfo
)

@Immutable
data class AiProviderConfigInfo(
    val selectedAiProviderName: String,
    val aiProviderInfos: List<AiProviderInfo>,
) {
    val selectedAiProviderInfo = aiProviderInfos.first {
        it.name == selectedAiProviderName
    }
}

@Immutable
data class AiProviderInfo(
    val name: String,
    val apiKey: String,
    val baseUrl: String,
    val models: List<String>,
    val selectedModel: String
)

val defaultAiProviders = listOf(
    AiProviderInfo(
        name = "OpenAI",
        apiKey = "",
        baseUrl = "https://api.openai.com/v1",
        models = listOf("gpt-3.5-turbo", "gpt-4"),
        selectedModel = "gpt-3.5-turbo"
    ),
    AiProviderInfo(
        name = "DeepSeek",
        apiKey = "sk-d2b158ec85794ced89a9bfdb5d00c238",
        baseUrl = "https://api.deepseek.com/v1",
        models = listOf("deepseek-chat", "deepseek-coder", "deepseek-reasoner"),
        selectedModel = "deepseek-chat"
    )
).reversed()

val DEFAULT_AI_PROVIDER_CONFIG_INFO = AiProviderConfigInfo(
    selectedAiProviderName = defaultAiProviders.first().name,
    aiProviderInfos = defaultAiProviders
)

