package com.qrcommunication.smsforwarder.service

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Capture les notifications des apps de messagerie pour intercepter les RCS
 * qui ne passent pas par SMS_RECEIVED ni par le content provider SMS.
 *
 * Supporte egalement le transfert de notifications d'apps tierces whitelistees.
 */
@AndroidEntryPoint
class NotificationInterceptorService : NotificationListenerService() {

    @Inject lateinit var preferencesManager: PreferencesManager

    companion object {
        private const val TAG = "NotifInterceptor"

        val MESSAGING_PACKAGES = setOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.mms",
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        when {
            packageName in MESSAGING_PACKAGES -> handleSmsNotification(sbn)
            preferencesManager.isAppWhitelistEnabled &&
                packageName in preferencesManager.appWhitelistPackages -> handleThirdPartyNotification(sbn)
        }
    }

    private fun handleSmsNotification(sbn: StatusBarNotification) {
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

    private fun handleThirdPartyNotification(sbn: StatusBarNotification) {
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val content = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()

        if (content.isNullOrBlank()) return

        if (sbn.isGroup && extras.getBoolean("android.isGroupSummary", false)) return
        if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return

        val appLabel = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        } catch (_: Exception) {
            sbn.packageName
        }

        val message = if (title != null) "$title: $content" else content
        val timestamp = sbn.postTime

        Log.d(TAG, "Third-party notification from $appLabel: $message")

        val serviceIntent = Intent(this, SmsForwardService::class.java).apply {
            action = SmsForwardService.ACTION_FORWARD_SMS
            putExtra(SmsForwardService.EXTRA_SENDER, appLabel)
            putExtra(SmsForwardService.EXTRA_MESSAGE, message)
            putExtra(SmsForwardService.EXTRA_TIMESTAMP, timestamp)
            putExtra(SmsForwardService.EXTRA_APP_LABEL, appLabel)
            putExtra("extra_source", "third_party_notification")
        }

        try {
            startForegroundService(serviceIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to forward third-party notification to service", e)
        }
    }
}
