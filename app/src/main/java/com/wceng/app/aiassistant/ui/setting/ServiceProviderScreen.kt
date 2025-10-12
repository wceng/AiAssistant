@file:OptIn(ExperimentalMaterial3Api::class)

package com.wceng.app.aiassistant.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.component.AiaAssistChip
import com.wceng.app.aiassistant.component.AiaCircularProgressIndicator
import com.wceng.app.aiassistant.component.AiaMessageConfirmAlertDialog
import com.wceng.app.aiassistant.component.AiaTextFieldAlertDialog
import com.wceng.app.aiassistant.component.LoadingContent
import com.wceng.app.aiassistant.domain.model.SelectableAiProviderModel
import com.wceng.app.aiassistant.domain.model.defaultAiProviders
import com.wceng.app.aiassistant.ui.theme.AiAssistantTheme
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.ui.theme.AiaSafeDp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ServiceProviderScreen(
    viewModel: ServiceProviderViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ServiceProviderContent(
        uiState = uiState,
        onBack = onBack,
        serviceProviderActions = ServiceProviderActions(
            onSaveConfig = viewModel::saveConfig,
            onUpdateApiKey = viewModel::updateApiKey,
            onToggleApiKeyVisibility = viewModel::toggleApiKeyVisibility,
            onUpdateBaseUrl = viewModel::updateBaseUrl,
            onSetSelectedProvider = viewModel::setSelectedProvider,
            onUpdateSelectedModel = viewModel::updateSelectedModel,
            onRefreshModels = viewModel::refreshModels,
            onAddModel = viewModel::addModel,
            onDeleteModel = viewModel::deleteModel,
            onAddCustomServiceProvider = viewModel::addCustomServiceProvider,
            onResetHostUrl = viewModel::resetHostUrl,
            onDeleteServiceProvider = viewModel::deleteServiceProvider
        ),
    )
}

data class ServiceProviderActions(
    val onSaveConfig: () -> Unit,
    val onUpdateApiKey: (String) -> Unit,
    val onToggleApiKeyVisibility: () -> Unit,
    val onUpdateBaseUrl: (String) -> Unit,
    val onSetSelectedProvider: (Long) -> Unit,
    val onUpdateSelectedModel: (String) -> Unit,
    val onRefreshModels: () -> Unit,
    val onAddModel: (String) -> Unit,
    val onDeleteModel: (String) -> Unit,
    val onAddCustomServiceProvider: (String) -> Unit,
    val onResetHostUrl: (Long) -> Unit,
    val onDeleteServiceProvider: (Long) -> Unit
)

