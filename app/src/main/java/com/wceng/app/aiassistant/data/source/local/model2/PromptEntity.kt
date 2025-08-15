package com.wceng.app.aiassistant.data.source.local.model2

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wceng.app.aiassistant.data.source.remote.model.NetworkPrompt

@Entity(
    tableName = "prompt"
)
data class PromptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val prompt: String,
    val source: String, //network,user
)

enum class PromptSource {
    Network,
    User,
    Unknow
}

fun NetworkPrompt.asEntity() = PromptEntity(
    title = act,
    prompt = prompt,
    source = "network"
)