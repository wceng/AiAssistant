package com.wceng.app.aiassistant.util

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AiaCircularProgressIndicator(
    modifier: Modifier = Modifier
){
    CircularProgressIndicator(
        modifier = modifier,
        strokeWidth = 2.dp
    )
}