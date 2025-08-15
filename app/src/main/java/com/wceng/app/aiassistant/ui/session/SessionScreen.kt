package com.wceng.app.aiassistant.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.domain.model.Conversation
import com.wceng.app.aiassistant.domain.model.ConversationGroup
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.ui.theme.AiaSafeDp
import com.wceng.app.aiassistant.util.ErrorContent
import com.wceng.app.aiassistant.util.LoadingContent
import com.wceng.app.aiassistant.util.datetime.formatAsRelativeTime
import kotlinx.coroutines.flow.filterNotNull
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SessionScreen(
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = koinViewModel(),
    onSelectSession: (Long) -> Unit,
    highlightSelectedConversation: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onSelectSession by viewModel.onSelectConversationId.collectAsStateWithLifecycle()

    SessionContent(
        modifier = modifier,
        uiState = uiState,
        sessionActions = SessionActions(
            onCreateNew = viewModel::createNewSession,
            onSelectSession = {
                viewModel.setSelectedConversation(it)
                onSelectSession(it)
            },
            onDeleteSession = viewModel::deleteSession,
            onUpdateSessionTitle = viewModel::updateSessionTitle,
        ),
        highlightSelectedConversation = highlightSelectedConversation
    )

    LaunchedEffect(Unit) {
        snapshotFlow { onSelectSession }
            .filterNotNull()
            .collect {
                onSelectSession(it)
            }
    }
}

data class SessionActions(
    val onCreateNew: () -> Unit,
    val onSelectSession: (id: Long) -> Unit,
    val onDeleteSession: (id: Long) -> Unit,
    val onUpdateSessionTitle: (Long, String) -> Unit,
)

@Composable
private fun SessionContent(
    modifier: Modifier,
    uiState: SessionsUiState,
    sessionActions: SessionActions,
    highlightSelectedConversation: Boolean
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            CreateSessionFab(onCreateNew = sessionActions.onCreateNew)
        }
    ) { paddingValues ->
        when (uiState) {
            is SessionsUiState.Error -> ErrorContent(errorMessage = uiState.message)
            SessionsUiState.Loading -> LoadingContent(Modifier.padding(paddingValues))
            is SessionsUiState.Success ->
                LazyColumn(
                    modifier = Modifier.padding(paddingValues = paddingValues)
                ) {
                    groupedSessionList(
                        uiState.groupedSessions,
                        sessionActions,
                        highlightSelectedConversation = highlightSelectedConversation
                    )
                }
        }
    }
}

@Composable
private fun CreateSessionFab(
    onCreateNew: () -> Unit
) {
    FloatingActionButton(onClick = onCreateNew) {
        Icon(imageVector = AiaImages.Add, contentDescription = "创建新会话")
    }
}

fun LazyListScope.groupedSessionList(
    groupedSessions: List<ConversationGroup>,
    sessionActions: SessionActions,
    selectedConversationId: Long? = null,
    highlightSelectedConversation: Boolean = false
) {
    println("session list happen recomposable. current sessionId is: $selectedConversationId. group num is ${groupedSessions.size}")

    groupedSessions.forEach { group ->
        stickyHeader {
            GroupHeader(group.title)
        }

        items(
            items = group.conversations,
            key = { it.id }
        ) { conversation ->
            val selected =
                highlightSelectedConversation && selectedConversationId == conversation.id
            SessionListItem(
                conversation = conversation,
                onClick = { sessionActions.onSelectSession(conversation.id) },
                onDelete = { sessionActions.onDeleteSession(conversation.id) },
                onUpdateSessionTitle = { sessionActions.onUpdateSessionTitle(conversation.id, it) },
                selected = selected
            )
        }
    }
}

@Composable
private fun GroupHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = AiaSafeDp.safeHorizontal, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun SessionListItem(
    conversation: Conversation,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onUpdateSessionTitle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(conversation.title) }

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isEditing) {
                onClick()
            },

        colors = ListItemDefaults.colors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        leadingContent = {
            Icon(
                imageVector = AiaImages.Chat,
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        headlineContent = {
            if (isEditing) {
                var isFocused by remember { mutableStateOf(false) }
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                TextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (isFocused && !focusState.isFocused) {
                                onUpdateSessionTitle(editedTitle)
                                isEditing = false
                            }
                            isFocused = focusState.isFocused
                        },
                    textStyle = MaterialTheme.typography.titleMedium,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onUpdateSessionTitle(editedTitle)
                            isEditing = false
                        }
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    )
                )
            } else {
                Text(
                    text = conversation.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        },
        supportingContent = {
            if (!isEditing) {
                Text(
                    text = conversation.lastUpdatedTime.formatAsRelativeTime(),
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        trailingContent = {
            if (isEditing) {
                IconButton(
                    onClick = {
                        onUpdateSessionTitle(editedTitle)
                        isEditing = false
                    }
                ) {
                    Icon(
                        imageVector = AiaImages.Done,
                        contentDescription = stringResource(R.string.save),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                IconButton(
                    onClick = { showMenu = true },
                ) {
                    Icon(
                        imageVector = AiaImages.MoreVert,
                        contentDescription = stringResource(R.string.more_operates),
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        onClick = {
                            showMenu = false
                            isEditing = true
                            editedTitle = conversation.title
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    )
}

