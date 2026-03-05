package com.qrcommunication.smsforwarder.domain.usecase

import com.qrcommunication.smsforwarder.data.local.entity.FilterRule
import com.qrcommunication.smsforwarder.data.local.entity.FilterType
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.data.repository.FilterRepository
import com.qrcommunication.smsforwarder.domain.validator.FilterMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageFiltersUseCase @Inject constructor(
    private val filterRepository: FilterRepository,
    private val preferencesManager: PreferencesManager
) {
    fun getAllRules(): Flow<List<FilterRule>> = filterRepository.getAllRules()

    fun getRulesByType(type: FilterType): Flow<List<FilterRule>> =
        filterRepository.getRulesByType(type)

    fun getCurrentMode(): FilterMode =
        FilterMode.fromString(preferencesManager.filterMode)

    fun setFilterMode(mode: FilterMode) {
        preferencesManager.filterMode = mode.name
    }

    suspend fun addRule(pattern: String, type: FilterType): Long {
        val rule = FilterRule(
            pattern = pattern.trim(),
            type = type.value,
            isActive = true,
            createdAt = System.currentTimeMillis()
        )
        return filterRepository.insertRule(rule)
    }

    suspend fun toggleRule(rule: FilterRule) {
        filterRepository.updateRule(rule.copy(isActive = !rule.isActive))
    }

    suspend fun deleteRule(rule: FilterRule) {
        filterRepository.deleteRule(rule)
    }

    suspend fun deleteAllRules() {
        filterRepository.deleteAllRules()
        preferencesManager.filterMode = FilterMode.NONE.name
    }
}
