package com.wceng.app.aiassistant.data.source.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            try {
                // 1. 为 conversation 表添加 title_source 列
                database.execSQL("""
                ALTER TABLE conversation 
                ADD COLUMN title_source INTEGER NOT NULL DEFAULT 0
            """)
            } catch (e: Exception) {
                throw RuntimeException("Migration from 5 to 6 failed", e)
            }
        }
    }
}