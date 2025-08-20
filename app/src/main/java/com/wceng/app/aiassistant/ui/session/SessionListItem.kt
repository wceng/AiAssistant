package com.wceng.app.aiassistant.ui.session

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wceng.app.aiassistant.domain.model.Conversation
import com.wceng.app.aiassistant.ui.theme.AiaImages

@Composable
fun SessionListItem(
    conversation: Conversation,
    selected: Boolean,
    selectionModeState: SelectionModeState,
    onOpenConversation: () -> Unit,
    onEnableSelectionMode: () -> Unit,
    onToggleSelectedItem: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    //TODO may be block UI
    val itemSelected = conversation.id in selectionModeState.selectedItems

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .combinedClickable(
                onClick = {
                    if (selectionModeState.isActive) {
                        onToggleSelectedItem(itemSelected.not())
                    } else {
                        onOpenConversation()
                    }
                },
                onLongClick = {
                    if (selectionModeState.isActive) {
                        onToggleSelectedItem(itemSelected.not())
                    } else {
                        onEnableSelectionMode()
                        onToggleSelectedItem(true)
                    }
                }
            ),
        colors = ListItemDefaults.colors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            headlineColor = if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        leadingContent = {
            if (selectionModeState.isActive) {
                Checkbox(
                    checked = itemSelected,
                    onCheckedChange = onToggleSelectedItem
                )
            } else {
                Icon(
                    imageVector = AiaImages.Chat,
                    contentDescription = null,
                    tint = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        headlineContent = {
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
        })
}

