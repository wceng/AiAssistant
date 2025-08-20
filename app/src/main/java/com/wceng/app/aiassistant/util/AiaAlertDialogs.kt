package com.wceng.app.aiassistant.util

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.wceng.app.aiassistant.R

@Composable
fun AiaTextFieldAlertDialog(
    @StringRes titleRes: Int,
    onDismissRequest: () -> Unit,
    @StringRes confirmButtonTextRes: Int,
    onConfirmAction: (text: String) -> Unit,
    icon: ImageVector? = null,
    requestFocus: Boolean = false,
    initialValue: String = "",
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
) {
    var text by rememberSaveable(initialValue) {
        mutableStateOf(initialValue)
    }

    val focusRequester = remember { FocusRequester() }

    AiaAlertDialog(
        modifier = Modifier.focusRequester(focusRequester),
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmAction(text)
                    onDismissRequest()
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(confirmButtonTextRes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        text = {
            AiaTextFiled(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                onValueChange = { text = it },
                maxLines = maxLines,
                minLines = minLines,
                singleLine = singleLine
            )
        },
        titleRes = titleRes,
        icon = icon,
    )

    if (requestFocus)
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
}

@Composable
fun AiaAlertDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    @StringRes titleRes: Int,
    text: @Composable (() -> Unit)? = null,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = text,
        title = {
            Text(stringResource(titleRes))
        },
        icon = {
            icon?.let {
                Icon(imageVector = icon, contentDescription = iconContentDescription)
            }
        }
    )
}