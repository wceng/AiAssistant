package com.wceng.app.aiassistant.ui.chat

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.util.AiaTextFieldAlertDialog

@Composable
fun EditUserMessageDialog(
    showDialog: Boolean,
    initialValue: String,
    onDismissRequest: () -> Unit,
    onSend: (String) -> Unit
) {
    if (showDialog) {
        AiaTextFieldAlertDialog(
            requestFocus = true,
            minLines = 2,
            initialValue = initialValue,
            onDismissRequest = onDismissRequest,
            onConfirmAction = onSend,
            titleRes = R.string.edit_user_message,
            confirmButtonTextRes = R.string.send,
            icon = AiaImages.Edit
        )
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

