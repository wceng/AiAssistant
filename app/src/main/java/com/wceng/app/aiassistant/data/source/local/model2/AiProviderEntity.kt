package com.wceng.app.aiassistant.data.source.local.model2

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wceng.app.aiassistant.domain.model.AiProviderInfo

@Entity(tableName = "ai_providers")
@Immutable
data class AiProviderEntity (
    @ColumnInfo(name = "provider_name")
    var providerName: String, // 如: OpenAI, Gemini, Claude, DeepSeek等

    @ColumnInfo(name = "api_key")
    var apiKey: String?,

    @ColumnInfo(name = "base_url")
    var baseUrl: String?,

    @ColumnInfo(name = "selected_model")
    var selectedModel: String?,

    @ColumnInfo(name = "available_models")
    var availableModels: List<String> = emptyList(),

    @ColumnInfo(name = "is_built_in", defaultValue = "false")
    var isBuiltIn: Boolean = false
){
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

fun AiProviderInfo.asEntity(): AiProviderEntity = AiProviderEntity(
    providerName = name,
    apiKey = apiKey,
    baseUrl = baseUrl,
    selectedModel = selectedModel,
    availableModels = availableModels,
    isBuiltIn = isBuiltIn,
).apply {
    id = this@asEntity.id
}