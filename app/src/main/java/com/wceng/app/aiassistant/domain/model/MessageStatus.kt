package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable

@Immutable
enum class MessageStatus(val value: Int) {
    NORMAL(0),
    LOADING(1),
    FAILED(2),
    STOPPED(3),
    GENERATING(4);

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}
