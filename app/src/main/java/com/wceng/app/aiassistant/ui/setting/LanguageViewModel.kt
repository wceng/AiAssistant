package com.wceng.app.aiassistant.ui.setting

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wceng.app.aiassistant.data.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class LanguageViewModel(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    val uiState: StateFlow<LanguageUiState> =
        userSettingsRepository.userSettingInfo
            .map { language ->
                LanguageUiState.Success(
                    SelectableLanguageInfoModel(
                        languageInfos,
                        null
                    )
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                LanguageUiState.Loading
            )


//    private fun getCurrentLanguageInfo(): LanguageInfo {
//        return languageInfos.firstOrNull {
//            it.language == getCurrentLanguage()
//        } ?: languageInfos[0]
//    }

//    fun selectLanguage(language: String) {
//        viewModelScope.launch {
//            userSettingsRepository.setSelectedLanguage(language)
//        }
//    }
}

sealed interface LanguageUiState {
    data object Loading : LanguageUiState

    data class Success(
        val selectableLanguageInfoModel: SelectableLanguageInfoModel
    ) : LanguageUiState
}

@Immutable
data class LanguageInfo(
    val name: String,
    val language: String
)

@Immutable
data class SelectableLanguageInfoModel(
    val languageInfos: List<LanguageInfo>,
    val selectedLanguageInfo: LanguageInfo?
)

private val languageInfos = listOf(
    LanguageInfo("English", "en"),
    LanguageInfo("简体中文", "zh"),
)