package com.wceng.app.aiassistant.data.source.local

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

class DateConverter {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.Companion.fromEpochMilliseconds(it) }
    }
}