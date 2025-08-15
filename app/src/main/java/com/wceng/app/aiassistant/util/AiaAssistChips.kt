package com.wceng.app.aiassistant.util

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.ui.theme.AiaImages

@Composable
fun AiaAssistChip(
    @StringRes labelRes: Int,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        modifier = modifier,
        onClick = onClick,
        label = {
            Text(text = stringResource(labelRes))
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

@Preview
@Composable
private fun AiaAssistChipPreview() {
    Surface {
        AiaAssistChip(
            R.string.copy,
            leadingIcon = AiaImages.ContentCopy,
            onClick = {},
        )
    }
}