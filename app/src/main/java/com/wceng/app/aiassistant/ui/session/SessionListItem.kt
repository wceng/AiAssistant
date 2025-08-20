package com.wceng.app.aiassistant.ui.session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.domain.model.Conversation
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.util.AiaTextFieldAlertDialog

@Composable
fun SessionListItem(
    conversation: Conversation,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onUpdateSessionTitle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
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
        },
        trailingContent = {
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
                        showEditDialog = true
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
    )

    if (showEditDialog) {
        AiaTextFieldAlertDialog(
            titleRes = R.string.edit_conversation_title,
            initialValue = conversation.title,
            onDismissRequest = { showEditDialog = false },
            confirmButtonTextRes = R.string.confirm,
            onConfirmAction = onUpdateSessionTitle,
            requestFocus = true,
            singleLine = true,
            icon = AiaImages.Edit
        )
    }
}

