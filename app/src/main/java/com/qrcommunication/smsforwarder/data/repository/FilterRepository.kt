package com.qrcommunication.smsforwarder.data.repository

import com.qrcommunication.smsforwarder.data.local.entity.FilterRule
import com.qrcommunication.smsforwarder.data.local.entity.FilterType
import kotlinx.coroutines.flow.Flow

interface FilterRepository {
    fun getAllRules(): Flow<List<FilterRule>>
    fun getRulesByType(type: FilterType): Flow<List<FilterRule>>
    suspend fun getActiveRules(): List<FilterRule>
    suspend fun insertRule(rule: FilterRule): Long
    suspend fun updateRule(rule: FilterRule)
    suspend fun deleteRule(rule: FilterRule)
    suspend fun deleteAllRules()
}
