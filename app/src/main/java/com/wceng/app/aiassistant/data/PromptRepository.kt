package com.wceng.app.aiassistant.data

import com.wceng.app.aiassistant.data.source.local.dao2.PromptDao
import com.wceng.app.aiassistant.data.source.local.model2.PromptEntity
import com.wceng.app.aiassistant.data.source.local.model2.asEntity
import com.wceng.app.aiassistant.data.source.remote.PromptApi
import com.wceng.app.aiassistant.domain.model.Prompt
import com.wceng.app.aiassistant.domain.model.asExternalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PromptRepository {
    val prompts: Flow<List<Prompt>>
    suspend fun refresh(forced: Boolean = false)
    suspend fun getPrompt(promptId: Long): Prompt?
}

class OfflineFirstPromptRepository(
    private val promptApi: PromptApi,
    private val promptDao: PromptDao,
) : PromptRepository {

    override val prompts: Flow<List<Prompt>>
        get() = promptDao.getAllFlow().map { it.map(PromptEntity::asExternalModel) }


    override suspend fun refresh(forced: Boolean) {
        if (forced) {
            fetchAll()
        } else {
            if (promptDao.isEmpty()) {
                fetchAll()
            }
        }
    }

    override suspend fun getPrompt(promptId: Long): Prompt? {
        return promptDao.getPrompt(promptId)?.asExternalModel()
    }

    private suspend fun fetchAll() {
        val fromNetwork = promptApi.getPrompts().map { it.asEntity() }
        promptDao.insertAll(fromNetwork)

    }
}

