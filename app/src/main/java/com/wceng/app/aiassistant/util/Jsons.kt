package com.wceng.app.aiassistant.util

import kotlinx.serialization.json.Json

/**
 * 将任意 [Serializable] 对象序列化为 JSON 字符串
 *
 * @receiver 任何可序列化的对象
 * @return JSON 字符串
 */
inline fun <reified T> T.toJson(): String {
    return Json.encodeToString(this)
}

/**
 * 可选：带格式化（美化输出）的版本
 */
inline fun <reified T> T.toPrettyJson(): String {
    return Json { prettyPrint = true }.encodeToString(this)
}