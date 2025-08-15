package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable
import com.wceng.app.aiassistant.data.source.local.model2.PromptEntity
import com.wceng.app.aiassistant.data.source.local.model2.PromptSource

@Immutable
data class Prompt(
    val id: Long,
    val title: String,
    val prompt: String,
    val source: PromptSource
)

fun PromptEntity.asExternalModel() = Prompt(
    id = id,
    title = title,
    prompt = prompt,
    source = when (source) {
        "network" -> PromptSource.Network
        "user" -> PromptSource.User
        else -> PromptSource.Unknow
    }
)
