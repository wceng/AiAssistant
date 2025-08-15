@file:OptIn(ExperimentalMaterial3Api::class)

package com.wceng.app.aiassistant.ui.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wceng.app.aiassistant.domain.model.DarkModeInfo
import com.wceng.app.aiassistant.domain.model.ThemeSchemeInfo
import com.wceng.app.aiassistant.ui.theme.AiaSafeDp
import com.wceng.app.aiassistant.util.LoadingContent
import org.koin.compose.koinInject

@Composable
fun ColorSchemeBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    viewModel: ColorSchemeViewModel = koinInject()
) {
    if (!show) return

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ColorSchemeBottomSheetContent(
        uiState = uiState,
        actions = ColorSchemeActions(
            onDismissRequest = onDismissRequest,
            onSelectThemeScheme = { viewModel.onSelectThemeScheme(it) },
            onSelectDarkModeInfo = { viewModel.onSelectDarkModeInfo(it) }
        )
    )

}

data class ColorSchemeActions(
    val onDismissRequest: () -> Unit,
    val onSelectThemeScheme: (ThemeSchemeInfo) -> Unit,
    val onSelectDarkModeInfo: (DarkModeInfo) -> Unit,
)

@Composable
private fun ColorSchemeBottomSheetContent(
    uiState: ColorSchemeUiState,
    actions: ColorSchemeActions
) {
    ModalBottomSheet(
        onDismissRequest = actions.onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        when (uiState) {
            ColorSchemeUiState.Loading -> LoadingContent()
            is ColorSchemeUiState.Success -> {
                Column(
                    modifier = Modifier.padding(horizontal = AiaSafeDp.safeHorizontal)
                ) {
                    Text(text = "Theme", style = MaterialTheme.typography.titleMedium)
                    ThemeChooserRow(
                        text = "Default",
                        selected = uiState.colorSchemeSettings.themeSchemeInfo == ThemeSchemeInfo.Default,
                        onClick = { actions.onSelectThemeScheme(ThemeSchemeInfo.Default) }
                    )
                    ThemeChooserRow(
                        text = "Dynamic",
                        selected = uiState.colorSchemeSettings.themeSchemeInfo == ThemeSchemeInfo.Dynamic,
                        onClick = { actions.onSelectThemeScheme(ThemeSchemeInfo.Dynamic) }
                    )

                    Spacer(Modifier.height(8.dp))
                    Text(text = "Dark mode", style = MaterialTheme.typography.titleMedium)
                    ThemeChooserRow(
                        text = "System DEFAULT",
                        selected = uiState.colorSchemeSettings.darkModeInfo == DarkModeInfo.System,
                        onClick = { actions.onSelectDarkModeInfo(DarkModeInfo.System) }
                    )
                    ThemeChooserRow(
                        text = "Light",
                        selected = uiState.colorSchemeSettings.darkModeInfo == DarkModeInfo.Light,
                        onClick = { actions.onSelectDarkModeInfo(DarkModeInfo.Light) }
                    )
                    ThemeChooserRow(
                        text = "Dark",
                        selected = uiState.colorSchemeSettings.darkModeInfo == DarkModeInfo.Dark,
                        onClick = { actions.onSelectDarkModeInfo(DarkModeInfo.Dark) }
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(12.dp)

    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )

        Spacer(Modifier.width(8.dp))

        Text(text)
    }
}