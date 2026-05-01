package com.qrcommunication.smsforwarder.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.qrcommunication.smsforwarder.data.local.entity.DestinationType
import com.qrcommunication.smsforwarder.data.local.entity.NotificationSeverity
import com.qrcommunication.smsforwarder.data.local.entity.NotificationType
import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.data.repository.AppNotificationRepository
import com.qrcommunication.smsforwarder.data.repository.ForwardingRuleRepository
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.domain.usecase.MatchForwardingRuleUseCase
import com.qrcommunication.smsforwarder.service.sender.DestinationDispatcher
import com.qrcommunication.smsforwarder.service.sender.MessagePayload
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
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loopProtection: LoopProtection
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var deduplicator: MessageDeduplicator
    @Inject lateinit var dispatcher: DestinationDispatcher
    @Inject lateinit var matchRule: MatchForwardingRuleUseCase
    @Inject lateinit var ruleRepository: ForwardingRuleRepository
    @Inject lateinit var notificationRepository: AppNotificationRepository

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
        const val EXTRA_SIM_SLOT = "extra_sim_slot"
        const val EXTRA_APP_LABEL = "extra_app_label"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        startForegroundWithNotification()

        contentObserver = SmsContentObserver(this) { sender, body, timestamp, simSlot ->
            handleForwardSms(sender, body, timestamp, simSlot)
        }
        contentObserver?.register()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_FORWARD_SMS -> {
                val sender = intent.getStringExtra(EXTRA_SENDER) ?: "Unknown"
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
                val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, System.currentTimeMillis())
                val simSlot = intent.getIntExtra(EXTRA_SIM_SLOT, -1)
                val appLabel = intent.getStringExtra(EXTRA_APP_LABEL)
                handleForwardSms(sender, message, timestamp, simSlot, appLabel)
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
            smsCount = preferencesManager.smsForwardedCount,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun handleForwardSms(
        sender: String,
        message: String,
        timestamp: Long,
        simSlot: Int = -1,
        appLabel: String? = null,
    ) {
        if (!deduplicator.shouldProcess(sender, message, timestamp)) {
            Log.d(TAG, "Duplicate message from $sender, skipping")
            return
        }

        if (appLabel == null && !passesSimFilter(simSlot)) return
        if (!preferencesManager.isForwardingEnabled) {
            Log.d(TAG, "Forwarding disabled, skipping")
            return
        }

        serviceScope.launch {
            val matchedRule = matchRule(sender, message)
            val destinationType = matchedRule?.let { DestinationType.fromValue(it.destinationType) }
                ?: DestinationType.SMS
            val destination = matchedRule?.destination ?: preferencesManager.destinationNumber

            if (destination.isBlank()) {
                Log.w(TAG, "No destination configured (no rule + no global), skipping")
                return@launch
            }

            // Loop protection : uniquement pour SMS (un webhook ne peut pas creer de SMS)
            if (destinationType == DestinationType.SMS &&
                loopProtection.isLoopDetected(sender, destination)
            ) {
                Log.w(TAG, "Loop detected: sender=$sender matches destination=$destination")
                return@launch
            }

            val record = SmsRecord(
                sender = sender,
                content = message,
                receivedAt = timestamp,
                status = SmsStatus.PENDING.value,
                destination = destination,
                ruleId = matchedRule?.id,
            )
            val recordId = smsRepository.insertRecord(record)
            val payload = MessagePayload(
                sender = sender,
                content = message,
                receivedAt = timestamp,
                sourceLabel = appLabel,
                originalDestination = preferencesManager.destinationNumber.takeIf { it.isNotBlank() },
            )

            try {
                dispatcher.dispatch(destinationType, destination, payload)
                smsRepository.updateStatus(recordId, SmsStatus.SENT)
                preferencesManager.incrementSmsCount()
                matchedRule?.id?.let { ruleRepository.recordSuccess(it) }
                updateNotification()
                Log.i(TAG, "Forwarded successfully via $destinationType to $destination")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to forward via $destinationType", e)
                val errorMessage = e.message ?: e.javaClass.simpleName
                smsRepository.updateStatus(recordId, SmsStatus.FAILED, errorMessage)
                matchedRule?.id?.let { ruleRepository.recordFailure(it, errorMessage) }
                publishFailureNotification(matchedRule?.name, destinationType, destination, errorMessage, recordId, matchedRule?.id)
            }
        }
    }

    private fun passesSimFilter(simSlot: Int): Boolean {
        val filter = preferencesManager.receivingSimSlot
        if (filter < 0 || simSlot < 0) return true
        if (simSlot == filter) return true
        Log.d(TAG, "SMS from SIM $simSlot ignored (filter set to SIM $filter)")
        return false
    }

    private suspend fun publishFailureNotification(
        ruleName: String?,
        type: DestinationType,
        destination: String,
        error: String,
        recordId: Long,
        ruleId: Long?,
    ) {
        val title = if (ruleName != null) "Echec regle '$ruleName'" else "Echec transfert"
        notificationRepository.notify(
            type = if (ruleName != null) NotificationType.RULE_ERROR else NotificationType.DESTINATION_UNREACHABLE,
            severity = NotificationSeverity.ERROR,
            title = title,
            message = "Vers ${type.name} $destination : $error",
            recordId = recordId,
            ruleId = ruleId,
        )
    }

    private fun updateNotification() {
        val notification = notificationHelper.createServiceNotification(
            destination = preferencesManager.destinationNumber,
            smsCount = preferencesManager.smsForwardedCount,
        )
        notificationHelper.updateNotification(NOTIFICATION_ID, notification)
    }
}
