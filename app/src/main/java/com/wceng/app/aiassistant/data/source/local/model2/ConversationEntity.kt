package com.wceng.app.aiassistant.data.source.local.model2

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "conversation",
    indices = [Index(value = ["current_message_version"])]
)
@Immutable
data class ConversationEntity constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "prompt_id")
    val promptId: Long? = null,

    val title: String,

    @ColumnInfo(name = "title_source")
    val titleSource: Int = 0, //default

    @ColumnInfo(name = "create_time")
    val createTime: Instant = Clock.System.now(),

    @ColumnInfo(name = "current_message_version", defaultValue = "1")
    val currentMessageVersion: Long = 1,

    @ColumnInfo(name = "last_updated_time")
    val lastUpdatedTime: Instant = createTime
)

enum class ConversationTitleSource(val value: Int) {
    Default(0),
    User(1),
    Ai(2),
    Prompt(3);

    companion object {
        fun fromInt(value: Int) = ConversationTitleSource.entries.first { it.value == value }
    }
}
