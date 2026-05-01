package com.qrcommunication.smsforwarder.service.sender

/**
 * Payload neutre passe au dispatcher : independant du canal de destination.
 */
data class MessagePayload(
    val sender: String,
    val content: String,
    val receivedAt: Long,
    val sourceLabel: String? = null,
    val originalDestination: String? = null,
)
