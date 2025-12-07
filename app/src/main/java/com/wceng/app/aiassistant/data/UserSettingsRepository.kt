package com.wceng.app.aiassistant.data

import com.wceng.app.aiassistant.data.source.datastore.UserSettingsDataSource
import com.wceng.app.aiassistant.domain.model.DarkModeInfo
import com.wceng.app.aiassistant.domain.model.ThemeSchemeInfo
import com.wceng.app.aiassistant.domain.model.UserSettingInfo
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val userSettingInfo: Flow<UserSettingInfo>

    suspend fun setSelectedAiProvider(aiProviderId: Long?)
    suspend fun setThemeScheme(themeSchemeInfo: ThemeSchemeInfo)
    suspend fun setDarkModel(darkModeInfo: DarkModeInfo)
}

class DefaultUserSettingsRepository(
    private val userSettingsDataSource: UserSettingsDataSource,
) : UserSettingsRepository {

    override val userSettingInfo: Flow<UserSettingInfo>
        get() = userSettingsDataSource.useSetting

    override suspend fun setSelectedAiProvider(aiProviderId: Long?) {
        userSettingsDataSource.setSelectedAiProvider(aiProviderId)
    }

    override suspend fun setThemeScheme(themeSchemeInfo: ThemeSchemeInfo) {
        userSettingsDataSource.setThemeScheme(themeSchemeInfo)
    }

    override suspend fun setDarkModel(darkModeInfo: DarkModeInfo) {
        userSettingsDataSource.setDarkModel(darkModeInfo)
    }
}