package com.qrcommunication.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.qrcommunication.smsforwarder.service.SmsForwardService

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
        private const val PREF_NAME = "sms_forwarder_prefs"
        private const val KEY_FORWARDING_ENABLED = "forwarding_enabled"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return

        Log.d(TAG, "Boot completed, checking forwarding state")

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val forwardingEnabled = prefs.getBoolean(KEY_FORWARDING_ENABLED, false)

        if (forwardingEnabled) {
            Log.i(TAG, "Forwarding was enabled, restarting service")
            val serviceIntent = Intent(context, SmsForwardService::class.java)
            context.startForegroundService(serviceIntent)
        } else {
            Log.d(TAG, "Forwarding was disabled, service not started")
        }
    }
}
