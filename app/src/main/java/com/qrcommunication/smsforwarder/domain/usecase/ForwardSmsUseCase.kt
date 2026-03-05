package com.qrcommunication.smsforwarder.domain.usecase

import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.domain.validator.FilterEngine
import com.qrcommunication.smsforwarder.service.LoopProtection
import com.qrcommunication.smsforwarder.service.SmsSender
import com.qrcommunication.smsforwarder.util.SmsFormatter
import javax.inject.Inject

sealed class ForwardResult {
    data class Success(val recordId: Long) : ForwardResult()
    data class Filtered(val recordId: Long, val reason: String) : ForwardResult()
    data class Failed(val recordId: Long, val error: String) : ForwardResult()
    data class Skipped(val reason: String) : ForwardResult()
}

class ForwardSmsUseCase @Inject constructor(
    private val smsRepository: SmsRepository,
    private val smsSender: SmsSender,
    private val preferencesManager: PreferencesManager,
    private val filterEngine: FilterEngine,
    private val loopProtection: LoopProtection
) {
    suspend operator fun invoke(sender: String, content: String, timestampMs: Long): ForwardResult {
        val destination = preferencesManager.destinationNumber
        if (destination.isBlank()) return ForwardResult.Skipped("No destination configured")
        if (!preferencesManager.isForwardingEnabled) return ForwardResult.Skipped("Forwarding disabled")
        if (loopProtection.isLoopDetected(sender, destination)) return ForwardResult.Skipped("Loop detected")

        // Verifier les filtres
        val filterResult = filterEngine.shouldForward(sender, content)
        if (!filterResult.shouldForward) {
            val record = SmsRecord(
                sender = sender,
                content = content,
                receivedAt = timestampMs,
                status = SmsStatus.FILTERED.value,
                destination = destination,
                errorMessage = filterResult.reason
            )
            val recordId = smsRepository.insertRecord(record)
            return ForwardResult.Filtered(recordId, filterResult.reason)
        }

        // Inserer le record en PENDING
        val record = SmsRecord(
            sender = sender,
            content = content,
            receivedAt = timestampMs,
            status = SmsStatus.PENDING.value,
            destination = destination
        )
        val recordId = smsRepository.insertRecord(record)

        // Envoyer le SMS
        return try {
            val formattedMessage = SmsFormatter.formatForwardedSms(sender, timestampMs, content)
            smsSender.sendSms(destination, formattedMessage)
            smsRepository.updateStatus(recordId, SmsStatus.SENT)
            preferencesManager.incrementSmsCount()
            ForwardResult.Success(recordId)
        } catch (e: Exception) {
            smsRepository.updateStatus(recordId, SmsStatus.FAILED, e.message)
            ForwardResult.Failed(recordId, e.message ?: "Unknown error")
        }
    }
}
