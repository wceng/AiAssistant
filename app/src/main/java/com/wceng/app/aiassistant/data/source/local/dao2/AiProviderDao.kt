package com.wceng.app.aiassistant.data.source.local.dao2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wceng.app.aiassistant.data.source.local.model2.AiProviderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiProviderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(provider: AiProviderEntity): Long

    @Update
    suspend fun update(provider: AiProviderEntity)

    @Query("delete from ai_providers where id = :providerId")
    suspend fun delete(providerId: Long)

    @Query("SELECT * FROM ai_providers ORDER BY is_built_in DESC, provider_name ASC")
    fun getAllProviders(): Flow<List<AiProviderEntity>>

    @Query("SELECT * FROM ai_providers WHERE is_built_in = 1")
    fun getBuiltInProviders(): Flow<List<AiProviderEntity>>

    @Query("SELECT * FROM ai_providers WHERE is_built_in = 0")
    fun getCustomProviders(): Flow<List<AiProviderEntity>>

    @Query("SELECT * FROM ai_providers WHERE id = :id")
    fun getProviderById(id: Long): Flow<AiProviderEntity?>

    @Query("SELECT * FROM ai_providers WHERE provider_name = :name")
    fun getProviderByName(name: String): Flow<AiProviderEntity?>

    @Query("DELETE FROM ai_providers WHERE is_built_in = 0")
    suspend fun deleteAllCustomProviders()

    @Query("UPDATE ai_providers SET api_key = :apiKey WHERE id = :id")
    suspend fun updateApiKey(id: Long, apiKey: String)

    @Query("UPDATE ai_providers SET is_built_in = :isBuiltIn WHERE id = :id")
    suspend fun updateBuiltInStatus(id: Long, isBuiltIn: Boolean)

    @Query("UPDATE ai_providers SET selected_model = :model WHERE id = :id")
    suspend fun updateSelectedModel(id: Long, model: String?)

    @Query("UPDATE ai_providers SET base_url = :baseUrl WHERE id = :id")
    suspend fun updateBaseUrl(id: Long, baseUrl: String?)

    @Query("UPDATE ai_providers SET available_models = :models WHERE id = :id")
    suspend fun updateAvailableModels(id: Long, models: List<String>)

    @Query("SELECT COUNT(*) FROM ai_providers")
    suspend fun getCount(): Int
}