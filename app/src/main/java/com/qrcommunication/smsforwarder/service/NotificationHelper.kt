package com.qrcommunication.smsforwarder.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.qrcommunication.smsforwarder.R
import com.qrcommunication.smsforwarder.SmsForwarderApp
import com.qrcommunication.smsforwarder.ui.main.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun createServiceNotification(destination: String, smsCount: Int): Notification {
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, SmsForwardService::class.java).apply {
            action = SmsForwardService.ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            context, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (destination.isNotBlank()) {
            context.getString(R.string.notification_forwarding_to, destination, smsCount)
        } else {
            context.getString(R.string.notification_service_text)
        }

        return NotificationCompat.Builder(context, SmsForwarderApp.CHANNEL_ID_FORWARDING)
            .setContentTitle(context.getString(R.string.notification_service_title))
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                context.getString(R.string.main_stop_service),
                stopPendingIntent
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun createSmsNotification(sender: String, preview: String): Notification {
        return NotificationCompat.Builder(context, SmsForwarderApp.CHANNEL_ID_STATUS)
            .setContentTitle(context.getString(R.string.notification_sms_forwarded_title))
            .setContentText("De: $sender - $preview")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setAutoCancel(true)
            .build()
    }

    fun updateNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}
