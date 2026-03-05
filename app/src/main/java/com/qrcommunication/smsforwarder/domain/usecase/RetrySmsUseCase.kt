package com.qrcommunication.smsforwarder.domain.usecase

import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.service.SmsRetryManager
import javax.inject.Inject

sealed class RetryResult {
    data object Success : RetryResult()
    data class Failed(val error: String) : RetryResult()
    data object NotFound : RetryResult()
    data object MaxRetriesReached : RetryResult()
}

class RetrySmsUseCase @Inject constructor(
    private val smsRepository: SmsRepository,
    private val smsRetryManager: SmsRetryManager
) {
    suspend operator fun invoke(recordId: Long): RetryResult {
        val record = smsRepository.getRecordById(recordId)
            ?: return RetryResult.NotFound

        if (record.status != SmsStatus.FAILED.value) {
            return RetryResult.Failed("Record is not in FAILED status")
        }

        if (record.retryCount >= SmsRetryManager.MAX_RETRIES) {
            return RetryResult.MaxRetriesReached
        }

        return if (smsRetryManager.retryFailedSms(record)) {
            RetryResult.Success
        } else {
            RetryResult.Failed("Retry failed after attempt")
        }
    }
}
