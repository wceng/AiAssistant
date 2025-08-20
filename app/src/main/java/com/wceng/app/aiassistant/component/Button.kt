package com.wceng.app.aiassistant.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.ui.theme.AiaImages

@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = AiaImages.ArrowBack,
            contentDescription = stringResource(R.string.back)
        )
    }
}

@Composable
fun CloseButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            AiaImages.Close,
            contentDescription = stringResource(R.string.close)
        )
    }
}