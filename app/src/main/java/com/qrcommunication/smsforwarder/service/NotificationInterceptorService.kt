package com.qrcommunication.smsforwarder.service

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * Capture les notifications des apps de messagerie pour intercepter les RCS
 * qui ne passent pas par SMS_RECEIVED ni par le content provider SMS.
 *
 * Packages surveilles: Google Messages, Samsung Messages, AOSP Messages
 */
class NotificationInterceptorService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotifInterceptor"

        val MESSAGING_PACKAGES = setOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.mms",
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in MESSAGING_PACKAGES) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        val content = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return
        val timestamp = sbn.postTime

        if (sbn.isGroup && extras.getBoolean("android.isGroupSummary", false)) return
        if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return

        Log.d(TAG, "Message notification from ${sbn.packageName}: sender=$sender")

        val serviceIntent = Intent(this, SmsForwardService::class.java).apply {
            action = SmsForwardService.ACTION_FORWARD_SMS
            putExtra(SmsForwardService.EXTRA_SENDER, sender)
            putExtra(SmsForwardService.EXTRA_MESSAGE, content)
            putExtra(SmsForwardService.EXTRA_TIMESTAMP, timestamp)
            putExtra("extra_source", "notification")
        }

        try {
            startForegroundService(serviceIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to forward notification to service", e)
        }
    }
}
