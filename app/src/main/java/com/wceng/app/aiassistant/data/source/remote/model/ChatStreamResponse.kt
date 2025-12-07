package com.wceng.app.aiassistant.data.source.remote.model

data class ChatStreamResponse(
    val deltaContent: String?,
    val chatFinishReason: ChatFinishReason?
)