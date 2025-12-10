package com.wceng.app.aiassistant.data.source.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wceng.app.aiassistant.data.source.local.dao2.AiProviderDao
import com.wceng.app.aiassistant.data.source.local.dao2.ChatDao
import com.wceng.app.aiassistant.data.source.local.dao2.PromptDao
import com.wceng.app.aiassistant.data.source.local.model2.AiProviderEntity
import com.wceng.app.aiassistant.data.source.local.model2.BubbleEntity
import com.wceng.app.aiassistant.data.source.local.model2.ConversationEntity
import com.wceng.app.aiassistant.data.source.local.model2.MessageEntity
import com.wceng.app.aiassistant.data.source.local.model2.MessageVersionEntity
import com.wceng.app.aiassistant.data.source.local.model2.PromptEntity

@Database(
    entities = [
        ConversationEntity::class,
        BubbleEntity::class,
        MessageEntity::class,
        MessageVersionEntity::class,
        PromptEntity::class,
        AiProviderEntity::class
    ],
    version = 8,
    autoMigrations = [
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8)
    ],
    exportSchema = true
)
@TypeConverters(DataConverter::class)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao

    abstract fun promptDao(): PromptDao

    abstract fun aiProviderDao(): AiProviderDao
}
