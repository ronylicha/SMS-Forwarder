package com.qrcommunication.smsforwarder.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsSender @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    companion object {
        private const val TAG = "SmsSender"
        const val ACTION_SMS_SENT = "com.qrcommunication.smsforwarder.SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.qrcommunication.smsforwarder.SMS_DELIVERED"
    }

    fun sendSms(destination: String, message: String) {
        val smsManager = getSmsManager()

        if (message.length <= 160) {
            val sentIntent = createSentPendingIntent()
            val deliveredIntent = createDeliveredPendingIntent()
            smsManager.sendTextMessage(destination, null, message, sentIntent, deliveredIntent)
            Log.d(TAG, "Single SMS sent to $destination")
        } else {
            val parts = smsManager.divideMessage(message)
            val sentIntents = ArrayList<PendingIntent>(parts.size)
            val deliveredIntents = ArrayList<PendingIntent>(parts.size)
            for (i in parts.indices) {
                sentIntents.add(createSentPendingIntent(i))
                deliveredIntents.add(createDeliveredPendingIntent(i))
            }
            smsManager.sendMultipartTextMessage(destination, null, parts, sentIntents, deliveredIntents)
            Log.d(TAG, "Multipart SMS (${parts.size} parts) sent to $destination")
        }
    }

    private fun getSmsManager(): SmsManager {
        val simSlot = preferencesManager.selectedSimSlot
        return if (simSlot >= 0 && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            try {
                val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
                val subscriptionInfo = subscriptionManager?.activeSubscriptionInfoList?.getOrNull(simSlot)
                if (subscriptionInfo != null) {
                    SmsManager.getSmsManagerForSubscriptionId(subscriptionInfo.subscriptionId)
                } else {
                    context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "Cannot access SIM info, using default SmsManager", e)
                context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
            }
        } else {
            context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
        }
    }

    private fun createSentPendingIntent(requestCode: Int = 0): PendingIntent {
        val intent = Intent(ACTION_SMS_SENT)
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createDeliveredPendingIntent(requestCode: Int = 0): PendingIntent {
        val intent = Intent(ACTION_SMS_DELIVERED)
        return PendingIntent.getBroadcast(
            context, 100 + requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
