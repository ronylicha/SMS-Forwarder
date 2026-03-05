package com.qrcommunication.smsforwarder.domain.usecase

import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val smsRepository: SmsRepository
) {
    fun getAllRecords(): Flow<List<SmsRecord>> = smsRepository.getAllRecords()

    fun getRecordsByStatus(status: SmsStatus): Flow<List<SmsRecord>> =
        smsRepository.getRecordsByStatus(status)

    fun searchRecords(query: String): Flow<List<SmsRecord>> =
        smsRepository.searchRecords(query)

    fun getRecordCount(): Flow<Int> = smsRepository.getRecordCount()

    fun getRecordCountByStatus(status: SmsStatus): Flow<Int> =
        smsRepository.getRecordCountByStatus(status)

    suspend fun getRecordById(id: Long): SmsRecord? = smsRepository.getRecordById(id)
}
