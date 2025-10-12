@file:OptIn(ExperimentalCoroutinesApi::class)

package com.wceng.app.aiassistant.domain.usecase

import com.wceng.app.aiassistant.data.AiProviderRepository
import com.wceng.app.aiassistant.data.UserSettingsRepository
import com.wceng.app.aiassistant.domain.model.AiProviderInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetSelectedAiProviderUseCase(
    private val userSettingsRepository: UserSettingsRepository,
    private val aiProviderRepository: AiProviderRepository
) {

    operator fun invoke(): Flow<AiProviderInfo?> {
        return userSettingsRepository.userSettingInfo
            .map { it.selectedAiProviderId }
            .flatMapLatest { aiProviderId ->
                aiProviderId?.let {
                    aiProviderRepository.getProviderById(it)
                } ?: flowOf(null)
            }
    }
}