@Composable
private fun ServiceProviderContent(
    uiState: ServiceProviderUiState,
    serviceProviderActions: ServiceProviderActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showProviderSheet by remember { mutableStateOf(false) }
    var showCustomServiceProvideDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            ServerProviderTopbar(
                onBack = onBack,
                onDelete = {
                    showDeleteConfirmDialog = true
                },
                onAdd = {
                    showCustomServiceProvideDialog = true
                }
            )
        },
        floatingActionButton = {
            if (uiState is ServiceProviderUiState.Success && uiState.isConfigChanged) {
                SaveConfigFab(onClick = serviceProviderActions.onSaveConfig)
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
                    ListItem(
                        modifier = Modifier.clickable { showProviderSheet = true },
                        headlineContent = { Text(stringResource(R.string.service_provider)) },
                        supportingContent = {
                            Text(
                                uiState.selectableAiProviderModel.selectedAiProvider?.name ?: ""
                            )
                        },
                        trailingContent = { Icon(AiaImages.ArrowDropDown, null) },
                        leadingContent = {
                            Icon(imageVector = AiaImages.AutoAwesome, contentDescription = null)
                        }
                    )

                    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = uiState.baseUrl,
                            singleLine = true,
                            onValueChange = serviceProviderActions.onUpdateBaseUrl,
                            label = { Text(stringResource(R.string.input_host_url_label)) },
                            leadingIcon = {
                                Icon(imageVector = AiaImages.Link, contentDescription = null)
                            },
                            trailingIcon = if (uiState.isShowResetHostUrl) {
                                {
                                    TextButton(onClick = {
                                        uiState.selectableAiProviderModel.selectedAiProvider?.let {
                                            serviceProviderActions.onResetHostUrl(it.id)
                                        }
                                    }) {
                                        Text(text = stringResource(R.string.reset))
                                    }
                                }
                            } else null
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = uiState.apiKey,
                            onValueChange = serviceProviderActions.onUpdateApiKey,
                            label = { Text(stringResource(R.string.input_api_key_label)) },
                            singleLine = true,
                            visualTransformation = if (uiState.showApiKey)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconToggleButton(
                                    checked = uiState.showApiKey,
                                    onCheckedChange = { serviceProviderActions.onToggleApiKeyVisibility() }
                                ) {
                                    Icon(
                                        if (uiState.showApiKey) AiaImages.VisibilityOff
                                        else AiaImages.Visibility,
                                        stringResource(R.string.toggle_visibility)
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(imageVector = AiaImages.Key, contentDescription = null)
                            }
                        )
                    }

                    ModelsPanel(
                        models = uiState.selectableAiProviderModel.selectedAiProvider?.availableModels ?: emptyList(),
                        selectedModel = uiState.selectableAiProviderModel.selectedAiProvider?.selectedModel
                            ?: "",
                        isFetching = uiState.isModelsRefreshing,
                        onFetchModels = serviceProviderActions.onRefreshModels,
                        onAddModel = serviceProviderActions.onAddModel,
                        onSelectModel = serviceProviderActions.onUpdateSelectedModel,
                        onDeleteModel = serviceProviderActions.onDeleteModel
                    )
                }


                AiProviderBottomSheet(
                    show = showProviderSheet,
                    onDismissRequest = { showProviderSheet = false },
                    selectableAiProviderModel = uiState.selectableAiProviderModel,
                    onProviderSelected = serviceProviderActions.onSetSelectedProvider,
                )
            }
        }
    }

    if (showCustomServiceProvideDialog)
        AiaTextFieldAlertDialog(
            title = stringResource(R.string.custom_service_provider),
            onDismissRequest = { showCustomServiceProvideDialog = false },
            confirmButtonText = stringResource(R.string.confirm),
            onConfirmAction = serviceProviderActions.onAddCustomServiceProvider,
            icon = AiaImages.Add,
            requestFocus = true,
            singleLine = true,
        )

    if (showDeleteConfirmDialog)
        if (uiState is ServiceProviderUiState.Success) {
            val name = uiState.selectableAiProviderModel.selectedAiProvider?.name ?: ""
            AiaMessageConfirmAlertDialog(
                title = stringResource(R.string.confirm_delete_title),
                onDismissRequest = { showDeleteConfirmDialog = false },
                onConfirmAction = {
                    uiState.selectableAiProviderModel.selectedAiProvider?.let {
                        serviceProviderActions.onDeleteServiceProvider(it.id)
                    }
                    showDeleteConfirmDialog = false
                },
                text = stringResource(
                    R.string.delete_service_provider_message, name
                ),
                icon = AiaImages.Delete
            )
        }
}

