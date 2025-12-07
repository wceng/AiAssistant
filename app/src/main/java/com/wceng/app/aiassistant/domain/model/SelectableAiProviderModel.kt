package com.wceng.app.aiassistant.domain.model

data class SelectableAiProviderModel(
    val selectedAiProvider: AiProviderInfo? = null,
    val aiProviders: List<AiProviderInfo> = emptyList()
)