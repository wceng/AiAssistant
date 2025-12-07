package com.wceng.app.aiassistant.domain.model

import androidx.compose.runtime.Immutable
import com.wceng.app.aiassistant.util.Constant

@Immutable
data class UserSettingInfo(
    val themeSchemeInfo: ThemeSchemeInfo,
    val darkModeInfo: DarkModeInfo,
    val selectedAiProviderId: Long?
)
