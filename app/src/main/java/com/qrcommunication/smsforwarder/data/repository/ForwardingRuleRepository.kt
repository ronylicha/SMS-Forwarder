package com.qrcommunication.smsforwarder.data.repository

import com.qrcommunication.smsforwarder.data.local.entity.ForwardingRule
import kotlinx.coroutines.flow.Flow

interface ForwardingRuleRepository {
    fun observeAll(): Flow<List<ForwardingRule>>
    suspend fun getEnabledOrdered(): List<ForwardingRule>
    suspend fun getById(id: Long): ForwardingRule?

    /** Cree ou met a jour. Retourne l'id (insertion) ou rule.id (update). */
    suspend fun upsert(rule: ForwardingRule): Long

    suspend fun delete(rule: ForwardingRule)
    suspend fun setEnabled(id: Long, enabled: Boolean)
    suspend fun recordSuccess(id: Long)
    suspend fun recordFailure(id: Long, error: String?)
}
