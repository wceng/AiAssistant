package com.wceng.app.aiassistant.ui

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.staticCompositionLocalOf

val LocalWindowWidthSize = staticCompositionLocalOf<WindowWidthSizeClass> {
    error("CompositionLocal LocalWindowWidthSize not present")
}