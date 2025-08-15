package com.wceng.app.aiassistant.di

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.room.Room
import com.wceng.app.aiassistant.MainViewModel
import com.wceng.app.aiassistant.data.ChatRepository
import com.wceng.app.aiassistant.data.DefaultChatRepository
import com.wceng.app.aiassistant.data.DefaultUserSettingsRepository
import com.wceng.app.aiassistant.data.OfflineFirstPromptRepository
import com.wceng.app.aiassistant.data.PromptRepository
import com.wceng.app.aiassistant.data.UserSettingsRepository
import com.wceng.app.aiassistant.data.source.datastore.UserPreferencesSerializer
import com.wceng.app.aiassistant.data.source.datastore.UserSettingsDataSource
import com.wceng.app.aiassistant.data.source.local.ChatDatabase
import com.wceng.app.aiassistant.data.source.local.dao2.ChatDao
import com.wceng.app.aiassistant.data.source.local.dao2.PromptDao
import com.wceng.app.aiassistant.data.source.remote.ChatApi
import com.wceng.app.aiassistant.data.source.remote.KtorPromptApi
import com.wceng.app.aiassistant.data.source.remote.OpenAIChatApi
import com.wceng.app.aiassistant.data.source.remote.PromptApi
import com.wceng.app.aiassistant.domain.usecase.CreateConversationWithPromptUseCase
import com.wceng.app.aiassistant.domain.usecase.GetGroupedConversationsUseCase
import com.wceng.app.aiassistant.navigation.ConversationWithChatViewModel
import com.wceng.app.aiassistant.ui.chat.ChatViewModel
import com.wceng.app.aiassistant.ui.prompt.PromptViewModel
import com.wceng.app.aiassistant.ui.session.SessionViewModel
import com.wceng.app.aiassistant.ui.setting.ColorSchemeViewModel
import com.wceng.app.aiassistant.ui.setting.ServiceProviderViewModel
import com.wceng.app.aiassistant.ui.setting.SettingViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            context = androidApplication(),
            klass = ChatDatabase::class.java,
            name = "chat_db"
        )
            .fallbackToDestructiveMigration(true)
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    single<ChatDao> { get<ChatDatabase>().chatDao() }
    single<PromptDao> { get<ChatDatabase>().promptDao() }


    single {
        DataStoreFactory.create(
            serializer = UserPreferencesSerializer,
        ) {
            androidApplication().dataStoreFile("user_preferences.pb")
        }
//        PreferenceDataStoreFactory.createWithPath(
//            produceFile = {
//                androidApplication().filesDir.resolve("chat.preferences_pb").absolutePath.toPath()
//            }
//        )
    }
    singleOf(::UserSettingsDataSource)

    singleOf(::OpenAiProvider)
    singleOf(::OpenAIChatApi) { bind<ChatApi>() }

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get(), contentType = ContentType.Any)
            }
        }
    }

    singleOf(::KtorPromptApi) { bind<PromptApi>() }
}

val repositoryModule = module {
    singleOf(::DefaultUserSettingsRepository) { bind<UserSettingsRepository>() }
    singleOf(::DefaultChatRepository) { bind<ChatRepository>() }
    singleOf(::OfflineFirstPromptRepository) { bind<PromptRepository>() }
}

val useCaseModule = module {
    factoryOf(::CreateConversationWithPromptUseCase)
    factoryOf(::GetGroupedConversationsUseCase)
}

val viewModelModule = module {
    viewModelOf(::ChatViewModel)
    viewModelOf(::SessionViewModel)
    viewModelOf(::SettingViewModel)
    viewModelOf(::PromptViewModel)
    viewModelOf(::ConversationWithChatViewModel)
    viewModelOf(::ServiceProviderViewModel)
    viewModelOf(::ColorSchemeViewModel)
    viewModelOf(::MainViewModel)
}

val configModule = module {
    single(named("ioDispatcher")) { Dispatchers.IO }
    single {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
        }
    }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            configModule,
            dataModule,
            repositoryModule,
            useCaseModule,
            viewModelModule
        )
    }

