package com.wceng.app.aiassistant.ui.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.component.AiaTextFiled
import com.wceng.app.aiassistant.ui.theme.AiaImages
import com.wceng.app.aiassistant.ui.theme.AiaSafeDp

@Composable
fun MessageInput(
    modifier: Modifier = Modifier,
    onSend: (text: String, images: List<Uri>) -> Unit,
    onStop: () -> Unit = {},
    isLoading: Boolean
) {
    var text by rememberSaveable { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // 1. 在组件内部创建图片选择器
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            // 可以限制图片数量, 例如:
            // val currentCount = selectedImageUris.size
            // val availableSlots = 5 - currentCount
            // selectedImageUris = selectedImageUris + uris.take(availableSlots)
            selectedImageUris = selectedImageUris + uris
        }
    )

    val enableSendButton by remember {
        derivedStateOf {
            (text.isNotBlank() || selectedImageUris.isNotEmpty()) && !isLoading
        }
    }

    // 移除图片的逻辑
    val onRemoveImage: (Uri) -> Unit = { uri ->
        selectedImageUris = selectedImageUris - uri
    }

    fun onSendMessage() {
        if (enableSendButton) {
            onSend(text, selectedImageUris)
            text = ""
            selectedImageUris = emptyList() // 发送后清空
        }
    }

    Surface(
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = modifier
        ) {
            // 图片预览区域
            AnimatedVisibility(visible = selectedImageUris.isNotEmpty()) {
                ImagePreviewRow(
                    imageUris = selectedImageUris,
                    onRemoveImage = onRemoveImage
                )
            }

            AiaTextFiled(
                value = text,
                onValueChange = { text = it },
                leadingIcon = {
                    IconButton(
                        // 2. 点击按钮直接启动图片选择器
                        onClick = {
                            multiplePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = stringResource(R.string.add_image)
                        )
                    }
                },
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

// ImagePreviewRow Composable (无需修改)
@Composable
private fun ImagePreviewRow(
    imageUris: List<Uri>,
    onRemoveImage: (Uri) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AiaSafeDp.safeHorizontal, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(imageUris) { uri ->
            ImagePreviewItem(uri = uri, onRemoveClick = { onRemoveImage(uri) })
        }
    }
}

// ImagePreviewItem Composable (无需修改)
@Composable
private fun ImagePreviewItem(
    uri: Uri,
    onRemoveClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Selected image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        IconButton(
            onClick = onRemoveClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(18.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove image",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

// --- Preview 已适配，无需修改 ---
@Preview(name = "Idle")
@Composable
private fun MessageInputIdle() {
    Surface {
        MessageInput(
            onSend = { _, _ -> },
            isLoading = false,
        )
    }
}

@Preview(name = "Loading")
@Composable
private fun MessageInputLoading() {
    Surface {
        MessageInput(
            onSend = { _, _ -> },
            isLoading = true,
        )
    }
}

// 这个Preview现在不能直接展示带图片的状态，因为状态是内部的。
// 但这对于组件本身的功能没有影响。
@Preview(name = "With Images (UI only)")
@Composable
private fun MessageInputWithImagesPreview() {
    Surface {
        Column {
            ImagePreviewRow(imageUris = listOf(Uri.EMPTY, Uri.EMPTY), onRemoveImage = {})
            AiaTextFiled(value = "Text with image", onValueChange = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
