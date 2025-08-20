package com.wceng.app.aiassistant.ui.chat

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.util.AiaTextFiled

@Composable
fun MessageInput(
    isLoading: Boolean,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    requestFocus: Boolean = false,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    var text by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val focusRequester = remember { FocusRequester() }

    fun onSendMessage() {
        if (text.isNotBlank() && !isLoading) {
            onSend(text)
            text = ""
        }
    }

    AiaTextFiled(
        value = text,
        onValueChange = { text = it },
        trailingIcon = {
            if (isLoading) {
                IconButton(
                    onClick = onStop
                ) {
                    Icon(
                        imageVector = AiaImages.Stop,
                        contentDescription = stringResource(R.string.stop)
                    )
                }
            } else {
                IconButton(onClick = { onSendMessage() }) {
                    Icon(
                        imageVector = AiaImages.Send,
                        contentDescription = stringResource(R.string.send)
                    )
                }
            }
        },
        placeholder = {
            Text(stringResource(R.string.message_input_placeholder))
        },
        singleLine = false,
        maxLines = 5,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                val isFocused = focusState.isFocused
                onFocusChanged(isFocused)

            },
        )

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }
}

@Preview
@Composable
private fun MessageInputIdle() {
    Surface {
        MessageInput(
            onSend = {},
            isLoading = false,
            onStop = { },
        )
    }
}

@Preview
@Composable
private fun MessageInputLoading() {
    Surface {
        MessageInput(
            onSend = {},
            isLoading = true,
            onStop = { },
        )
    }
}