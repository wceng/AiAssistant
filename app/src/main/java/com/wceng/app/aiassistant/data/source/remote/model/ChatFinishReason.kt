package com.wceng.app.aiassistant.data.source.remote.model

sealed interface ChatFinishReason {
    data object Stop : ChatFinishReason
    data object Length : ChatFinishReason
    data object FunctionCall : ChatFinishReason
    data object ToolCalls : ChatFinishReason
    data object ContentFilter : ChatFinishReason
    data object UnKnown : ChatFinishReason
}