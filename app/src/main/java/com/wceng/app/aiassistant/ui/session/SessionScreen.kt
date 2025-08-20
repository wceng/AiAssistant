package com.wceng.app.aiassistant.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.domain.model.ConversationGroup
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.ui.theme.AiaSafeDp
import com.wceng.app.aiassistant.util.ErrorContent
import com.wceng.app.aiassistant.util.LoadingContent
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
        Icon(imageVector = AiaImages.Add, contentDescription = stringResource(R.string.create_new_conversation))
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
            val title = when (group) {
                is ConversationGroup.SimpleGroup -> stringResource(group.titleRes)
                is ConversationGroup.YearMonthGroup -> stringResource(
                    R.string.group_year_month_format,
                    group.year,
                    group.month
                )
            }
            GroupHeader(title = title)
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

