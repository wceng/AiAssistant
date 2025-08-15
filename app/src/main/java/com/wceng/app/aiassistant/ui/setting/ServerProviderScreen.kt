@file:OptIn(ExperimentalMaterial3Api::class)

package com.wceng.app.aiassistant.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wceng.app.aiassistant.domain.model.AiProviderInfo
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.ui.theme.AiaSafeDp
import com.wceng.app.aiassistant.util.LoadingContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ServerProviderScreen(
    viewModel: ServiceProviderViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ServerProviderContent(
        uiState = uiState,
        onBack = onBack,
        serverProviderActions = ServerProviderActions(
            onSaveConfig = viewModel::saveConfig,
            onUpdateApiKey = viewModel::updateApiKey,
            onToggleApiKeyVisibility = viewModel::toggleApiKeyVisibility,
            onUpdateBaseUrl = viewModel::updateBaseUrl,
            onSetSelectedProvider = viewModel::setSelectedProvider,
            onUpdateSelectedModel = viewModel::updateSelectedModel
        ),
    )
}

data class ServerProviderActions(
    val onSaveConfig: () -> Unit,
    val onUpdateApiKey: (String) -> Unit,
    val onToggleApiKeyVisibility: () -> Unit,
    val onUpdateBaseUrl: (String) -> Unit,
    val onSetSelectedProvider: (String) -> Unit,
    val onUpdateSelectedModel: (String) -> Unit
)

@Composable
private fun ServerProviderContent(
    uiState: ServiceProviderUiState,
    serverProviderActions: ServerProviderActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showProviderSheet by remember { mutableStateOf(false) }
    var showModelSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            ServerProviderTopbar(onBack = onBack)
        },
        floatingActionButton = {
            if (uiState is ServiceProviderUiState.Success && uiState.isConfigChanged) {
                SaveConfigFab(onClick = serverProviderActions.onSaveConfig)
            }
        }
    ) { paddingValues ->
        when (uiState) {
            ServiceProviderUiState.Loading -> LoadingContent()
            is ServiceProviderUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(AiaSafeDp.safeHorizontal),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingItemPanel(title = "AI Provider") {
                        ListItem(
                            modifier = Modifier.clickable { showProviderSheet = true },
                            headlineContent = { Text("Current Provider") },
                            supportingContent = { Text(uiState.aiProviderConfigInfo.selectedAiProviderName) },
                            trailingContent = { Icon(AiaImages.ArrowDropDown, null) }
                        )
                    }

                    SettingItemPanel(title = "API Configuration") {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = uiState.apiKey,
                                onValueChange = serverProviderActions.onUpdateApiKey,
                                label = { Text("API Key") },
                                singleLine = true,
                                visualTransformation = if (uiState.showApiKey)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconToggleButton(
                                        checked = uiState.showApiKey,
                                        onCheckedChange = { serverProviderActions.onToggleApiKeyVisibility() }
                                    ) {
                                        Icon(
                                            if (uiState.showApiKey) AiaImages.VisibilityOff
                                            else AiaImages.Visibility,
                                            "Toggle visibility"
                                        )
                                    }
                                }
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = uiState.baseUrl,
                                singleLine = true,
                                onValueChange = serverProviderActions.onUpdateBaseUrl,
                                label = { Text("Host URL") }
                            )
                        }
                    }

                    SettingItemPanel(title = "Model") {
                        ListItem(
                            modifier = Modifier.clickable { showModelSheet = true },
                            headlineContent = { Text("Selected Model") },
                            supportingContent = {
                                Text(uiState.aiProviderConfigInfo.selectedAiProviderInfo.selectedModel)
                            },
                            trailingContent = {
                                Icon(AiaImages.ArrowDropDown, null)
                            }
                        )
                    }
                }

                AiProviderBottomSheet(
                    show = showProviderSheet,
                    onDismissRequest = { showProviderSheet = false },
                    providers = uiState.aiProviderConfigInfo.aiProviderInfos,
                    currentProvider = uiState.aiProviderConfigInfo.selectedAiProviderName,
                    onProviderSelected = serverProviderActions.onSetSelectedProvider
                )

                ModelSelectionBottomSheet(
                    show = showModelSheet,
                    onDismissRequest = { showModelSheet = false },
                    models = uiState.aiProviderConfigInfo.selectedAiProviderInfo.models,
                    selectedModel = uiState.aiProviderConfigInfo.selectedAiProviderInfo.selectedModel,
                    onModelSelected = serverProviderActions.onUpdateSelectedModel
                )
            }
        }
    }
}

@Composable
private fun AiProviderBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    providers: List<AiProviderInfo>,
    currentProvider: String,
    onProviderSelected: (String) -> Unit
) {
    if (show) {
        ModalBottomSheet(onDismissRequest = onDismissRequest) {
            LazyColumn {
                items(providers) { provider ->
                    ListItem(
                        modifier = Modifier.clickable {
                            onProviderSelected(provider.name)
                            onDismissRequest()
                        },
                        headlineContent = { Text(provider.name) },
                        trailingContent = {
                            if (provider.name == currentProvider) {
                                Icon(AiaImages.Done, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ModelSelectionBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    models: List<String>,
    selectedModel: String,
    onModelSelected: (String) -> Unit
) {
    if (show) {
        ModalBottomSheet(onDismissRequest = onDismissRequest) {
            LazyColumn {
                items(models) { model ->
                    ListItem(
                        modifier = Modifier.clickable {
                            onModelSelected(model)
                            onDismissRequest()
                        },
                        headlineContent = { Text(model) },
                        trailingContent = {
                            if (model == selectedModel) {
                                Icon(AiaImages.Done, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ServerProviderTopbar(
    onBack: () -> Unit
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(AiaImages.ArrowBack, contentDescription = "Back")
            }
        })
}

@Composable
private fun SaveConfigFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(text = "保存配置")
        Spacer(Modifier.width(8.dp))
        Icon(imageVector = AiaImages.Save, contentDescription = null)
    }
}
