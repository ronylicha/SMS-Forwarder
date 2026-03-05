package com.qrcommunication.smsforwarder.data.repository

import com.qrcommunication.smsforwarder.data.local.dao.SmsRecordDao
import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SmsRepositoryImpl @Inject constructor(
    private val dao: SmsRecordDao
) : SmsRepository {

    override fun getAllRecords(): Flow<List<SmsRecord>> {
        return dao.getAllRecords()
    }

    override fun getRecordsByStatus(status: SmsStatus): Flow<List<SmsRecord>> {
        return dao.getRecordsByStatus(status.value)
    }

    override fun getRecordCount(): Flow<Int> {
        return dao.getRecordCount()
    }

    override fun getRecordCountByStatus(status: SmsStatus): Flow<Int> {
        return dao.getRecordCountByStatus(status.value)
    }

    override fun searchRecords(query: String): Flow<List<SmsRecord>> {
        return dao.searchRecords(query)
    }

    override suspend fun getRecordById(id: Long): SmsRecord? {
        return dao.getRecordById(id)
    }

    override suspend fun insertRecord(record: SmsRecord): Long {
        return dao.insertRecord(record)
    }

    override suspend fun updateRecord(record: SmsRecord) {
        dao.updateRecord(record)
    }

    override suspend fun updateStatus(id: Long, status: SmsStatus, errorMessage: String?) {
        val record = dao.getRecordById(id) ?: return
        val updatedRecord = record.copy(
            status = status.value,
            errorMessage = errorMessage,
            forwardedAt = if (status == SmsStatus.SENT) System.currentTimeMillis() else record.forwardedAt
        )
        dao.updateRecord(updatedRecord)
    }

    override suspend fun getRecordsForDateRange(startMs: Long, endMs: Long): List<SmsRecord> {
        return dao.getRecordsForDateRange(startMs, endMs)
    }

    override suspend fun deleteAllRecords() {
        dao.deleteAllRecords()
    }
}
