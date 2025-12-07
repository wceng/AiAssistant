package com.wceng.app.aiassistant.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.component.AiaTextFiled
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.ui.theme.AiaSafeDp

@Composable
fun MessageInput(
    modifier: Modifier = Modifier,
    onSend: (String) -> Unit,
    onStop: () -> Unit = {},
    isLoading: Boolean
) {
    var text by rememberSaveable { mutableStateOf("") }
//    val keyboardController = LocalSoftwareKeyboardController.current
//    val focusManager = LocalFocusManager.current
    val enableSendButton by remember {
        derivedStateOf {
            text.isNotBlank()
        }
    }

    fun onSendMessage() {
        if (text.isNotBlank() && !isLoading) {
            onSend(text)
            text = ""
        }
    }

    Surface(
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = modifier
        ) {
            AiaTextFiled(
                value = text,
                onValueChange = { text = it },
                trailingIcon = {
                    if (isLoading) {
                        IconButton(
                            onClick = onStop,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = AiaImages.Stop,
                                contentDescription = stringResource(R.string.stop)
                            )
                        }
                    } else {
                        IconButton(
                            enabled = enableSendButton, onClick = { onSendMessage() },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AiaSafeDp.safeHorizontal)
            )
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
        )
    }
}