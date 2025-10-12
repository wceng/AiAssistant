package com.wceng.app.aiassistant.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wceng.app.aiassistant.data.AiProviderRepository
import com.wceng.app.aiassistant.data.UserSettingsRepository
import com.wceng.app.aiassistant.domain.model.AiProviderInfo
import com.wceng.app.aiassistant.domain.model.SelectableAiProviderModel
import com.wceng.app.aiassistant.domain.model.defaultAiProviders
import com.wceng.app.aiassistant.domain.usecase.GetSelectedAiProviderUseCase
import com.wceng.app.aiassistant.util.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServiceProviderViewModel(
    private val userSettingsRepository: UserSettingsRepository,
    private val aiProviderRepository: AiProviderRepository,
    private val getSelectedAiProviderUseCase: GetSelectedAiProviderUseCase
) : ViewModel() {

    private val _apiKey = MutableStateFlow("")
    private val _baseUrl = MutableStateFlow("")
    private val _showApiKey = MutableStateFlow(false)
    private val _isModelsRefreshing = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            getSelectedAiProviderUseCase()
                .collect {
                    val selectedProvider = it ?: aiProviderRepository.getAllProviders().first().first()
                    _apiKey.value = selectedProvider.apiKey ?: ""
                    _baseUrl.value = selectedProvider.baseUrl ?: ""
                }
        }
    }

    private val _isConfigChanged: Flow<Boolean> = combine(
        getSelectedAiProviderUseCase(),
        _apiKey,
        _baseUrl
    ) { selectedAiProvider, apiKey, baseUrl ->
        selectedAiProvider?.apiKey != apiKey
                || selectedAiProvider.baseUrl != baseUrl
    }

    private val _isShowResetHostUrl: Flow<Boolean> = combine(
        userSettingsRepository.userSettingInfo,
        _baseUrl
    ) { userSettingInfo, baseUrl ->
//        val selectedAiProviderName =
//            userSettingInfo.aiProviderConfigInfo.selectedAiProviderInfo.name
//        defaultAiProviders.firstOrNull {
//            it.name == selectedAiProviderName
//        }?.let {
//            it.baseUrl != baseUrl.trim()
//        } ?: false
        false
    }

    private val selectableAiProviderModel = combine(
        userSettingsRepository.userSettingInfo.map { it.selectedAiProviderId },
        aiProviderRepository.getAllProviders()
    ) { providerId, allProviders ->
        SelectableAiProviderModel(
            selectedAiProvider = allProviders.firstOrNull{ it.id == providerId } ?: allProviders.first(),
            aiProviders = allProviders
        )
    }

    val uiState: StateFlow<ServiceProviderUiState> =
        combine(
            selectableAiProviderModel,
            _isConfigChanged,
            _apiKey,
            _baseUrl,
            _showApiKey,
            _isModelsRefreshing,
            _isShowResetHostUrl
        ) { selectableAiProviderModel, isConfigChanged, apiKey, baseUrl, showApiKey, isModelsRefresh, isShowResetHostUrl ->
            ServiceProviderUiState.Success(
                selectableAiProviderModel = selectableAiProviderModel,
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

    fun setSelectedProvider(aiProviderId: Long) {
        viewModelScope.launch {
            userSettingsRepository.setSelectedAiProvider(aiProviderId)
        }
    }

    fun updateSelectedModel(model: String) {
        viewModelScope.launch {
            getSelectedAIProviderId()?.let {
                aiProviderRepository.updateSelectedModel(it, model)
            }
        }
    }

    fun saveConfig() {
        viewModelScope.launch {
            getSelectedAIProviderId()?.let {
                aiProviderRepository.updateApiKey(it, _apiKey.value.trim())
                aiProviderRepository.updateBaseUrl(it, _baseUrl.value.trim())
            }
        }
    }

    fun refreshModels() {
        viewModelScope.launch {
            getSelectedAIProviderId()?.let {
                _isModelsRefreshing.value = true
                aiProviderRepository.refreshAvailableModels(it)
                _isModelsRefreshing.value = false
            }
        }
    }

    fun addModel(model: String) {
        viewModelScope.launch {
            getSelectedAIProviderId()?.let {
                aiProviderRepository.addAvailableModel(it, model)
            }
        }
    }

    fun deleteModel(model: String) {
        viewModelScope.launch {
            getSelectedAIProviderId()?.let {
                aiProviderRepository.removeAvailableModel(it, model)
            }
        }
    }

    fun addCustomServiceProvider(name: String) {
        viewModelScope.launch {
            aiProviderRepository.addProvider(
                AiProviderInfo(name = name)
            ).let {
                userSettingsRepository.setSelectedAiProvider(it)
            }
        }
    }

    fun resetHostUrl(aiProviderId: Long) {
        viewModelScope.launch {
            defaultAiProviders.firstOrNull { it.id == aiProviderId }?.let {
                _baseUrl.value = it.baseUrl ?: ""
                aiProviderRepository.updateBaseUrl(aiProviderId, it.baseUrl)
            }
        }
    }

    fun deleteServiceProvider(aiProviderId: Long) {
        viewModelScope.launch {
            aiProviderRepository.deleteProvider(aiProviderId)
            userSettingsRepository.setSelectedAiProvider(null)
        }
    }

    private suspend fun getSelectedAIProviderId() = getSelectedAiProviderUseCase().first()?.id
}

sealed interface ServiceProviderUiState {
    data object Loading : ServiceProviderUiState
    data class Success(
        val selectableAiProviderModel: SelectableAiProviderModel,
        val isConfigChanged: Boolean,
        val apiKey: String,
        val baseUrl: String,
        val showApiKey: Boolean,
        val isModelsRefreshing: Boolean,
        val isShowResetHostUrl: Boolean
    ) : ServiceProviderUiState
}
