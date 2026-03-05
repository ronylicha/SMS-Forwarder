package com.qrcommunication.smsforwarder.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsForwardService : Service() {

    @Inject lateinit var smsRepository: SmsRepository
    @Inject lateinit var smsSender: SmsSender
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loopProtection: LoopProtection
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var deduplicator: MessageDeduplicator

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var contentObserver: SmsContentObserver? = null

    companion object {
        private const val TAG = "SmsForwardService"
        const val NOTIFICATION_ID = 1001

        const val ACTION_FORWARD_SMS = "com.qrcommunication.smsforwarder.action.FORWARD_SMS"
        const val ACTION_STOP_SERVICE = "com.qrcommunication.smsforwarder.action.STOP_SERVICE"

        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        startForegroundWithNotification()

        contentObserver = SmsContentObserver(this) { sender, body, timestamp ->
            handleForwardSms(sender, body, timestamp)
        }
        contentObserver?.register()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_FORWARD_SMS -> {
                val sender = intent.getStringExtra(EXTRA_SENDER) ?: "Unknown"
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
                val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, System.currentTimeMillis())
                handleForwardSms(sender, message, timestamp)
            }
            ACTION_STOP_SERVICE -> {
                Log.i(TAG, "Stop action received")
                preferencesManager.isForwardingEnabled = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        contentObserver?.unregister()
        contentObserver = null
        serviceScope.cancel()
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }

    private fun startForegroundWithNotification() {
        val notification = notificationHelper.createServiceNotification(
            destination = preferencesManager.destinationNumber,
            smsCount = preferencesManager.smsForwardedCount
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun handleForwardSms(sender: String, message: String, timestamp: Long) {
        if (!deduplicator.shouldProcess(sender, message, timestamp)) {
            Log.d(TAG, "Duplicate message from $sender, skipping")
            return
        }

        val destination = preferencesManager.destinationNumber
        if (destination.isBlank()) {
            Log.w(TAG, "No destination configured, skipping")
            return
        }
        if (!preferencesManager.isForwardingEnabled) {
            Log.d(TAG, "Forwarding disabled, skipping")
            return
        }
        if (loopProtection.isLoopDetected(sender, destination)) {
            Log.w(TAG, "Loop detected: sender=$sender matches destination=$destination")
            return
        }

        serviceScope.launch {
            val record = SmsRecord(
                sender = sender,
                content = message,
                receivedAt = timestamp,
                status = SmsStatus.PENDING.value,
                destination = destination
            )
            val recordId = smsRepository.insertRecord(record)

            try {
                val formattedMessage = formatForwardedSms(sender, timestamp, message)
                smsSender.sendSms(destination, formattedMessage)
                smsRepository.updateStatus(recordId, SmsStatus.SENT)
                preferencesManager.incrementSmsCount()
                updateNotification()
                Log.i(TAG, "SMS forwarded successfully from $sender")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to forward SMS from $sender", e)
                smsRepository.updateStatus(recordId, SmsStatus.FAILED, e.message)
            }
        }
    }

    private fun formatForwardedSms(sender: String, timestamp: Long, content: String): String {
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE)
        val date = dateFormat.format(java.util.Date(timestamp))
        return "[De: $sender | $date] $content"
    }

    private fun updateNotification() {
        val notification = notificationHelper.createServiceNotification(
            destination = preferencesManager.destinationNumber,
            smsCount = preferencesManager.smsForwardedCount
        )
        notificationHelper.updateNotification(NOTIFICATION_ID, notification)
    }
}
