package com.wceng.app.aiassistant.ui.chat

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.component.AiaTextFieldAlertDialog
import com.wceng.app.aiassistant.ui.theme.AiaImages

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
            title = stringResource(R.string.edit_user_message),
            confirmButtonText = stringResource(R.string.send),
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

