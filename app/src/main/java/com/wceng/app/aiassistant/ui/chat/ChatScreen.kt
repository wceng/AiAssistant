package com.wceng.app.aiassistant.ui.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.domain.model.BubbleToMessages
import com.wceng.app.aiassistant.domain.model.Sender
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.ui.theme.AiaSafeDp
import com.wceng.app.aiassistant.util.LoadingContent
import com.wceng.app.aiassistant.util.MarkDownActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatScreen(
    sessionId: Long?,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    viewModel: ChatViewModel = koinViewModel<ChatViewModel>(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    sessionId?.let {
        LaunchedEffect(sessionId) {
            viewModel.updateSessionId(sessionId)
        }
    }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val messagesUiState by viewModel.messagesUiState.collectAsStateWithLifecycle()
    val chatUiState by viewModel.chatUiState.collectAsStateWithLifecycle()

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
            onClearAllMessages = {
                sessionId?.let { viewModel.clearMessages(sessionId) }
            },
            onCopyMessageContent = ::copyToClipboard,
            onRetrySendUserMessage = viewModel::retrySendUserMessage,
            onDeleteMessage = {

            },
            onRetryResponseAiMessage = viewModel::retryResponseAssistantMessage,
            onToggleMessage = viewModel::toggleMessage,
            onCancelReceiveMessage = viewModel::cancelReceiveMessage,
            onNewChat = {

            }
        ),
        onBack = onBack
    )

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
    val onNewChat: () -> Unit
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

    var needInputFocus by rememberSaveable { mutableStateOf(false) }

    fun scrollToBottom() {
        coroutineScope.launch {
            lazyListState.scrollToRealBottom(false)
        }
    }

    Scaffold(
        topBar = {
            ChatTopAppBar(
                title = chatUiState.sessionTitle,
                onClearAll = messageActions.onClearAllMessages,
                showBackButton = showBackButton,
                onBack = onBack,
                onNewChat = messageActions.onNewChat
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        floatingActionButton = {
            if (lazyListState.canScrollForward)
                ScrollToBottomButton {
                    scrollToBottom()
                }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            MessageInput(
                onSend = {
                    messageActions.onSendMessage(it, chatUiState.prompt)
                    scrollToBottom()
                    needInputFocus = false
                },
                onStop = messageActions.onCancelReceiveMessage,
                isLoading = chatUiState.isReceiving,
                requestFocus = needInputFocus,
                onFocusChanged = {
                    needInputFocus = it
                },
                modifier = Modifier
                    .padding(horizontal = AiaSafeDp.safeHorizontal)
//                    .imePadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues),
        ) {
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
                        chatUiState.prompt?.let(::prompt)

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

    if (messagesUiState is MessagesUiState.Success) {
        LaunchedEffect(Unit) {
            scrollToBottom()
        }
    }

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress) {
            needInputFocus = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    showBackButton: Boolean,
    onClearAll: () -> Unit = {},
    onBack: () -> Unit = {},
    onNewChat: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        modifier = modifier.fillMaxWidth(),
        navigationIcon = {
            if (showBackButton)
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = AiaImages.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
        },

        actions = {
            IconButton(onClick = onClearAll) {
                Icon(
                    imageVector = AiaImages.DeleteSweep,
                    contentDescription = stringResource(id = R.string.clear_all)
                )
            }
            IconButton(onClick = onNewChat) {
                Icon(
                    imageVector = AiaImages.AddComment,
                    contentDescription = stringResource(id = R.string.new_chat)
                )
            }

        }
    )
}

private fun LazyListScope.prompt(prompt: String) {
    item {
        var expended by rememberSaveable { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .animateContentSize()
                    .clickable {
                        expended = expended.not()
                    },
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
                    bubble.currentVersionMessage.id,
                    it
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

@Composable
private fun ScrollToBottomButton(onClick: () -> Unit) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Icon(
            imageVector = AiaImages.KeyboardArrowDown,
            contentDescription = stringResource(R.string.scroll_to_bottom)
        )
    }
}

suspend fun LazyListState.scrollToRealBottom(animatable: Boolean = false) {
    val lastItem = layoutInfo.totalItemsCount - 1
    if (lastItem >= 0) {
        if (animatable)
            animateScrollToItem(lastItem)
        else
            scrollToItem(lastItem)
        val viewportHeight = layoutInfo.viewportEndOffset
        val lastItemBottom = layoutInfo.visibleItemsInfo.lastOrNull()?.offset?.let {
            it + layoutInfo.visibleItemsInfo.last().size
        } ?: 0
        val extraScroll = lastItemBottom - viewportHeight
        if (extraScroll > 0) {
            if (animatable)
                animateScrollBy(extraScroll.toFloat())
            else
                scrollBy(extraScroll.toFloat())
        }
    }
}