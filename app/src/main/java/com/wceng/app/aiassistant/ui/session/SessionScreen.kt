@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)

package com.wceng.app.aiassistant.ui.session

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.component.AiaMessageConfirmAlertDialog
import com.wceng.app.aiassistant.component.ErrorContent
import com.wceng.app.aiassistant.component.LoadingContent
import com.wceng.app.aiassistant.domain.model.Conversation
import com.wceng.app.aiassistant.domain.model.ConversationGroup
import com.wceng.app.aiassistant.ui.DevicePreview
import com.wceng.app.aiassistant.ui.LocalWindowWidthSize
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.ui.theme.AiaSafeDp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SessionScreen(
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = koinViewModel(),
    onSelectSession: (Long) -> Unit,
    highlightSelectedConversation: Boolean = false
) {
    val sessionsState by viewModel.sessionsState.collectAsStateWithLifecycle()
    val onOpenSession by viewModel.onOpenConversationId.collectAsStateWithLifecycle()
    val selectionModeState by viewModel.selectionModeState.collectAsStateWithLifecycle()
    var showDeleteSelectedConversationsDialog by remember { mutableStateOf(false) }

    SessionContent(
        modifier = modifier,
        sessionsState = sessionsState,
        selectionModeState = selectionModeState,
        sessionActions = SessionActions(
            onCreateNew = viewModel::createAndOpenNewConversation,
            onSelectSession = {
                viewModel.setSelectedConversation(it)
                viewModel.openConversation(it)
            },
            onUpdateSessionTitle = viewModel::updateSessionTitle,
            onDisableSelectionMode = viewModel::disableSelectionMode,
            onDeleteSelectedConversation = {
                showDeleteSelectedConversationsDialog = true
            },
            onToggleSelectedItem = viewModel::toggleSelectedItem,
            onEnableSelectionMode = viewModel::enableSelectionMode
        ),
        highlightSelectedConversation = highlightSelectedConversation
    )


    onOpenSession?.let {
        LaunchedEffect(it) {
            onSelectSession(it)
            viewModel.setConversationOpened()
        }
    }

    BackHandler(enabled = selectionModeState.isActive) {
        viewModel.disableSelectionMode()
    }


    if (showDeleteSelectedConversationsDialog) {
        AiaMessageConfirmAlertDialog(
            title = stringResource(R.string.confirm_delete_title),
            onDismissRequest = { showDeleteSelectedConversationsDialog = false },
            onConfirmAction = viewModel::deleteSelectedConversations,
            text = stringResource(R.string.delete_selected_conversations_message),
            icon = AiaImages.Delete
        )
    }
}

data class SessionActions(
    val onCreateNew: () -> Unit = {},
    val onSelectSession: (id: Long) -> Unit = {},
    val onUpdateSessionTitle: (Long, String) -> Unit = { _, _ -> },
    val onDisableSelectionMode: () -> Unit = {},
    val onDeleteSelectedConversation: () -> Unit = {},
    val onToggleSelectedItem: (Long, Boolean) -> Unit = { _, _ -> },
    val onEnableSelectionMode: () -> Unit = {},
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SessionContent(
    sessionsState: SessionsUiState,
    selectionModeState: SelectionModeState,
    sessionActions: SessionActions,
    highlightSelectedConversation: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            AnimatedVisibility(selectionModeState.isActive.not()) {
                CreateSessionFab(onCreateNew = sessionActions.onCreateNew)
            }
        },
        topBar = {
            ConversationTopBar(
                selectionModeState = selectionModeState,
                scrollBehavior = scrollBehavior,
                onDisableSelectionMode = sessionActions.onDisableSelectionMode,
                onDelete = sessionActions.onDeleteSelectedConversation
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (sessionsState) {
                is SessionsUiState.Error -> ErrorContent(errorMessage = sessionsState.message)
                SessionsUiState.Loading -> LoadingContent()
                is SessionsUiState.Success -> LazyColumn {
                    groupedSessionList(
                        groupedSessions = sessionsState.groupedSessions,
                        selectionModeState = selectionModeState,
                        sessionActions = sessionActions,
                        selectedConversationId = sessionsState.selectedSessionId,
                        highlightSelectedConversation = highlightSelectedConversation
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateSessionFab(
    onCreateNew: () -> Unit
) {
    val expended = if (LocalInspectionMode.current) false
    else LocalWindowWidthSize.current != WindowWidthSizeClass.Compact

    if (expended) {
        ExtendedFloatingActionButton(onClick = onCreateNew) {
            Icon(
                imageVector = AiaImages.Add,
                contentDescription = stringResource(R.string.create_new_conversation)
            )
            Text(text = stringResource(R.string.create_new_conversation))
        }
    } else {
        FloatingActionButton(onClick = onCreateNew) {
            Icon(
                imageVector = AiaImages.Add,
                contentDescription = stringResource(R.string.create_new_conversation)
            )
        }
    }
}

fun LazyListScope.groupedSessionList(
    groupedSessions: List<ConversationGroup>,
    sessionActions: SessionActions,
    selectionModeState: SelectionModeState,
    selectedConversationId: Long? = null,
    highlightSelectedConversation: Boolean = false
) {
    groupedSessions.forEach { group ->
        stickyHeader {
            val title = when (group) {
                is ConversationGroup.SimpleGroup -> stringResource(group.titleRes)
                is ConversationGroup.YearMonthGroup -> stringResource(
                    R.string.group_year_month_format, group.year, group.month
                )
            }
            GroupHeader(title = title)
        }

        items(
            items = group.conversations, key = { it.id }) { conversation ->
            val selected =
                highlightSelectedConversation && selectedConversationId == conversation.id
            SessionListItem(
                conversation = conversation,
                onOpenConversation = { sessionActions.onSelectSession(conversation.id) },
//                onUpdateSessionTitle = { sessionActions.onUpdateSessionTitle(conversation.id, it) },
                selected = selected,
                modifier = Modifier.animateItem(),
                selectionModeState = selectionModeState,
                onEnableSelectionMode = sessionActions.onEnableSelectionMode,
                onToggleSelectedItem = { sessionActions.onToggleSelectedItem(conversation.id, it) })
        }
    }
}

@Composable
private fun GroupHeader(
    title: String, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = AiaSafeDp.safeHorizontal, top = 12.dp, bottom = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@DevicePreview
@Composable
private fun SessionContentSuccess(
    @PreviewParameter(ConversationPreviewParamProvider::class) conversations: List<Conversation>
) {
    SessionContent(
        sessionsState = SessionsUiState.Success(
            selectedSessionId = 1,
            groupedSessions = listOf(
                ConversationGroup.SimpleGroup(
                    titleRes = R.string.group_today, conversations = conversations
                )
            ),
        ),
        sessionActions = SessionActions(),
        highlightSelectedConversation = true,
        selectionModeState = SelectionModeState()
    )
}

@DevicePreview
@Composable
private fun SessionContentSelectionModeEnabled(
    @PreviewParameter(ConversationPreviewParamProvider::class) conversations: List<Conversation>
) {
    SessionContent(
        sessionsState = SessionsUiState.Success(
            selectedSessionId = 1,
            groupedSessions = listOf(
                ConversationGroup.SimpleGroup(
                    titleRes = R.string.group_today, conversations = conversations
                )
            ),
        ),
        sessionActions = SessionActions(),
        highlightSelectedConversation = true,
        selectionModeState = SelectionModeState()
    )
}