@file:OptIn(ExperimentalMaterial3Api::class)

package com.wceng.app.aiassistant.ui.session

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.component.CloseButton
import com.wceng.app.aiassistant.ui.theme.AiaImages

@Composable
fun ConversationTopBar(
    selectionModeState: SelectionModeState,
    scrollBehavior: TopAppBarScrollBehavior,
    onDisableSelectionMode: () -> Unit,
    onDelete: () -> Unit
) {
    LargeTopAppBar(
        title = {
            if (selectionModeState.isActive) {
                Text(
                    text = stringResource(
                        R.string.count_selected,
                        selectionModeState.selectedItems.size
                    )
                )
            } else {
                Text(text = stringResource(R.string.session_title))
            }
        },
        navigationIcon = {
            if (selectionModeState.isActive) {
                CloseButton(onDisableSelectionMode)
            }
        },
        colors = if (selectionModeState.isActive)
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
//                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
//                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
        else TopAppBarDefaults.topAppBarColors(),
        actions = {
            if (selectionModeState.isActive)
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = AiaImages.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
        },
        scrollBehavior = scrollBehavior,
    )
}