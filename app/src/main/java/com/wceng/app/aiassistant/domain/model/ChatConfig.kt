package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class ChatConfig(
    val apiKey: String,
    val baseUrl: String,
    val model: String,
)

fun UserSettingInfo.asChatConfig(): ChatConfig {
    val selectedAiProviderInfo = aiProviderConfigInfo.selectedAiProviderInfo

    return ChatConfig(
        apiKey = selectedAiProviderInfo.apiKey,
        baseUrl = selectedAiProviderInfo.baseUrl,
        model = selectedAiProviderInfo.selectedModel,
    )
}