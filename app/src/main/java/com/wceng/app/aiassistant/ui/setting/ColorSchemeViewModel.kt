package com.wceng.app.aiassistant.ui.setting

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wceng.app.aiassistant.data.UserSettingsRepository
import com.wceng.app.aiassistant.domain.model.DarkModeInfo
import com.wceng.app.aiassistant.domain.model.ThemeSchemeInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ColorSchemeViewModel(
    val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    val uiState: StateFlow<ColorSchemeUiState> = userSettingsRepository.userSettingInfo
        .map {
            ColorSchemeUiState.Success(
                colorSchemeSettings = ColorSchemeSettings(
                    themeSchemeInfo = it.themeSchemeInfo,
                    darkModeInfo = it.darkModeInfo
                )
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ColorSchemeUiState.Loading
        )

    fun onSelectThemeScheme(themeSchemeInfo: ThemeSchemeInfo) {
        viewModelScope.launch {
            userSettingsRepository.setThemeScheme(themeSchemeInfo)
        }
    }

    fun onSelectDarkModeInfo(darkModeInfo: DarkModeInfo) {
        viewModelScope.launch {
            userSettingsRepository.setDarkModel(darkModeInfo)
        }
    }

}

sealed interface ColorSchemeUiState {
    data object Loading : ColorSchemeUiState

    data class Success(
        val colorSchemeSettings: ColorSchemeSettings
    ) : ColorSchemeUiState
}

@Immutable
data class ColorSchemeSettings(
    val themeSchemeInfo: ThemeSchemeInfo,
    val darkModeInfo: DarkModeInfo
)