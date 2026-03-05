package com.qrcommunication.smsforwarder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmsForwarderApp : Application() {

    companion object {
        const val CHANNEL_ID_FORWARDING = "sms_forwarding_channel"
        const val CHANNEL_ID_STATUS = "sms_status_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val forwardingChannel = NotificationChannel(
            CHANNEL_ID_FORWARDING,
            getString(R.string.channel_forwarding_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_forwarding_description)
        }

        val statusChannel = NotificationChannel(
            CHANNEL_ID_STATUS,
            getString(R.string.channel_status_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.channel_status_description)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(forwardingChannel)
        notificationManager.createNotificationChannel(statusChannel)
    }
}
