package com.wceng.app.aiassistant.ui.chat

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wceng.app.aiassistant.data.ChatRepository
import com.wceng.app.aiassistant.domain.model.BubbleToMessages
import com.wceng.app.aiassistant.util.Constant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    private val _isReceiving = MutableStateFlow(false)

    private val currentSession = _currentSessionId
        .flatMapLatest { sessionId ->
            sessionId?.let {
                chatRepository.getConversationWithPromptFlow(it)
            } ?: flowOf(null)
        }

    val chatUiState: StateFlow<ChatUiState> =
        combine(
            currentSession, _isReceiving
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
        viewModelScope.launch {
            val currentConvId = _currentSessionId.value ?: return@launch
            _isReceiving.value = true
            chatRepository.sendMessageWithStream(currentConvId, content, prompt)
            _isReceiving.value = false
        }
    }

    fun retrySendUserMessage(
        userMessageId: Long,
        newContent: String
    ) {
        viewModelScope.launch {
            val currentConvId = _currentSessionId.value ?: return@launch

            _isReceiving.value = true
            chatRepository.retrySendUserMessage(
                convId = currentConvId,
                currentVersionMessageId = userMessageId,
                newVersionMessageContent = newContent
            )
            _isReceiving.value = false
        }
    }

    fun retryResponseAssistantMessage(
        aiMessageId: Long,
    ) {
        viewModelScope.launch {
            val currentConvId = _currentSessionId.value ?: return@launch

            _isReceiving.value = true
            chatRepository.retryResponseAssistantMessage(currentConvId, aiMessageId)
            _isReceiving.value = false
        }
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
        _currentSessionId.value = sessionId
    }

    fun pauseGeneratingMessage() {
//        getMessagesUseCase.pause()
    }

    fun resumeGeneratingMessage() {
//        getMessagesUseCase.resume()
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
