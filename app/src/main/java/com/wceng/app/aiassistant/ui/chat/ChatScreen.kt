@file:OptIn(ExperimentalMaterial3Api::class)

package com.wceng.app.aiassistant.ui.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.component.AiaTextFieldAlertDialog
import com.wceng.app.aiassistant.component.LoadingContent
import com.wceng.app.aiassistant.component.MarkDownActions
import com.wceng.app.aiassistant.domain.model.BubbleToMessages
import com.wceng.app.aiassistant.domain.model.Sender
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.ui.theme.AiaSafeDp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatScreen(
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    viewModel: ChatViewModel = koinViewModel<ChatViewModel>(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val messagesUiState by viewModel.messagesUiState.collectAsStateWithLifecycle()
    val chatUiState by viewModel.chatUiState.collectAsStateWithLifecycle()

    var showEditConversationTitleDialog by remember { mutableStateOf(false) }

    fun copyToClipboard(content: String) = coroutineScope.launch {
        clipboardManager.setText(AnnotatedString(content))
    }

    ChatContent(
        showBackButton = showBackButton,
        messagesUiState = messagesUiState,
        chatUiState = chatUiState,
        snackbarHostState = snackbarHostState,
        markDownActions = MarkDownActions(
            onCopyCode = ::copyToClipboard
        ),
        messageActions = MessageActions(
            onSendMessage = viewModel::sendMessage,
            onClearAllMessages = viewModel::clearMessages,
            onCopyMessageContent = ::copyToClipboard,
            onRetrySendUserMessage = viewModel::retrySendUserMessage,
            onDeleteMessage = {

            },
            onRetryResponseAiMessage = viewModel::retryResponseAssistantMessage,
            onToggleMessage = viewModel::toggleMessage,
            onCancelReceiveMessage = viewModel::cancelReceiveMessage,
            onNewChat = {

            },
            onRenameConversationTitle = {
                showEditConversationTitleDialog = true
            }
        ),
        onBack = onBack
    )

    if (showEditConversationTitleDialog) {
        AiaTextFieldAlertDialog(
            title = stringResource(R.string.rename_conversation_title),
            initialValue = chatUiState.convTitle,
            onDismissRequest = { showEditConversationTitleDialog = false },
            confirmButtonText = stringResource(R.string.confirm),
            onConfirmAction = viewModel::renameCurrentConversationTitle,
            requestFocus = true,
            singleLine = true,
            icon = AiaImages.Edit
        )
    }
}

data class MessageActions(
    val onSendMessage: (String, String?) -> Unit,
    val onClearAllMessages: () -> Unit,
    val onCopyMessageContent: (String) -> Unit,
    val onRetrySendUserMessage: (id: Long, content: String) -> Unit,
    val onRetryResponseAiMessage: (aiMessageId: Long) -> Unit,
    val onDeleteMessage: (id: String) -> Unit,
    val onToggleMessage: (targetMessageId: Long) -> Unit,
    val onCancelReceiveMessage: () -> Unit,
    val onNewChat: () -> Unit,
    val onRenameConversationTitle: () -> Unit
)

@Composable
fun ChatContent(
    showBackButton: Boolean,
    messagesUiState: MessagesUiState,
    chatUiState: ChatUiState,
    snackbarHostState: SnackbarHostState,
    messageActions: MessageActions,
    markDownActions: MarkDownActions,
    onBack: () -> Unit = {},
    lazyListState: LazyListState = rememberLazyListState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val jumpToBottomButtonEnabled by remember {
        derivedStateOf {
            lazyListState.enableJumpToBottomButton()
        }
    }

    fun scrollToBottom(delayMs: Long = 0) {
        if (messagesUiState is MessagesUiState.Success
            && messagesUiState.bubbleToMessages.isNotEmpty()
        )
            coroutineScope.launch {
                delayMs.takeIf { it > 0L }?.let { delay(it) }
                lazyListState.animateScrollToItem(messagesUiState.bubbleToMessages.size + 1)
            }
    }

    fun clearInputFocus() {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            ChatTopAppBar(
                title = chatUiState.convTitle,
                onClearAll = messageActions.onClearAllMessages,
                showBackButton = showBackButton,
                onBack = onBack,
                onNewChat = messageActions.onNewChat,
                onEdit = messageActions.onRenameConversationTitle,
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            MessageInput(
                onSend = {
                    messageActions.onSendMessage(it, chatUiState.convPrompt)
                    clearInputFocus()
                    scrollToBottom(100)
                },
                onStop = messageActions.onCancelReceiveMessage,
                isLoading = chatUiState.isReceiving,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        floatingActionButton = {
            if (jumpToBottomButtonEnabled) {
                JumpToBottomButton {
                    scrollToBottom()
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when (messagesUiState) {
                    is MessagesUiState.Error, MessagesUiState.Idle -> Unit
                    is MessagesUiState.Loading -> LoadingContent()
                    is MessagesUiState.Success -> {
                        LazyColumn(
                            reverseLayout = false,
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            state = lazyListState,
                            userScrollEnabled = true,
                            contentPadding = PaddingValues(horizontal = AiaSafeDp.safeHorizontal),
                        ) {
                            chatUiState.convPrompt.let(::prompt)

                            messageList(
                                bubbleToMessages = messagesUiState.bubbleToMessages,
                                markDownActions = markDownActions,
                                messageActions = messageActions,
                            )
                        }
                    }

                }
            }
        }
    }

    LaunchedEffect(chatUiState.convId) {
        scrollToBottom()
    }
}

private fun LazyListScope.prompt(prompt: String) {
    item {
        var expended by rememberSaveable { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .animateContentSize()
                    .clickable {
                        expended = expended.not()
                    }, contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                if (expended) {
                    Text(
                        text = prompt,
                    )
                } else {
                    Text(
                        text = prompt,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

fun LazyListScope.messageList(
    bubbleToMessages: List<BubbleToMessages>,
    markDownActions: MarkDownActions,
    messageActions: MessageActions,
) {
    items(
        items = bubbleToMessages,
        key = { it.bubble.id },
    ) { bubble ->
        MessageItem(
            modifier = Modifier.animateContentSize(),
            bubbleToMessages = bubble,
            markDownActions = markDownActions,
            onCopyClick = {
                bubble.currentVersionMessage ?: return@MessageItem
                messageActions.onCopyMessageContent(bubble.currentVersionMessage.content)
            },
            onRetrySendUserMessage = {
                bubble.currentVersionMessage ?: return@MessageItem
                messageActions.onRetrySendUserMessage(
                    bubble.currentVersionMessage.id, it
                )
            },
            onRetryResponseAiMessage = {
                bubble.currentVersionMessage ?: return@MessageItem
                messageActions.onRetryResponseAiMessage(
                    bubble.currentVersionMessage.id
                )
            },
            onToggleMessage = messageActions.onToggleMessage
        )
    }
}

@Composable
private fun MessageItem(
    modifier: Modifier = Modifier,
    bubbleToMessages: BubbleToMessages,
    markDownActions: MarkDownActions,
    onCopyClick: () -> Unit,
    onRetrySendUserMessage: (String) -> Unit,
    onRetryResponseAiMessage: () -> Unit,
    onToggleMessage: (Long) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = when (bubbleToMessages.bubble.sender) {
            Sender.Ai -> Alignment.CenterStart
            Sender.User -> Alignment.CenterEnd
        }
    ) {
        Column {
            when (bubbleToMessages.bubble.sender) {
                Sender.Ai -> AssistantMessageBubble(
                    bubbleToMsg = bubbleToMessages,
                    markDownActions = markDownActions,
                    onCopyClick = onCopyClick,
                    onRetryClick = onRetryResponseAiMessage,
                    onToggleMessage = onToggleMessage,
                )

                Sender.User -> UserMessageBubble(
                    bubbleToMsg = bubbleToMessages,
                    onCopyClick = onCopyClick,
                    onRetrySendUserMessage = onRetrySendUserMessage,
                    onToggleMessage = onToggleMessage,
                )
            }
        }
    }
}

fun LazyListState.enableJumpToBottomButton(): Boolean {
    if (layoutInfo.totalItemsCount <= 0) return false

    val bottomOffset = layoutInfo.visibleItemsInfo.lastOrNull()?.offset?.let {
        it + layoutInfo.visibleItemsInfo.last().size - layoutInfo.viewportEndOffset
    } ?: 0

    return layoutInfo.visibleItemsInfo.last().index != layoutInfo.totalItemsCount - 1
            || bottomOffset > JumpToBottomThreshold
}

private const val JumpToBottomThreshold = 240