package com.wceng.app.aiassistant.ui.prompt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wceng.app.aiassistant.data.PromptRepository
import com.wceng.app.aiassistant.domain.model.Prompt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PromptViewModel(
    private val promptRepository: PromptRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            promptRepository.refresh()
        }
    }

    val uiState: StateFlow<PromptUiState> = promptRepository.prompts
        .map(PromptUiState::Success)
        .catch { PromptUiState.Error }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PromptUiState.Loading
        )
}

sealed interface PromptUiState {
    data object Loading : PromptUiState
    data class Success(
        val prompts: List<Prompt>,
    ) : PromptUiState

    data object Error : PromptUiState
}