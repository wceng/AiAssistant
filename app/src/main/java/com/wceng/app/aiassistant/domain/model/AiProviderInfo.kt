package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable
import com.wceng.app.aiassistant.BuildConfig
import com.wceng.app.aiassistant.data.source.local.model2.AiProviderEntity
import com.wceng.app.aiassistant.util.Constant

@Immutable
data class AiProviderInfo(
    val id: Long = 0,
    val name: String,
    val apiKey: String? = null,
    val baseUrl: String? = null,
    val selectedModel: String? = null,
    val availableModels: List<String> = emptyList(),
    val isBuiltIn: Boolean = false
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                baseUrl?.isNotBlank() == true &&
                selectedModel?.isNotBlank() == true

    val hasApiKey: Boolean
        get() = !apiKey.isNullOrBlank()
}

fun AiProviderEntity.asExternalModel(): AiProviderInfo = AiProviderInfo(
    id = id,
    name = providerName,
    apiKey = apiKey,
    baseUrl = baseUrl,
    selectedModel = selectedModel,
    availableModels = availableModels,
    isBuiltIn = isBuiltIn
)

val defaultAiProviders = listOf(
    AiProviderInfo(
        name = "OpenAI",
        apiKey = "",
        baseUrl = Constant.OPENAI_HOST_URL,
        availableModels = listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-4"),
        selectedModel = "gpt-4o",
        isBuiltIn = true,
        id = 1
    ),
    AiProviderInfo(
        name = "DeepSeek",
        apiKey = "sk-d2b158ec85794ced89a9bfdb5d00c238",
        baseUrl = Constant.DEEPSEEK_HOST_URL,
        availableModels = listOf("deepseek-chat", "deepseek-reasoner"),
        selectedModel = "deepseek-chat",
        isBuiltIn = true,
        id = 2
    ) ,
    AiProviderInfo(
        name = "ChatAnywhere",
        apiKey = BuildConfig.OPENAI_KEY,
        baseUrl = BuildConfig.OPENAI_HOST,
        availableModels = listOf("gpt-5-mini-ca", "gpt-4o-ca"),
        selectedModel = "gpt-5-mini-ca",
        isBuiltIn = true,
        id = 3
    )
)