@Composable
private fun ModelsPanel(
    models: List<String>,
    selectedModel: String,
    isFetching: Boolean,
    onFetchModels: () -> Unit,
    onAddModel: (String) -> Unit,
    onDeleteModel: (String) -> Unit,
    onSelectModel: (String) -> Unit
) {
    var showAddModelDialog by remember { mutableStateOf(false) }
    var editable by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    ) {
                        append("${stringResource(R.string.setting_model)}: ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(selectedModel)
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { editable = editable.not() }) {
                    Icon(
                        imageVector = AiaImages.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = if (editable)
                            MaterialTheme.colorScheme.primary
                        else
                            LocalContentColor.current
                    )
                }
                IconButton(
                    onClick = onFetchModels,
                    enabled = isFetching.not()
                ) {
                    if (isFetching)
                        AiaCircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    else
                        Icon(
                            imageVector = AiaImages.Refresh,
                            contentDescription = stringResource(R.string.refresh_models)
                        )
                }

                IconButton(onClick = {
                    showAddModelDialog = true
                }) {
                    Icon(
                        imageVector = AiaImages.Add,
                        contentDescription = stringResource(R.string.add_model)
                    )
                }
            }
        }

        if (models.isNotEmpty())
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.small,
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    models.forEach {
                        key(it) {
                            if (editable)
                                AiaAssistChip(
                                    label = it,
                                    selected = it == selectedModel,
                                    onClick = { onDeleteModel(it) },
                                    leadingIconImageVector = AiaImages.Delete,
                                )
                            else
                                AiaAssistChip(
                                    label = it,
                                    selected = it == selectedModel,
                                    onClick = { onSelectModel(it) }
                                )
                        }
                    }
                }
            }
    }

    if (showAddModelDialog) {
        AiaTextFieldAlertDialog(
            title = stringResource(R.string.add_custom_model),
            onDismissRequest = { showAddModelDialog = false },
            confirmButtonText = stringResource(R.string.confirm),
            onConfirmAction = onAddModel,
            requestFocus = true,
            singleLine = true,
            icon = AiaImages.Add
        )
    }
}

@Composable
private fun AiProviderBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    selectableAiProviderModel: SelectableAiProviderModel,
    onProviderSelected: (Long) -> Unit,
) {
    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            LazyColumn {
                items(selectableAiProviderModel.aiProviders) { provider ->
                    ListItem(
                        modifier = Modifier.clickable {
                            onProviderSelected(provider.id)
                            onDismissRequest()
                        },
                        headlineContent = { Text(provider.name) },
                        trailingContent = {
                            if (provider == selectableAiProviderModel.selectedAiProvider) {
                                Icon(AiaImages.Done, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        leadingContent = {
                            Icon(imageVector = AiaImages.AutoAwesome, contentDescription = null)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerProviderTopbar(
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onAdd: () -> Unit
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(AiaImages.ArrowBack, contentDescription = stringResource(R.string.back))
            }
        },
        actions = {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = AiaImages.Delete,
                    contentDescription = stringResource(R.string.delete)
                )
            }

            IconButton(onClick = onAdd) {
                Icon(imageVector = AiaImages.Add, contentDescription = stringResource(R.string.add))
            }
        })
}

@Composable
private fun SaveConfigFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(imageVector = AiaImages.Save, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text = stringResource(R.string.save_config))
    }
}


@Preview
@Composable
private fun ServiceProviderContentPreview() {
    AiAssistantTheme {
        Surface {
            ServiceProviderContent(
                uiState = ServiceProviderUiState.Success(
                    selectableAiProviderModel = SelectableAiProviderModel(
                        defaultAiProviders.first(),
                        defaultAiProviders
                    ),
                    isConfigChanged = true,
                    apiKey = "sk-abcdefg",
                    baseUrl = "https://api.openai.com/v1",
                    showApiKey = true,
                    isModelsRefreshing = true,
                    isShowResetHostUrl = true
                ),
                serviceProviderActions = ServiceProviderActions(
                    onSaveConfig = { },
                    onUpdateApiKey = { },
                    onToggleApiKeyVisibility = { },
                    onUpdateBaseUrl = { },
                    onSetSelectedProvider = { },
                    onUpdateSelectedModel = { },
                    onRefreshModels = { },
                    onAddModel = { },
                    onDeleteModel = { },
                    onAddCustomServiceProvider = { },
                    onResetHostUrl = { },
                    onDeleteServiceProvider = { }
                ),
                onBack = {},
            )
        }
    }
}
