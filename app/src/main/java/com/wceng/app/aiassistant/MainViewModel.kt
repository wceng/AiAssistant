package com.wceng.app.aiassistant

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wceng.app.aiassistant.data.UserSettingsRepository
import com.wceng.app.aiassistant.domain.model.DarkModeInfo
import com.wceng.app.aiassistant.domain.model.ThemeSchemeInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = userSettingsRepository.userSettingInfo
        .map {
            MainUiState.Success(
                themeInfos = ThemeInfos(
                    it.darkModeInfo,
                    it.themeSchemeInfo
                ),
            )
        }
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MainUiState.Loading
        )
}

@Immutable
data class ThemeInfos(
    val darkModeInfo: DarkModeInfo,
    val themeSchemeInfo: ThemeSchemeInfo
)

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(
        val themeInfos: ThemeInfos,
    ) : MainUiState {
        override fun shouldUseDarkTheme(isSystemDarkTheme: Boolean): Boolean =
            when (themeInfos.darkModeInfo) {
                DarkModeInfo.Light -> false
                DarkModeInfo.Dark -> true
                DarkModeInfo.System -> isSystemDarkTheme
            }

        override val shouldDisableDynamicTheming: Boolean =
            when (themeInfos.themeSchemeInfo) {
                ThemeSchemeInfo.Dynamic -> false
                ThemeSchemeInfo.Default -> true
            }
    }

    val shouldDisableDynamicTheming: Boolean get() = true

    fun shouldUseDarkTheme(isSystemDarkTheme: Boolean) = isSystemDarkTheme

}

