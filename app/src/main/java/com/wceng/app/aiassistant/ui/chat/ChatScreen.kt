package com.wceng.app.aiassistant.ui.chat

import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatScreen(
    sessionId: Long?,
    modifier: Modifier = Modifier,
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
    val context = LocalContext.current

    val messagesUiState by viewModel.messagesUiState.collectAsStateWithLifecycle()
    val chatUiState by viewModel.chatUiState.collectAsStateWithLifecycle()

    fun copyToClipboard(content: String) = coroutineScope.launch {
        clipboardManager.setText(AnnotatedString(content))
    }

    ChatContent(
        modifier = modifier,
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
            onPauseGenerateMessage = viewModel::pauseGeneratingMessage,
            onResumeGenerateMessage = viewModel::resumeGeneratingMessage,
            onRetryResponseAiMessage = viewModel::retryResponseAssistantMessage,
            onToggleMessage = viewModel::toggleMessage
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
    val onPauseGenerateMessage: () -> Unit,
    val onResumeGenerateMessage: () -> Unit,
    val onToggleMessage: (targetMessageId: Long) -> Unit,
)

@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
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

    fun scrollToBottom() {
        coroutineScope.launch {
            delay(200)
            lazyListState.scrollToRealBottom(false)
        }
    }

    LaunchedEffect(Unit) {
        scrollToBottom()
    }

    Scaffold(
        topBar = {
            ChatTopAppBar(
                title = chatUiState.sessionTitle,
                onClearAll = messageActions.onClearAllMessages,
                showBackButton = showBackButton,
                onBack = onBack
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
                },
                isLoading = chatUiState.isReceiving,
                modifier = Modifier
                    .padding(horizontal = AiaSafeDp.safeHorizontal)
                    .imePadding()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier.padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AiaSafeDp.safeHorizontal),
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
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    showBackButton: Boolean,
    onClearAll: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        modifier = modifier.fillMaxWidth(),
        navigationIcon = {
            if (showBackButton)
                IconButton(onClick = onBack) {
                    Icon(imageVector = AiaImages.ArrowBack, contentDescription = "Back")
                }
        },

        actions = {
            IconButton(onClick = onClearAll) {
                Icon(
                    imageVector = AiaImages.ClearAll,
                    contentDescription = stringResource(id = R.string.clear_all)
                )
            }
        }
    )
}

private fun LazyListScope.prompt(prompt: String) {
    item {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.clip(MaterialTheme.shapes.medium)
            ) {
                Text(text = prompt)
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
    SideEffect {
//        println("正在发生重组的气泡的Id： ${bubbleToMessages.bubble.id}")
    }

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