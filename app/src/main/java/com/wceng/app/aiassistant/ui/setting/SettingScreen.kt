@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.component.AiaLargeTopBar
import com.wceng.app.aiassistant.ui.DevicePreview
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

    var showLanguageBottomSheet by remember {
        mutableStateOf(false)
    }

    SettingContent(
        modifier = modifier,
        settingActions = SettingActions(
            onClickServiceProvider = onNavigateToServerProviderScreen,
            onClickConversationSetting = {},
            onClickLanguage = {
                showLanguageBottomSheet = true
            },
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

    LanguageBottomSheet(
        show = showLanguageBottomSheet,
        onDismissRequest = {
            showLanguageBottomSheet = false
        },
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            AiaLargeTopBar(
                titleRes = R.string.setting_title,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = AiaSafeDp.safeHorizontal),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SettingItemPanel(
                title = stringResource(R.string.service_provider),
            ) {
                SettingItem(
                    title = stringResource(R.string.service_provider),
                    onClick = settingActions.onClickServiceProvider,
                    leadingIcon = AiaImages.Cloud
                )
            }

            SettingItemPanel(
                title = stringResource(R.string.setting_conversation_section),
            ) {
                SettingItem(
                    title = stringResource(R.string.setting_conversation_section),
                    onClick = settingActions.onClickConversationSetting,
                    leadingIcon = AiaImages.Forum
                )
            }

            SettingItemPanel(
                title = stringResource(R.string.setting_display_section),
            ) {
                SettingItem(
                    title = stringResource(R.string.setting_language),
                    onClick = settingActions.onClickLanguage,
                    leadingIcon = AiaImages.Language
                )

                SettingItem(
                    title = stringResource(R.string.setting_appearance),
                    onClick = settingActions.onClickAppearance,
                    leadingIcon = AiaImages.Palette
                )
            }

            SettingItemPanel(
                title = stringResource(R.string.setting_about_section),
            ) {
                SettingItem(
                    title = stringResource(R.string.setting_version),
                    onClick = settingActions.onClickVersion,
                    leadingIcon = AiaImages.Info
                )

                SettingItem(
                    title = stringResource(R.string.setting_license),
                    onClick = settingActions.onClickLicense,
                    leadingIcon = AiaImages.Description
                )

                SettingItem(
                    title = stringResource(R.string.setting_github),
                    onClick = settingActions.onClickGithub,
                    leadingIcon = AiaImages.Code
                )

                SettingItem(
                    title = stringResource(R.string.setting_feedback),
                    onClick = settingActions.onClickFeedback,
                    leadingIcon = AiaImages.Feedback
                )
            }
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
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides TextStyle(
                    color = MaterialTheme.colorScheme.onSurface
                )
            ) {
                content()
            }
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


@DevicePreview
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