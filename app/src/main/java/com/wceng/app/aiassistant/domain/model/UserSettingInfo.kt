package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable
import com.wceng.app.aiassistant.util.Constant

@Immutable
data class UserSettingInfo(
    val aiProviderConfigInfo: AiProviderConfigInfo,
    val themeSchemeInfo: ThemeSchemeInfo,
    val darkModeInfo: DarkModeInfo,
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
        baseUrl = Constant.OPENAI_HOST_URL,
        models = listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-4"),
        selectedModel = "gpt-4o"
    ),
    AiProviderInfo(
        name = "DeepSeek",
        apiKey = "sk-d2b158ec85794ced89a9bfdb5d00c238",
        baseUrl = Constant.DEEPSEEK_HOST_URL,
        models = listOf("deepseek-chat", "deepseek-reasoner"),
        selectedModel = "deepseek-chat"
    )
).reversed()

val DEFAULT_AI_PROVIDER_CONFIG_INFO = AiProviderConfigInfo(
    selectedAiProviderName = defaultAiProviders.first().name,
    aiProviderInfos = defaultAiProviders
)

