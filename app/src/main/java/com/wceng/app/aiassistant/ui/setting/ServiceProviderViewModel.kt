package com.wceng.app.aiassistant.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wceng.app.aiassistant.data.UserSettingsRepository
import com.wceng.app.aiassistant.domain.model.AiProviderConfigInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServiceProviderViewModel(
    private val userSettingsRepository: UserSettingsRepository,
) : ViewModel() {

    private val _apiKey = MutableStateFlow("")
    private val _baseUrl = MutableStateFlow("")
    private val _showApiKey = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            userSettingsRepository.userSettingInfo
                .map { it.aiProviderConfigInfo.selectedAiProviderInfo }
                .collect { selectedAiProviderInfo ->
                    _apiKey.value = selectedAiProviderInfo.apiKey
                    _baseUrl.value = selectedAiProviderInfo.baseUrl
                }
        }
    }

    private val isConfigChanged: Flow<Boolean> = combine(
        userSettingsRepository.userSettingInfo,
        _apiKey,
        _baseUrl
    ) { userSettingInfo, apiKey, baseUrl ->
        userSettingInfo.aiProviderConfigInfo.selectedAiProviderInfo.apiKey != apiKey
                || userSettingInfo.aiProviderConfigInfo.selectedAiProviderInfo.baseUrl != baseUrl
    }

    val uiState: StateFlow<ServiceProviderUiState> =
        combine(
            userSettingsRepository.userSettingInfo.map { it.aiProviderConfigInfo },
            isConfigChanged,
            _apiKey,
            _baseUrl,
            _showApiKey
        ) { aiProviderConfigInfo, isConfigChanged, apiKey, baseUrl, showApiKey ->
            ServiceProviderUiState.Success(
                aiProviderConfigInfo = aiProviderConfigInfo,
                isConfigChanged = isConfigChanged,
                apiKey = apiKey,
                baseUrl = baseUrl,
                showApiKey = showApiKey
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                ServiceProviderUiState.Loading
            )

    fun updateApiKey(key: String) {
        _apiKey.value = key
    }

    fun updateBaseUrl(url: String) {
        _baseUrl.value = url
    }

    fun toggleApiKeyVisibility() {
        _showApiKey.value = !_showApiKey.value
    }

    fun setSelectedProvider(name: String) {
        viewModelScope.launch {
            userSettingsRepository.setSelectedAiProvider(name)
        }
    }

    fun updateSelectedModel(model: String) {
        viewModelScope.launch {
            userSettingsRepository.setSelectedAiProviderSelectedModel(model)
        }
    }

    fun saveConfig() {
        viewModelScope.launch {
            userSettingsRepository.setSelectedAiProviderApiKey(_apiKey.value.trim())
            userSettingsRepository.setSelectedAiProviderBaseUrl(_baseUrl.value.trim())
        }
    }
}

sealed interface ServiceProviderUiState {
    data object Loading : ServiceProviderUiState
    data class Success(
        val aiProviderConfigInfo: AiProviderConfigInfo,
        val isConfigChanged: Boolean,
        val apiKey: String,
        val baseUrl: String,
        val showApiKey: Boolean
    ) : ServiceProviderUiState
}
