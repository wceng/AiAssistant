package com.wceng.app.aiassistant.data.source.local.model2

import androidx.compose.runtime.Immutable
import androidx.room.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "message_version",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BubbleEntity::class,
            parentColumns = ["id"],
            childColumns = ["bubble_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )],
    indices = [
        Index(value = ["message_id", "version", "bubble_id"], unique = true),
        Index(value = ["version"]),
        Index(value = ["message_id"]),
        Index(value = ["bubble_id"]),
        Index(value = ["last_view_time"])]
)
@Immutable
data class MessageVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val version: Long,

    @ColumnInfo(name = "message_id")
    val messageId: Long,

    @ColumnInfo(name = "bubble_id")
    val bubbleId: Long,

    @ColumnInfo(name = "last_view_time")
    val lastViewTime: Instant = Clock.System.now(),
)
