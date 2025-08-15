package com.wceng.app.aiassistant.data.source.local.model2

import androidx.compose.runtime.Immutable
import androidx.room.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "message",
    foreignKeys = [ForeignKey(
        entity = BubbleEntity::class,
        parentColumns = ["id"],
        childColumns = ["bubble_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
    )],
    indices = [
        Index(value = ["bubble_id"]),
        Index(value = ["timestamp"])
    ]
)
@Immutable
data class MessageEntity constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "bubble_id")
    val bubbleId: Long,

    val content: String,

    @ColumnInfo(defaultValue = "0")
    val status: Int = 0,

    val timestamp: Instant = Clock.System.now(),
)


//fun Message.asEntity() = MessageEntity(
//    id = id,
//    bubbleId = bubbleId,
//    content = content,
//    status = status.value,
//)