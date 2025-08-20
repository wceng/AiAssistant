package com.wceng.app.aiassistant.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wceng.app.aiassistant.data.UserSettingsRepository
import com.wceng.app.aiassistant.domain.model.AiProviderConfigInfo
import com.wceng.app.aiassistant.domain.model.defaultAiProviders
import com.wceng.app.aiassistant.util.combine
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
    private val _isModelsRefreshing = MutableStateFlow(false)

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

    private val _isConfigChanged: Flow<Boolean> = combine(
        userSettingsRepository.userSettingInfo,
        _apiKey,
        _baseUrl
    ) { userSettingInfo, apiKey, baseUrl ->
        userSettingInfo.aiProviderConfigInfo.selectedAiProviderInfo.apiKey != apiKey
                || userSettingInfo.aiProviderConfigInfo.selectedAiProviderInfo.baseUrl != baseUrl
    }

    private val _isShowResetHostUrl: Flow<Boolean> = combine(
        userSettingsRepository.userSettingInfo,
        _baseUrl
    ) { userSettingInfo, baseUrl ->
        val selectedAiProviderName =
            userSettingInfo.aiProviderConfigInfo.selectedAiProviderInfo.name
        defaultAiProviders.firstOrNull {
            it.name == selectedAiProviderName
        }?.let {
            it.baseUrl != baseUrl.trim()
        } ?: false
    }

    val uiState: StateFlow<ServiceProviderUiState> =
        combine(
            userSettingsRepository.userSettingInfo.map { it.aiProviderConfigInfo },
            _isConfigChanged,
            _apiKey,
            _baseUrl,
            _showApiKey,
            _isModelsRefreshing,
            _isShowResetHostUrl
        ) { aiProviderConfigInfo, isConfigChanged, apiKey, baseUrl, showApiKey, isModelsRefresh, isShowResetHostUrl ->
            ServiceProviderUiState.Success(
                aiProviderConfigInfo = aiProviderConfigInfo,
                isConfigChanged = isConfigChanged,
                apiKey = apiKey,
                baseUrl = baseUrl,
                showApiKey = showApiKey,
                isModelsRefreshing = isModelsRefresh,
                isShowResetHostUrl = isShowResetHostUrl
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

    fun refreshModels() {
        viewModelScope.launch {
            _isModelsRefreshing.value = true
            userSettingsRepository.refreshSelectedAiProviderModels()
            _isModelsRefreshing.value = false
        }
    }

    fun addModel(model: String) {
        viewModelScope.launch {
            if (userSettingsRepository.existSelectedAiProviderModel(model).not())
                userSettingsRepository.addSelectedAiProviderModel(model)
        }
    }

    fun deleteModel(model: String) {
        viewModelScope.launch {
            userSettingsRepository.deleteSelectedAiProviderModel(model)
        }
    }

    fun addCustomServiceProvider(name: String) {
        viewModelScope.launch {
            if (userSettingsRepository.existServiceProviderName(name).not()) {
                userSettingsRepository.addAiProviderInfo(name)
                userSettingsRepository.setSelectedAiProvider(name)
            }
        }
    }

    fun resetHostUrl(aiProviderName: String) {
        viewModelScope.launch {
            defaultAiProviders.firstOrNull { it.name == aiProviderName }?.let {
                _baseUrl.value = it.baseUrl
                userSettingsRepository.setSelectedAiProviderBaseUrl(it.baseUrl)
            } ?: return@launch

        }
    }

    fun deleteServiceProvider(providerName: String) {
        viewModelScope.launch {
            userSettingsRepository.deleteServiceProvider(providerName)
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
        val showApiKey: Boolean,
        val isModelsRefreshing: Boolean,
        val isShowResetHostUrl: Boolean
    ) : ServiceProviderUiState
}
