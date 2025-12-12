package com.wceng.app.aiassistant.ui.session

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateSetOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wceng.app.aiassistant.data.ConversationRepository
import com.wceng.app.aiassistant.domain.model.ConversationGroup
import com.wceng.app.aiassistant.domain.usecase.CreateConversationWithPromptUseCase
import com.wceng.app.aiassistant.domain.usecase.GetGroupedConversationsUseCase
import com.wceng.app.aiassistant.navigation.ConversationsRoute
import com.wceng.app.aiassistant.util.Constant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val SelectedConversationIdKey = "selected_conversation_id_key"

class SessionViewModel(
    private val conversationRepository: ConversationRepository,
    private val createConversationWithPromptUseCase: CreateConversationWithPromptUseCase,
    getGroupedConversationsUseCase: GetGroupedConversationsUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val promptId: Long? = savedStateHandle.toRoute<ConversationsRoute>().promptId
    private var selectedItems = mutableStateSetOf<Long>()
    private val _selectionMode = MutableStateFlow(false)
    private val selectedSessionId: StateFlow<Long?> = savedStateHandle.getStateFlow(
        key = SelectedConversationIdKey,
        initialValue = null
    )

    init {
        promptId?.let { createAndOpenNewConversation(promptId = it) }
    }

    private val _onOpenConversationId: MutableStateFlow<Long?> = MutableStateFlow(null)
    val onOpenConversationId: StateFlow<Long?> = _onOpenConversationId.asStateFlow()

    val selectionModeState: StateFlow<SelectionModeState> = _selectionMode
        .map { SelectionModeState(isActive = it, selectedItems = selectedItems) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SelectionModeState()
        )

    val sessionsState: StateFlow<SessionsUiState> = combine(
        selectedSessionId,
        getGroupedConversationsUseCase(),
    ) { selectedSessionId, groupedSessions ->
        SessionsUiState.Success(
            selectedSessionId = selectedSessionId,
            groupedSessions = groupedSessions,
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

    fun createAndOpenNewConversation(
        title: String = Constant.DEFAULT_NEW_CONVERSATION_TITLE,
        promptId: Long? = null,
    ) {
        viewModelScope.launch {
            val newConvId: Long = if (promptId == null) {
                conversationRepository.createNewConversation(title)
            } else {
                createConversationWithPromptUseCase(promptId = promptId)
            }

            setSelectedConversation(newConvId)
            _onOpenConversationId.value = newConvId
        }
    }

    fun setSelectedConversation(id: Long) {
        savedStateHandle[SelectedConversationIdKey] = id
    }

    fun openConversation(id: Long) {
        _onOpenConversationId.value = id
    }

    fun setConversationOpened() {
        _onOpenConversationId.value = null
    }

    fun enableSelectionMode() {
        _selectionMode.value = true
    }

    fun disableSelectionMode() {
        _selectionMode.value = false
        selectedItems.clear()
    }

    fun deleteSelectedConversations() {
        viewModelScope.launch {
            if (selectedItems.isNotEmpty()) {
                conversationRepository.deleteConversations(selectedItems)
                disableSelectionMode()
            }
        }
    }

    fun toggleSelectedItem(convId: Long, selected: Boolean) {
        if (selected) {
            selectedItems.add(convId)
        } else {
            selectedItems.remove(convId)
        }

        if (selectedItems.isEmpty())
            disableSelectionMode()
    }
}

sealed interface SessionsUiState {
    object Loading : SessionsUiState
    data class Error(val message: String?) : SessionsUiState
    data class Success(
        val selectedSessionId: Long? = null,
        val groupedSessions: List<ConversationGroup>,
    ) : SessionsUiState
}

@Immutable
data class SelectionModeState(
    val isActive: Boolean = false,
    val selectedItems: Set<Long> = emptySet()
)