package com.wceng.app.aiassistant.data.source.local.dao2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wceng.app.aiassistant.data.source.local.model2.PromptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {

    @Insert
    suspend fun insertAll(prompts: List<PromptEntity>)

    @Insert
    suspend fun insert(prompt: PromptEntity)

    @Query("select * from prompt where id = :id")
    suspend fun getPrompt(id: Long): PromptEntity?

    @Query("""select prompt.prompt from prompt
        inner join conversation on conversation.prompt_id = prompt.id
        where conversation.id = :convId
    """)
    suspend fun getPromptContentByConvId(convId: Long): String?

    @Query("select * from prompt where id = :id")
    fun getFlow(id: Long): Flow<PromptEntity>

    @Query("select * from prompt")
    fun getAllFlow(): Flow<List<PromptEntity>>

    @Query("select COUNT(*) from prompt")
    suspend fun count(): Int

    @Query("select count(*) == 0 from prompt")
    suspend fun isEmpty(): Boolean
}