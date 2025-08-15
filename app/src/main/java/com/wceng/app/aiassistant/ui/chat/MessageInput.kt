package com.wceng.app.aiassistant.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wceng.app.aiassistant.R

@Composable
fun MessageInput(
    onSend: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }

    fun onSendMessage() {
        if (text.isNotBlank() && !isLoading) {
            onSend(text)
            text = ""
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Column(modifier = modifier) {
        // 输入框和发送按钮
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 修改为多行 TextField
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(textFieldFocusRequester)
                    .onKeyEvent { event ->
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                            if (!event.isShiftPressed) {
                                true
                            } else {
                                onSendMessage()
                                false
                            }
                        } else {
                            false
                        }
                    },
                placeholder = { Text(stringResource(R.string.message_input_placeholder)) },
                singleLine = false, // 设置为 false 支持多行
                maxLines = 5 // 限制最大行数
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    onSendMessage()
                },
                enabled = text.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.send))
                }
            }
        }
    }
}