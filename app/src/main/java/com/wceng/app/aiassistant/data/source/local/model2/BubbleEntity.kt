package com.wceng.app.aiassistant.data.source.local.model2

import androidx.compose.runtime.Immutable
import androidx.room.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "bubble",
    foreignKeys = [ForeignKey(
        entity = ConversationEntity::class,
        parentColumns = ["id"],
        childColumns = ["conversation_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["timestamp"])]
)
@Immutable
data class BubbleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "conversation_id")
    val conversationId: Long,

    val sender: String,

    val timestamp: Instant = Clock.System.now(),
)
