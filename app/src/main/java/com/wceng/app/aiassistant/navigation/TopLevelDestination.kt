package com.wceng.app.aiassistant.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.wceng.app.aiassistant.R
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconText: Int,
    @StringRes val titleText: Int,
    val route: KClass<*>,
    val baseRoute: KClass<*> = route,
) {
    Chat(
        selectedIcon = Icons.AutoMirrored.Rounded.Chat,
        unselectedIcon = Icons.AutoMirrored.Outlined.Chat,
        iconText = R.string.chat_title,
        titleText = R.string.chat_title,
        route = ConversationsRoute::class
    ),

    Prompt(
        selectedIcon = Icons.Rounded.Lightbulb,
        unselectedIcon = Icons.Outlined.Lightbulb,
        iconText = R.string.prompt_title,
        titleText = R.string.prompt_title,
        route = PromptsRoute::class
    ),

    Setting(
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        iconText = R.string.setting_title,
        titleText = R.string.setting_title,
        route = MainSettingRoute::class,
        baseRoute = SettingBaseRoute::class
    ),


}
