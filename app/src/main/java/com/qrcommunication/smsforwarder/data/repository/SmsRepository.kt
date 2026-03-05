package com.qrcommunication.smsforwarder.data.repository

import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    fun getAllRecords(): Flow<List<SmsRecord>>
    fun getRecordsByStatus(status: SmsStatus): Flow<List<SmsRecord>>
    fun getRecordCount(): Flow<Int>
    fun getRecordCountByStatus(status: SmsStatus): Flow<Int>
    fun searchRecords(query: String): Flow<List<SmsRecord>>
    suspend fun getRecordById(id: Long): SmsRecord?
    suspend fun insertRecord(record: SmsRecord): Long
    suspend fun updateRecord(record: SmsRecord)
    suspend fun updateStatus(id: Long, status: SmsStatus, errorMessage: String? = null)
    suspend fun getRecordsForDateRange(startMs: Long, endMs: Long): List<SmsRecord>
    suspend fun deleteAllRecords()
}
