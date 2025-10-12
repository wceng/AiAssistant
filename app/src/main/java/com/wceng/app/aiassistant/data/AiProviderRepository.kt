package com.wceng.app.aiassistant.data

import com.wceng.app.aiassistant.data.source.local.dao2.AiProviderDao
import com.wceng.app.aiassistant.data.source.local.model2.asEntity
import com.wceng.app.aiassistant.data.source.remote.ChatApi
import com.wceng.app.aiassistant.domain.model.AiProviderInfo
import com.wceng.app.aiassistant.domain.model.asExternalModel
import com.wceng.app.aiassistant.domain.model.defaultAiProviders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface AiProviderRepository {
    suspend fun updateSelectedModel(providerId: Long, model: String?)

    suspend fun updateBaseUrl(providerId: Long, baseUrl: String?)

    suspend fun updateAvailableModels(providerId: Long, models: List<String>)

    suspend fun addAvailableModel(providerId: Long, model: String)

    suspend fun addAvailableModels(providerId: Long, models: List<String>)

    suspend fun removeAvailableModel(providerId: Long, model: String)

    suspend fun updateApiKey(providerId: Long, apiKey: String)

    fun getAllProviders(): Flow<List<AiProviderInfo>>

    fun getBuiltInProviders(): Flow<List<AiProviderInfo>>

    fun getCustomProviders(): Flow<List<AiProviderInfo>>

    suspend fun deleteProvider(providerId: Long)

    suspend fun deleteAllCustomProviders()

    suspend fun addProvider(provider: AiProviderInfo): Long

    suspend fun getProviderById(providerId: Long): Flow<AiProviderInfo?>

    suspend fun refreshAvailableModels(providerId: Long): Result<Unit>
}

class DefaultAiProviderRepository(
    private val dao: AiProviderDao,
    private val chatApi: ChatApi
) : AiProviderRepository {

    init {
        initializeDefaultProviders()
    }

    private fun initializeDefaultProviders() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (dao.getCount() == 0) {
                    insertBuiltInProviders()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun insertBuiltInProviders() {
        val builtInProviders = defaultAiProviders.map { it.asEntity() }
        builtInProviders.forEach { provider ->
            dao.insert(provider)
        }
    }

    override suspend fun updateSelectedModel(providerId: Long, model: String?) {
        dao.updateSelectedModel(providerId, model)
    }

    override suspend fun updateBaseUrl(providerId: Long, baseUrl: String?) {
        dao.updateBaseUrl(providerId, baseUrl)
    }

    override suspend fun updateAvailableModels(providerId: Long, models: List<String>) {
        dao.updateAvailableModels(providerId, models)
    }

    override suspend fun addAvailableModel(providerId: Long, model: String) {
        val currentModels = getAvailableModels(providerId)
        val updatedModels = currentModels.toMutableList().apply {
            if (!contains(model)) {
                add(model)
            }
        }
        dao.updateAvailableModels(providerId, updatedModels)
    }

    override suspend fun addAvailableModels(providerId: Long, models: List<String>) {
        val currentModels = getAvailableModels(providerId)
        val updatedModels = currentModels.toMutableList().apply {
            models.forEach { model ->
                if (!contains(model)) {
                    add(model)
                }
            }
        }
        dao.updateAvailableModels(providerId, updatedModels)
    }

    override suspend fun removeAvailableModel(providerId: Long, model: String) {
        val currentModels = getAvailableModels(providerId)
        val updatedModels = currentModels.toMutableList().apply {
            remove(model)
        }
        dao.updateAvailableModels(providerId, updatedModels)
    }

    override suspend fun updateApiKey(providerId: Long, apiKey: String) {
        dao.updateApiKey(providerId, apiKey)
    }

    override fun getAllProviders(): Flow<List<AiProviderInfo>> {
        return dao.getAllProviders().map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override fun getBuiltInProviders(): Flow<List<AiProviderInfo>> {
        return dao.getBuiltInProviders().map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override fun getCustomProviders(): Flow<List<AiProviderInfo>> {
        return dao.getCustomProviders().map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override suspend fun deleteProvider(providerId: Long) {
        dao.delete(providerId)
    }

    override suspend fun deleteAllCustomProviders() {
        dao.deleteAllCustomProviders()
    }

    override suspend fun addProvider(provider: AiProviderInfo): Long {
        return dao.insert(provider.asEntity())
    }

    override suspend fun getProviderById(providerId: Long): Flow<AiProviderInfo?> {
        return dao.getProviderById(providerId).map { it?.asExternalModel() }
    }

    override suspend fun refreshAvailableModels(providerId: Long): Result<Unit> {
        chatApi.listModes()
            .onSuccess {
                addAvailableModels(providerId, it)
            }
            .onFailure {
                return Result.failure(it)
            }
        return Result.success(Unit)
    }

    private suspend fun getAvailableModels(providerId: Long): List<String> {
        return dao.getProviderById(providerId).first()?.availableModels ?: emptyList()
    }
}