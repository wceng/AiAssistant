package com.wceng.app.aiassistant.data.source.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkPrompt(
    val act: String,
    val prompt: String
)
