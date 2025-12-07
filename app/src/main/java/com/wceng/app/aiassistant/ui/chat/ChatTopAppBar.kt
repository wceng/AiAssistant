package com.wceng.app.aiassistant.ui.chat

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.component.BackButton
import com.wceng.app.aiassistant.ui.theme.AiaImages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    showBackButton: Boolean,
    onClearAll: () -> Unit = {},
    onBack: () -> Unit = {},
    onNewChat: () -> Unit = {},
    onEdit: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        modifier = modifier.fillMaxWidth(),
        navigationIcon = {
            if (showBackButton) BackButton(onClick = onBack)
        },
        actions = {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = AiaImages.Edit,
                    contentDescription = stringResource(id = R.string.rename_conversation_title)
                )
            }
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

        },
        scrollBehavior = scrollBehavior
    )
}