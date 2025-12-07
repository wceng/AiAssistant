package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class ChatConfig(
    val apiKey: String = "",
    val baseUrl: String = "",
    val model: String = "",
)

fun AiProviderInfo.asChatConfig(): ChatConfig {
    return ChatConfig(
        apiKey = apiKey ?: "",
        baseUrl = baseUrl ?: "",
        model = selectedModel ?: "",
    )
}