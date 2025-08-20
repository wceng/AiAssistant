package com.wceng.app.aiassistant.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Copyright
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wceng.app.aiassistant.R

@Composable
fun AiaAssistChip(
    @StringRes labelRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIconImageVector: ImageVector? = null,
    trailingIconImageVector: ImageVector? = null,
) {
    AiaAssistChip(
        label = stringResource(labelRes),
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        leadingIconImageVector = leadingIconImageVector,
        trailingIconImageVector = trailingIconImageVector
    )
}

@Composable
fun AiaAssistChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIconImageVector: ImageVector? = null,
    trailingIconImageVector: ImageVector? = null,
) {
    AiaAssistChip(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = leadingIconImageVector?.let {
            {
                Icon(
                    imageVector = leadingIconImageVector,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        },
        trailingIcon = trailingIconImageVector?.let {
            {
                Icon(
                    imageVector = trailingIconImageVector,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        }
    )
}

@Composable
private fun AiaAssistChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    AiaAssistChip(
        label = label,
        onClick = onClick,
        modifier = modifier,
        colors = if (selected) AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) else AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = Color.Unspecified
        ),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon
    )
}

@Composable
fun AiaAssistChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: ChipColors = AssistChipDefaults.assistChipColors(),
) {
    AssistChip(
        modifier = modifier,
        onClick = onClick,
        label = {
            Text(text = label)
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = colors
    )
}

@Preview
@Composable
private fun AiaAssistChipDefault() {
    Surface {
        AiaAssistChip(
            label = "Copy",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun AiaAssistChipDefaultWithIcon() {
    Surface {
        AiaAssistChip(
            labelRes = R.string.copy,
            selected = false,
            leadingIconImageVector = Icons.Rounded.Copyright,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun AiaAssistChipSelected() {
    Surface {
        AiaAssistChip(
            labelRes = R.string.copy,
            selected = true,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun AiaAssistChipSelectedWithIcon() {
    Surface {
        AiaAssistChip(
            "deepseek-chat",
            onClick = {},
            selected = false,
            leadingIconImageVector = Icons.Rounded.Copyright
        )
    }
}

