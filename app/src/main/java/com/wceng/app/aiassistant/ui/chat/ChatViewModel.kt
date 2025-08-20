package com.wceng.app.aiassistant.ui.chat

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wceng.app.aiassistant.data.ChatRepository
import com.wceng.app.aiassistant.domain.model.BubbleToMessages
import com.wceng.app.aiassistant.util.Constant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SESSION_ID_KEY = "session_id_key"

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val cancelableJobMap = mutableMapOf<Long, Job>()

    private val _currentSessionId = savedStateHandle.getStateFlow<Long?>(SESSION_ID_KEY, null)

    private val _isReceiving = _currentSessionId
        .flatMapLatest { convId ->
            convId?.let {
                chatRepository.isMessageActiveInConversation(convId)
                    .distinctUntilChanged()
                    .onEach {
                        if(it.not()) cancelableJobMap.remove(convId)
                    }
            } ?: flow { false }
        }

    private val _currentSession = _currentSessionId
        .flatMapLatest { sessionId ->
            sessionId?.let {
                chatRepository.getConversationWithPromptFlow(it)
            } ?: flowOf(null)
        }

    val chatUiState: StateFlow<ChatUiState> =
        combine(
            _currentSession, _isReceiving
        ) { currentSession, isReceiving ->
            ChatUiState(
                sessionTitle = currentSession?.conversation?.title ?: "",
                prompt = currentSession?.prompt?.prompt ?: Constant.DEFAULT_PROMPT,
                isReceiving = isReceiving,
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                ChatUiState()
            )

    val messagesUiState: StateFlow<MessagesUiState> = _currentSessionId
        .flatMapLatest { sessionId ->
            sessionId?.let { convId ->
                chatRepository.getBubbleToMessages(convId, false)
                    .distinctUntilChanged()
                    .map { MessagesUiState.Success(it) }
                    .onStart<MessagesUiState> { emit(MessagesUiState.Loading) }
                    .catch {
                        MessagesUiState.Error(it.message ?: "Unknown error")
                        it.printStackTrace()
                    }
            } ?: flowOf(MessagesUiState.Idle)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MessagesUiState.Loading
        )

    fun sendMessage(
        content: String,
        prompt: String? = null
    ) {
        val currentConvId = _currentSessionId.value ?: return
        cancelableJobMap.put(currentConvId, viewModelScope.launch {
            chatRepository.sendMessage(currentConvId, content, prompt)
        })
    }

    fun retrySendUserMessage(
        userMessageId: Long,
        newContent: String
    ) {
        val currentConvId = _currentSessionId.value ?: return
        cancelableJobMap.put(currentConvId, viewModelScope.launch {
            chatRepository.retrySendUserMessage(
                convId = currentConvId,
                currentVersionMessageId = userMessageId,
                newVersionMessageContent = newContent
            )
        })
    }

    fun retryResponseAssistantMessage(
        aiMessageId: Long,
    ) {
        val currentConvId = _currentSessionId.value ?: return
        cancelableJobMap.put(currentConvId, viewModelScope.launch {
            chatRepository.retryResponseAssistantMessage(currentConvId, aiMessageId)
        })
    }

    fun toggleMessage(targetMessageId: Long) {
        viewModelScope.launch {
            val currentConvId = _currentSessionId.value ?: return@launch
            chatRepository.toggleMessage(currentConvId, targetMessageId)
        }
    }


    fun clearMessages(sessionId: Long) {
        viewModelScope.launch {
            chatRepository.clearAllBubbleAndMessages(sessionId)
        }
    }

    fun updateSessionId(sessionId: Long) {
        savedStateHandle[SESSION_ID_KEY] = sessionId
    }

    fun cancelReceiveMessage() {
        viewModelScope.launch {
            val currentConvId = _currentSessionId.value ?: return@launch
            cancelableJobMap[currentConvId]?.cancel()
            cancelableJobMap.remove(currentConvId)
            chatRepository.cancelReceiveMessage(currentConvId)
        }
    }
}

sealed interface MessagesUiState {
    object Loading : MessagesUiState
    data class Success(val bubbleToMessages: List<BubbleToMessages>) : MessagesUiState
    data class Error(val message: String) : MessagesUiState
    data object Idle : MessagesUiState
}

@Immutable
data class ChatUiState(
    val sessionTitle: String = "",
    val prompt: String? = null,
    val isReceiving: Boolean = false,
)
