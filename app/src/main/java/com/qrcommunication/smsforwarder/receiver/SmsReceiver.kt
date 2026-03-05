package com.qrcommunication.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.qrcommunication.smsforwarder.service.SmsForwardService

class SmsReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) {
            Log.w(TAG, "Received SMS intent but no messages found")
            return
        }

        val senderNumber = messages[0].displayOriginatingAddress ?: "Unknown"
        val messageBody = messages.joinToString(separator = "") { it.displayMessageBody ?: "" }
        val timestamp = messages[0].timestampMillis

        Log.d(TAG, "SMS received from: $senderNumber, length: ${messageBody.length}")

        val serviceIntent = Intent(context, SmsForwardService::class.java).apply {
            action = SmsForwardService.ACTION_FORWARD_SMS
            putExtra(SmsForwardService.EXTRA_SENDER, senderNumber)
            putExtra(SmsForwardService.EXTRA_MESSAGE, messageBody)
            putExtra(SmsForwardService.EXTRA_TIMESTAMP, timestamp)
        }
        context.startForegroundService(serviceIntent)
    }
}
