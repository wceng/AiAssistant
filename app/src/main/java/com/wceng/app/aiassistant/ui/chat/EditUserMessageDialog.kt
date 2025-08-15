package com.wceng.app.aiassistant.ui.chat

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.util.AiaTextFiled

@Composable
fun EditUserMessageDialog(
    showDialog: Boolean,
    initialValue: String,
    onDismissRequest: () -> Unit,
//    onValueChanged: (String) -> Unit,
    onSend: (String) -> Unit
) {
    if (showDialog) {
        var text by rememberSaveable(initialValue) {
            mutableStateOf(initialValue)
        }

        val focusRequester = remember { FocusRequester() }

        AlertDialog(
            modifier = Modifier.focusRequester(focusRequester),
            onDismissRequest = onDismissRequest,
            confirmButton = {
                IconButton(onClick = {
                    onSend(text)
                    onDismissRequest()
                }) {
                    Icon(AiaImages.Send, contentDescription = stringResource(R.string.send))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = {
                AiaTextFiled(
                    modifier = Modifier.fillMaxWidth(),
                    value = text,
                    onValueChange = { text = it },
                    maxLines = 8
                )
            },
            title = {
                Text(stringResource(R.string.edit_user_message))
            }
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Preview
@Composable
private fun EditUserMessageDialogPreview() {
    Surface {
        EditUserMessageDialog(
            true,
            onDismissRequest = {},
            initialValue = "Hello.",
            onSend = {}
        )
    }
}

