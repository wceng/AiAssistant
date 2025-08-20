package com.wceng.app.aiassistant.component

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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.wceng.app.aiassistant.R

@Composable
fun AiaTextFieldAlertDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButtonText: String,
    onConfirmAction: (text: String) -> Unit,
    icon: ImageVector? = null,
    requestFocus: Boolean = false,
    initialValue: String = "",
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
) {
    var textFieldValue by remember (initialValue) {
        mutableStateOf(
            TextFieldValue(
                text = initialValue,
                selection = TextRange(initialValue.length)
            )
        )
    }

    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        modifier = Modifier.focusRequester(focusRequester),
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmAction(textFieldValue.text)
                    onDismissRequest()
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(confirmButtonText)
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
                textFieldValue = textFieldValue,
                onValueChange = { textFieldValue = it },
                maxLines = maxLines,
                minLines = minLines,
                singleLine = singleLine
            )
        },
        title = {
            Text(text = title)
        },
        icon = icon?.let { { Icon(imageVector = icon, contentDescription = null) } },
    )

    if (requestFocus)
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
}

@Composable
fun AiaMessageConfirmAlertDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onConfirmAction: () -> Unit,
    text: String? = null,
    icon: ImageVector? = null
) {
    AlertDialog(
        title = {
            Text(text = title)
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onConfirmAction()
                onDismissRequest()
            }) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        text = text?.let { { Text(it) } },
        icon = icon?.let { { Icon(imageVector = icon, contentDescription = null) } }
    )
}