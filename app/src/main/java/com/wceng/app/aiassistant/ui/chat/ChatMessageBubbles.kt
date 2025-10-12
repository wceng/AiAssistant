package com.wceng.app.aiassistant.ui.chat

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.domain.model.BubbleToMessages
import com.wceng.app.aiassistant.domain.model.MessageStatus
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.component.AiaAssistChip
import com.wceng.app.aiassistant.component.MarkDownActions
import com.wceng.app.aiassistant.component.MarkDownPage

@Composable
internal fun UserMessageBubble(
    modifier: Modifier = Modifier,
    bubbleToMsg: BubbleToMessages,
    onCopyClick: () -> Unit,
    onRetrySendUserMessage: (String) -> Unit,
    onToggleMessage: (Long) -> Unit,
) {
    var showEditUserMessageDialog by remember { mutableStateOf(false) }
    var userEditableText by remember { mutableStateOf("") }

    val userBubbleShape =
        RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)

    Row {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                modifier = modifier,
                shape = userBubbleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                SelectionContainer {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        text = bubbleToMsg.currentVersionMessage?.content ?: ""
                    )
                }
            }

            MessageUserOperateBar(
                bubbleToMsg = bubbleToMsg,
                onCopyClick = onCopyClick,
                onEditClick = {
                    showEditUserMessageDialog = true
                    userEditableText = bubbleToMsg.currentVersionMessage?.content ?: ""
                },
                onToggleMessage = onToggleMessage
            )
        }
    }

    EditUserMessageDialog(
        showDialog = showEditUserMessageDialog,
        initialValue = userEditableText,
        onDismissRequest = { showEditUserMessageDialog = false },
        onSend = onRetrySendUserMessage
    )
}


@Composable
internal fun AssistantMessageBubble(
    bubbleToMsg: BubbleToMessages,
    markDownActions: MarkDownActions,
    onCopyClick: () -> Unit,
    onRetryClick: () -> Unit,
    onToggleMessage: (Long) -> Unit,
) {
    bubbleToMsg.currentVersionMessage ?: return
    val aiBubbleShape =
        RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)

    Row {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = aiBubbleShape,
//                color = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                SelectionContainer {
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        when (bubbleToMsg.currentVersionMessage.status) {
                            MessageStatus.LOADING -> CircularProgressIndicator(
                                modifier = Modifier.size(
                                    24.dp
                                )
                            )

                            MessageStatus.FAILED -> Surface(
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = bubbleToMsg.currentVersionMessage.content,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                )
                            }

                            MessageStatus.NORMAL, MessageStatus.STOPPED, MessageStatus.GENERATING, MessageStatus.CANCELED -> {
                                MarkDownPage(
                                    content = bubbleToMsg.currentVersionMessage.content
                                )
                            }
                        }
                    }
                }
            }

            MessageAssistantOperateBar(
                modifier = Modifier.align(Alignment.Start),
                onCopyClick = onCopyClick,
                onRetryClick = onRetryClick,
                bubbleToMsg = bubbleToMsg,
                onToggleMessage = onToggleMessage,
            )
        }
    }
}

@Composable
private fun MessageUserOperateBar(
    modifier: Modifier = Modifier,
    bubbleToMsg: BubbleToMessages,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    onToggleMessage: (Long) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AiaAssistChip(
            R.string.copy,
            leadingIconImageVector = AiaImages.ContentCopy,
            selected = false,
            onClick = onCopyClick,
        )
        AiaAssistChip(
            R.string.edit,
            leadingIconImageVector = AiaImages.Edit,
            selected = false,
            onClick = onEditClick,
        )

        if (!bubbleToMsg.hasOnlyOneMessage)
            MessageToggleBar(
                currentIndex = bubbleToMsg.currentMessageIndex,
                totalIndex = bubbleToMsg.totalMessageNumber,
                disableLeftToggleIcon = !bubbleToMsg.hasPreviousMessage,
                disableRightToggleIcon = !bubbleToMsg.hasNextMessage,
                onLeftToggleClick = {
                    bubbleToMsg.previousMessage ?: return@MessageToggleBar
                    onToggleMessage(bubbleToMsg.previousMessage.id)
                },
                onRightToggleClick = {
                    bubbleToMsg.nextMessage ?: return@MessageToggleBar
                    onToggleMessage(bubbleToMsg.nextMessage.id)
                }
            )
    }
}


@Composable
private fun MessageAssistantOperateBar(
    bubbleToMsg: BubbleToMessages,
    onCopyClick: () -> Unit,
    onRetryClick: () -> Unit,
    onToggleMessage: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!bubbleToMsg.hasOnlyOneMessage)
            MessageToggleBar(
                currentIndex = bubbleToMsg.currentMessageIndex,
                totalIndex = bubbleToMsg.totalMessageNumber,
                disableLeftToggleIcon = !bubbleToMsg.hasPreviousMessage,
                disableRightToggleIcon = !bubbleToMsg.hasNextMessage,
                onLeftToggleClick = {
                    bubbleToMsg.previousMessage ?: return@MessageToggleBar
                    onToggleMessage(bubbleToMsg.previousMessage.id)
                },
                onRightToggleClick = {
                    bubbleToMsg.nextMessage ?: return@MessageToggleBar
                    onToggleMessage(bubbleToMsg.nextMessage.id)
                }
            )
        AiaAssistChip(
            labelRes = R.string.copy,
            leadingIconImageVector = AiaImages.ContentCopy,
            selected = false,
            onClick = onCopyClick,
        )
        AiaAssistChip(
            R.string.retry_response,
            leadingIconImageVector = AiaImages.Refresh,
            selected = false,
            onClick = onRetryClick,
        )
    }
}

@Composable
private fun MessageToggleBar(
    currentIndex: Int,
    totalIndex: Int,
    modifier: Modifier = Modifier,
    disableLeftToggleIcon: Boolean = false,
    disableRightToggleIcon: Boolean = false,
    onLeftToggleClick: () -> Unit = {},
    onRightToggleClick: () -> Unit = {},
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onLeftToggleClick,
            enabled = !disableLeftToggleIcon
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.toggle_previous_message)
            )
        }

        Text(text = "${currentIndex + 1} / $totalIndex")

        IconButton(
            onClick = onRightToggleClick,
            enabled = !disableRightToggleIcon
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.toggle_next_message)
            )
        }
    }
}

@Preview
@Composable
private fun MessageToggleBarPreview() {
    Surface {
        MessageToggleBar(
            currentIndex = 1,
            totalIndex = 3,
            disableLeftToggleIcon = true
        )
    }
}