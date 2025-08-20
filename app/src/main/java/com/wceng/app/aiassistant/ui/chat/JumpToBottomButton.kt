package com.wceng.app.aiassistant.ui.chat

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.ui.theme.AiaImages

@Composable
fun JumpToBottomButton(
    onClick: () -> Unit
) {
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