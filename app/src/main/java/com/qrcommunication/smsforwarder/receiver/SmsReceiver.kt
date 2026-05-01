package com.qrcommunication.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SubscriptionManager
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
        val simSlot = resolveSimSlot(context, intent)

        Log.d(TAG, "SMS received from: $senderNumber, length: ${messageBody.length}, simSlot: $simSlot")

        val serviceIntent = Intent(context, SmsForwardService::class.java).apply {
            action = SmsForwardService.ACTION_FORWARD_SMS
            putExtra(SmsForwardService.EXTRA_SENDER, senderNumber)
            putExtra(SmsForwardService.EXTRA_MESSAGE, messageBody)
            putExtra(SmsForwardService.EXTRA_TIMESTAMP, timestamp)
            putExtra(SmsForwardService.EXTRA_SIM_SLOT, simSlot)
        }
        context.startForegroundService(serviceIntent)
    }

    private fun resolveSimSlot(context: Context, intent: Intent): Int {
        val subscriptionId = intent.getIntExtra("subscription", -1)
        if (subscriptionId < 0) return -1
        return try {
            val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
            subscriptionManager?.getActiveSubscriptionInfo(subscriptionId)?.simSlotIndex ?: -1
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot resolve SIM slot, READ_PHONE_STATE missing?", e)
            -1
        }
    }
}
