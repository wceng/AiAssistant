@file:OptIn(ExperimentalMaterial3Api::class)

package com.wceng.app.aiassistant.ui.setting

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.component.LoadingContent
import org.koin.androidx.compose.koinViewModel

@Composable
fun LanguageBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    viewModel: LanguageViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (show.not()) return

    LanguageBottomSheetContent(
        uiState = uiState,
        onDismissRequest = onDismissRequest,
        onSelectLanguage = {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(it)
            )
//            viewModel.selectLanguage(it)
        }
    )
}

@Composable
private fun LanguageBottomSheetContent(
    uiState: LanguageUiState,
    onDismissRequest: () -> Unit,
    onSelectLanguage: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        when (uiState) {
            LanguageUiState.Loading -> LoadingContent()
            is LanguageUiState.Success -> {
                LazyColumn {
                    items(
                        items = uiState.selectableLanguageInfoModel.languageInfos,
                        key = { it.language }
                    ) {
                        val selected =
                            getCurrentLanguage() == it.language
                        println(getCurrentLanguage() + "   " + it.language)

                        ListItem(
                            modifier = Modifier.clickable {
                                onSelectLanguage(it.language)
                                onDismissRequest()
                            },
                            headlineContent = {
                                Text(text = it.name)
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            trailingContent = if (selected) {
                                {
                                    Icon(imageVector = AiaImages.Done, contentDescription = null)
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

private fun getCurrentLanguage(): String? {
    val locales = AppCompatDelegate.getApplicationLocales()
    val primaryLocale = locales[0] // 获取第一个（主要）语言区域

    if (primaryLocale != null) {
        return primaryLocale.language // "en", "zh", "es" 等
    }

    return null
}