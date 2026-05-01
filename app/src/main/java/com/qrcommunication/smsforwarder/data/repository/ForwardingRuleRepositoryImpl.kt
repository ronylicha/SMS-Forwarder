package com.qrcommunication.smsforwarder.data.repository

import com.qrcommunication.smsforwarder.data.local.dao.ForwardingRuleDao
import com.qrcommunication.smsforwarder.data.local.entity.ForwardingRule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForwardingRuleRepositoryImpl @Inject constructor(
    private val dao: ForwardingRuleDao,
) : ForwardingRuleRepository {

    override fun observeAll(): Flow<List<ForwardingRule>> = dao.observeAll()

    override suspend fun getEnabledOrdered(): List<ForwardingRule> = dao.getEnabledOrdered()

    override suspend fun getById(id: Long): ForwardingRule? = dao.getById(id)

    override suspend fun upsert(rule: ForwardingRule): Long {
        val now = System.currentTimeMillis()
        return if (rule.id == 0L) {
            dao.insert(rule.copy(createdAt = now, updatedAt = now))
        } else {
            dao.update(rule.copy(updatedAt = now))
            rule.id
        }
    }

    override suspend fun delete(rule: ForwardingRule) = dao.delete(rule)

    override suspend fun setEnabled(id: Long, enabled: Boolean) =
        dao.setEnabled(id, enabled, System.currentTimeMillis())

    override suspend fun recordSuccess(id: Long) =
        dao.recordSuccess(id, System.currentTimeMillis())

    override suspend fun recordFailure(id: Long, error: String?) =
        dao.recordFailure(id, System.currentTimeMillis(), error)
}
