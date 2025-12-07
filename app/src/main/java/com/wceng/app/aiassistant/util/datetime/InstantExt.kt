package com.wceng.app.aiassistant.util.datetime

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 格式化为相对时间 (例如: "2小时前", "昨天", "3月5日")
 */
fun Instant.formatAsRelativeTime(): String {
    val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
    val duration = now - this

    return when {
        duration.inWholeSeconds < 60 -> "刚刚"
        duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}分钟前"
        duration.inWholeHours < 24 -> "${duration.inWholeHours}小时前"
        duration.inWholeDays < 30 -> "${duration.inWholeDays}天前"
        else -> formatAsDate()
    }
}

/**
 * 格式化为日期 (例如: "3月5日 14:30")
 */
fun Instant.formatAsDate(): String {
    val localDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.monthNumber}月${localDateTime.dayOfMonth}日 ${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
}

/**
 * 格式化为简短时间 (例如: "14:30")
 */
fun Instant.formatAsTime(): String {
    val localDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
}
