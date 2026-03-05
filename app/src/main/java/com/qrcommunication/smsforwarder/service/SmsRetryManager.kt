package com.qrcommunication.smsforwarder.service

import android.util.Log
import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsRetryManager @Inject constructor(
    private val smsRepository: SmsRepository,
    private val smsSender: SmsSender,
    private val preferencesManager: PreferencesManager
) {
    companion object {
        private const val TAG = "SmsRetryManager"
        const val MAX_RETRIES = 3
        private const val BASE_DELAY_MS = 2000L
    }

    suspend fun retryFailedSms(record: SmsRecord): Boolean {
        if (record.retryCount >= MAX_RETRIES) {
            Log.w(TAG, "Max retries reached for record ${record.id}")
            return false
        }

        val destination = preferencesManager.destinationNumber
        if (destination.isBlank()) {
            Log.w(TAG, "No destination configured")
            return false
        }

        val delayMs = calculateBackoffDelay(record.retryCount)
        Log.d(TAG, "Retrying SMS ${record.id}, attempt ${record.retryCount + 1}, delay ${delayMs}ms")

        delay(delayMs)

        return try {
            val formattedMessage = formatForwardedSms(record.sender, record.receivedAt, record.content)
            smsSender.sendSms(destination, formattedMessage)
            smsRepository.updateRecord(
                record.copy(
                    status = SmsStatus.SENT.value,
                    forwardedAt = System.currentTimeMillis(),
                    retryCount = record.retryCount + 1,
                    errorMessage = null
                )
            )
            preferencesManager.incrementSmsCount()
            Log.i(TAG, "Retry successful for record ${record.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Retry failed for record ${record.id}", e)
            smsRepository.updateRecord(
                record.copy(
                    status = SmsStatus.FAILED.value,
                    retryCount = record.retryCount + 1,
                    errorMessage = e.message
                )
            )
            false
        }
    }

    suspend fun retryAllFailed() {
        val failedRecords = smsRepository.getRecordsByStatus(SmsStatus.FAILED)
            .first()
            .filter { it.retryCount < MAX_RETRIES }

        Log.d(TAG, "Found ${failedRecords.size} failed records to retry")
        for (record in failedRecords) {
            retryFailedSms(record)
        }
    }

    private fun calculateBackoffDelay(retryCount: Int): Long {
        // Backoff exponentiel: 2s, 4s, 8s (plafonne a 30s)
        val delay = BASE_DELAY_MS * (1L shl retryCount)
        return delay.coerceAtMost(30_000L)
    }

    private fun formatForwardedSms(sender: String, timestamp: Long, content: String): String {
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE)
        val date = dateFormat.format(java.util.Date(timestamp))
        return "[De: $sender | $date] $content"
    }
}
