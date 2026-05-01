package com.qrcommunication.smsforwarder.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmsFormatter {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)

    /**
     * Format unifie : prefere `appLabel` (notif tierce) sinon `sender` (SMS classique).
     */
    fun format(sender: String, timestamp: Long, content: String, appLabel: String? = null): String {
        val date = dateFormat.format(Date(timestamp))
        return if (appLabel != null) {
            "[Notif $appLabel | $date] $content"
        } else {
            "[De: $sender | $date] $content"
        }
    }

    /** Conserve pour retro-compat avec les tests / appels existants. */
    fun formatForwardedSms(sender: String, timestampMs: Long, content: String): String =
        format(sender, timestampMs, content)

    fun getPreview(content: String, maxLength: Int = 50): String {
        return if (content.length <= maxLength) content
        else content.take(maxLength) + "..."
    }

    fun estimatePartCount(message: String): Int {
        return if (message.length <= 160) 1
        else {
            val partLength = 153
            (message.length + partLength - 1) / partLength
        }
    }
}
