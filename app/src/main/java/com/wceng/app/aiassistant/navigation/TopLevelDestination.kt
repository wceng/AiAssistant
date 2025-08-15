package com.wceng.app.aiassistant.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconText: String,
    val titleText: String,
    val route: KClass<*>,
    val baseRoute: KClass<*> = route,
) {
    Chat(
        selectedIcon = Icons.AutoMirrored.Rounded.Chat,
        unselectedIcon = Icons.AutoMirrored.Outlined.Chat,
        iconText = "Chat",
        titleText = "Chat",
        route = ConversationsRoute::class
    ),

    Prompt(
        selectedIcon = Icons.Rounded.Lightbulb,
        unselectedIcon = Icons.Outlined.Lightbulb,
        iconText = "Prompts",
        titleText = "Prompts",
        route = PromptsRoute::class
    ),

    Setting(
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        iconText = "Settings",
        titleText = "Settings",
        route = MainSettingRoute::class,
        baseRoute = SettingBaseRoute::class
    ),


}
