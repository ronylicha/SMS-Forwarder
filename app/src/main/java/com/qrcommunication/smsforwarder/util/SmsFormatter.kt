package com.qrcommunication.smsforwarder.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmsFormatter {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)

    fun formatForwardedSms(sender: String, timestampMs: Long, content: String): String {
        val date = dateFormat.format(Date(timestampMs))
        return "[De: $sender | $date] $content"
    }

    fun getPreview(content: String, maxLength: Int = 50): String {
        return if (content.length <= maxLength) content
        else content.take(maxLength) + "..."
    }

    fun estimatePartCount(message: String): Int {
        return if (message.length <= 160) 1
        else {
            // Multipart: chaque partie fait max 153 chars (7 chars header UDH)
            val partLength = 153
            (message.length + partLength - 1) / partLength
        }
    }
}
