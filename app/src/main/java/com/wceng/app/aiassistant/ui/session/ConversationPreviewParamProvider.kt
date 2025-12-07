package com.wceng.app.aiassistant.ui.session

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.wceng.app.aiassistant.domain.model.Conversation

class ConversationPreviewParamProvider : PreviewParameterProvider<List<Conversation>> {
    override val values: Sequence<List<Conversation>>
        get() = sequenceOf(
            listOf(
                Conversation(
                    id = 1,
                    title = "Conversation 1",
                ),
                Conversation(
                    id = 2,
                    title = "Conversation 2",
                ),
                Conversation(
                    id = 3,
                    title = "Conversation 3",
                ),
                Conversation(
                    id = 4,
                    title = "Conversation 4",
                ),
                Conversation(
                    id = 5,
                    title = "Conversation 5",
                ),
            )
        )
}

