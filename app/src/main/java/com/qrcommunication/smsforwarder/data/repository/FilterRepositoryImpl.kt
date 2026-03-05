package com.qrcommunication.smsforwarder.data.repository

import com.qrcommunication.smsforwarder.data.local.dao.FilterRuleDao
import com.qrcommunication.smsforwarder.data.local.entity.FilterRule
import com.qrcommunication.smsforwarder.data.local.entity.FilterType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FilterRepositoryImpl @Inject constructor(
    private val dao: FilterRuleDao
) : FilterRepository {

    override fun getAllRules(): Flow<List<FilterRule>> {
        return dao.getAllRules()
    }

    override fun getRulesByType(type: FilterType): Flow<List<FilterRule>> {
        return dao.getRulesByType(type.value)
    }

    override suspend fun getActiveRules(): List<FilterRule> {
        return dao.getActiveRules()
    }

    override suspend fun insertRule(rule: FilterRule): Long {
        return dao.insertRule(rule)
    }

    override suspend fun updateRule(rule: FilterRule) {
        dao.updateRule(rule)
    }

    override suspend fun deleteRule(rule: FilterRule) {
        dao.deleteRule(rule)
    }

    override suspend fun deleteAllRules() {
        dao.deleteAllRules()
    }
}
