//package com.wceng.app.aiassistant.data.source
//
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.PreferenceDataStoreFactory
//import androidx.datastore.preferences.core.Preferences
//import androidx.room.RoomDatabase
//import androidx.sqlite.driver.bundled.BundledSQLiteDriver
//import com.wceng.app.aiassistant.data.source.local.ChatDatabase
//import kotlinx.coroutines.Dispatchers
//import okio.Path.Companion.toPath
//
//internal const val dataStoreFileName = "dice.preferences_pb"
//
////DataStore
//expect fun createDataStore(): DataStore<Preferences>
//
//fun createDataStoreWithPath(producePath: () -> String): DataStore<Preferences> =
//    PreferenceDataStoreFactory.createWithPath(
//        produceFile = { producePath().toPath() }
//    )
//
////数据库
//expect fun getDatabaseBuilder(): RoomDatabase.Builder<ChatDatabase>
//
//fun getChatDatabase(
//    builder: RoomDatabase.Builder<ChatDatabase> = getDatabaseBuilder()
//): ChatDatabase {
//    return builder
////      .addMigrations(MIGRATIONS)
//        .fallbackToDestructiveMigration(true)
//        .setDriver(BundledSQLiteDriver())
//        .setQueryCoroutineContext(Dispatchers.IO)
//        .build()
//}

