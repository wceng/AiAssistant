package com.wceng.app.aiassistant.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.ui.theme.AiaSafeDp
import com.wceng.app.aiassistant.util.Constant

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
//    viewModel: SettingViewModel = koinViewModel(),
    onNavigateToServerProviderScreen: () -> Unit,
    onNavigateToLicenseScreen: () -> Unit
) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val uriHandler = LocalUriHandler.current

    var showColorSchemeBottomSheet by remember {
        mutableStateOf(false)
    }

    SettingContent(
        modifier = modifier,
        settingActions = SettingActions(
            onClickServiceProvider = onNavigateToServerProviderScreen,
            onClickConversationSetting = {},
            onClickLanguage = {},
            onClickAppearance = {
                showColorSchemeBottomSheet = true
            },
            onClickVersion = {},
            onClickLicense = {
                onNavigateToLicenseScreen()
            },
            onClickGithub = {
                uriHandler.openUri(Constant.GITHUB_LINK)
            },
            onClickFeedback = {}
        )
    )

    ColorSchemeBottomSheet(
        show = showColorSchemeBottomSheet,
        onDismissRequest = { showColorSchemeBottomSheet = false }
    )
}

data class SettingActions(
    val onClickServiceProvider: () -> Unit,
    val onClickConversationSetting: () -> Unit,
    val onClickLanguage: () -> Unit,
    val onClickAppearance: () -> Unit,
    val onClickVersion: () -> Unit,
    val onClickLicense: () -> Unit,
    val onClickGithub: () -> Unit,
    val onClickFeedback: () -> Unit,
)

@Composable
private fun SettingContent(
    modifier: Modifier = Modifier,
    settingActions: SettingActions
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AiaSafeDp.safeHorizontal),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        SettingItemPanel(
            title = "Service Provider Config",
        ) {
            SettingItem(
                title = "Service Provider",
                onClick = settingActions.onClickServiceProvider,
                leadingIcon = AiaImages.Cloud
            )
        }

        SettingItemPanel(
            title = "Conversation",
        ) {
            SettingItem(
                title = "Conversation Setting",
                onClick = settingActions.onClickConversationSetting,
                leadingIcon = AiaImages.Forum
            )
        }

        SettingItemPanel(
            title = "Display",
        ) {
            SettingItem(
                title = "Language",
                onClick = settingActions.onClickLanguage,
                leadingIcon = AiaImages.Language
            )

            SettingItem(
                title = "Appearance",
                onClick = settingActions.onClickAppearance,
                leadingIcon = AiaImages.Palette
            )
        }

        SettingItemPanel(
            title = "About",
        ) {
            SettingItem(
                title = "Version",
                onClick = settingActions.onClickVersion,
                leadingIcon = AiaImages.Info
            )

            SettingItem(
                title = "License",
                onClick = settingActions.onClickLicense,
                leadingIcon = AiaImages.Description
            )

            SettingItem(
                title = "Github",
                onClick = settingActions.onClickGithub,
                leadingIcon = AiaImages.Code
            )

            SettingItem(
                title = "Feedback",
                onClick = settingActions.onClickFeedback,
                leadingIcon = AiaImages.Feedback
            )
        }
    }
}

@Composable
fun SettingItemPanel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.alpha(0.7f)
        )
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun SettingItemPanelPreview() {
    Surface {
        SettingItemPanel(
            title = "配置",
        ) {
            SettingItem(title = "Title1", onClick = {})
            SettingItem(title = "Title1", onClick = {})
            SettingItem(title = "Title1", onClick = {})
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable (() -> Unit) = {
        Icon(imageVector = AiaImages.ChevronRight, contentDescription = null)
    },
) {
    ListItem(
        headlineContent = { Text(text = title, style = MaterialTheme.typography.titleMedium) },
        leadingContent = {
            if (leadingIcon != null)
                Icon(imageVector = leadingIcon, contentDescription = null)
        },
        trailingContent = trailingContent,
        modifier = modifier.clickable {
            onClick()
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Preview
@Composable
private fun SettingItemPreview() {
    Surface {
        SettingItem(
            title = "AI提供商",
            onClick = {},
            leadingIcon = Icons.Rounded.Settings
        )
    }
}


@Preview()
@Composable
private fun SettingContentPreview() {
    Surface {
        SettingContent(
            settingActions = SettingActions(
                onClickServiceProvider = {},
                onClickConversationSetting = {},
                onClickLanguage = {},
                onClickAppearance = {},
                onClickVersion = {},
                onClickLicense = {},
                onClickGithub = {},
                onClickFeedback = {}
            )
        )
    }
}