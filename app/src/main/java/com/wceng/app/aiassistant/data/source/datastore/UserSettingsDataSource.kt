package com.wceng.app.aiassistant.data.source.datastore

import androidx.datastore.core.DataStore
import com.wceng.app.aiassistant.domain.model.DarkModeInfo
import com.wceng.app.aiassistant.domain.model.ThemeSchemeInfo
import com.wceng.app.aiassistant.domain.model.UserSettingInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSettingsDataSource(
    private val datastore: DataStore<UserPreferences>,
) {

    val useSetting: Flow<UserSettingInfo> = datastore.data
        .map { preferences ->
            UserSettingInfo(
                themeSchemeInfo = when (preferences.themeScheme) {
                    ThemeScheme.THEME_UNSPECIFIED,
                    ThemeScheme.DEFAULT,
                    ThemeScheme.UNRECOGNIZED -> ThemeSchemeInfo.Default

                    ThemeScheme.DYNAMIC -> ThemeSchemeInfo.Dynamic
                },
                darkModeInfo = when (preferences.darkMode) {
                    DarkMode.SYSTEM,
                    DarkMode.UNRECOGNIZED,
                    DarkMode.DARK_MODE_UNSPECIFIED -> DarkModeInfo.System

                    DarkMode.LIGHT -> DarkModeInfo.Light
                    DarkMode.DARK -> DarkModeInfo.Dark
                },
                selectedAiProviderId = preferences.selectedAiProviderId.takeIf { it != 0L }
            )
        }

    suspend fun setSelectedAiProvider(aiProviderId: Long?) {
        datastore.updateData { userPreferences ->
            userPreferences.toBuilder()
                .setSelectedAiProviderId(aiProviderId ?: 0L)
                .build()
        }
    }

    suspend fun setThemeScheme(themeSchemeInfo: ThemeSchemeInfo) {
        datastore.updateData { userPreferences ->
            userPreferences
                .toBuilder()
                .setThemeScheme(
                    when (themeSchemeInfo) {
                        ThemeSchemeInfo.Default -> ThemeScheme.DEFAULT
                        ThemeSchemeInfo.Dynamic -> ThemeScheme.DYNAMIC
                    }
                )
                .build()
        }
    }

    suspend fun setDarkModel(darkModeInfo: DarkModeInfo) {
        datastore.updateData { userPreferences ->
            userPreferences.toBuilder()
                .setDarkMode(
                    when (darkModeInfo) {
                        DarkModeInfo.Light -> DarkMode.LIGHT
                        DarkModeInfo.Dark -> DarkMode.DARK
                        DarkModeInfo.System -> DarkMode.SYSTEM
                    }
                )
                .build()
        }
    }

}