package com.wceng.app.aiassistant.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wceng.app.aiassistant.data.ChatRepository
import com.wceng.app.aiassistant.domain.model.ConversationGroup
import com.wceng.app.aiassistant.domain.usecase.CreateConversationWithPromptUseCase
import com.wceng.app.aiassistant.domain.usecase.GetGroupedConversationsUseCase
import com.wceng.app.aiassistant.navigation.ConversationsRoute
import com.wceng.app.aiassistant.util.Constant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

const val SelectedConversationIdKey = "selected_conversation_id_key"

class SessionViewModel(
    private val chatRepository: ChatRepository,
    private val createConversationWithPromptUseCase: CreateConversationWithPromptUseCase,
    getGroupedConversationsUseCase: GetGroupedConversationsUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val promptId: Long? = savedStateHandle.toRoute<ConversationsRoute>().promptId

    init {
        if (promptId != null) {
            createNewSession(promptId = promptId)
        }
    }

    private val selectedSessionId: StateFlow<Long?> = savedStateHandle.getStateFlow(
        key = SelectedConversationIdKey,
        initialValue = null
    )

    private val _onSelectConversationId: MutableStateFlow<Long?> = MutableStateFlow(null)

    val onSelectConversationId: StateFlow<Long?> = _onSelectConversationId.asStateFlow()

    val uiState: StateFlow<SessionsUiState> = combine(
        selectedSessionId,
        getGroupedConversationsUseCase()
    ) { selectedSessionId, groupedSessions ->
        SessionsUiState.Success(
            selectedSessionId = selectedSessionId,
            groupedSessions = groupedSessions
        )
    }
        .catch<SessionsUiState> {
            emit(SessionsUiState.Error(it.message))
            it.printStackTrace()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SessionsUiState.Loading
        )

    fun createNewSession(
        title: String = Constant.DEFAULT_NEW_CONVERSATION_TITLE,
        promptId: Long? = null,
        needSelect: Boolean = true
    ) {
        viewModelScope.launch {
            val newConvId: Long = if (promptId == null) {
                chatRepository.createNewConversation(title)
            } else {
                createConversationWithPromptUseCase(promptId = promptId)
            }

            if (needSelect) {
                setSelectedConversation(newConvId)
                _onSelectConversationId.value = newConvId
                delay(100)
                _onSelectConversationId.value = null
            }
        }
    }


    fun deleteSession(convId: Long) = viewModelScope.launch {
        chatRepository.deleteConversation(convId)
    }

    fun setSelectedConversation(id: Long) {
        savedStateHandle[SelectedConversationIdKey] = id
    }

    fun updateSessionTitle(convId: Long, title: String) = viewModelScope.launch {
        chatRepository.updateConversationTitle(convId, title)
    }
}

sealed interface SessionsUiState {
    object Loading : SessionsUiState
    data class Error(val message: String?) : SessionsUiState
    data class Success(
        val selectedSessionId: Long? = null,
        val groupedSessions: List<ConversationGroup>
    ) : SessionsUiState
}