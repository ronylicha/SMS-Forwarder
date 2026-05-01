package com.qrcommunication.smsforwarder.service

import android.content.Context
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoopProtection @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "LoopProtection"
    }

    fun isLoopDetected(sender: String, destination: String): Boolean {
        if (normalizeNumber(sender) == normalizeNumber(destination)) {
            Log.w(TAG, "Direct loop detected: sender matches destination")
            return true
        }

        val normalizedDestination = normalizeNumber(destination)
        return getLocalPhoneNumbers().any { normalizeNumber(it) == normalizedDestination }.also {
            if (it) Log.w(TAG, "SIM loop detected: destination matches local SIM number")
        }
    }

    /**
     * Recupere les numeros locaux des SIM. Sur API 33+, utilise SubscriptionManager.getPhoneNumber
     * qui requiert READ_PHONE_NUMBERS. Sur API < 33, fallback sur TelephonyManager.line1Number
     * (deprecated mais seul moyen disponible).
     */
    private fun getLocalPhoneNumbers(): List<String> {
        val numbers = mutableListOf<String>()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
                val activeSubs = subscriptionManager?.activeSubscriptionInfoList.orEmpty()
                for (sub in activeSubs) {
                    val number = subscriptionManager?.getPhoneNumber(sub.subscriptionId)
                    if (!number.isNullOrBlank()) numbers.add(number)
                }
            } else {
                @Suppress("DEPRECATION")
                val line1 = context.getSystemService(TelephonyManager::class.java)?.line1Number
                if (!line1.isNullOrBlank()) numbers.add(line1)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot read phone number, loop detection limited", e)
        }
        return numbers
    }

    private fun normalizeNumber(number: String): String {
        val cleaned = number.replace(Regex("[^+0-9]"), "")
        return when {
            cleaned.startsWith("+33") -> cleaned
            cleaned.startsWith("0033") -> "+33" + cleaned.drop(4)
            cleaned.startsWith("0") && cleaned.length == 10 -> "+33" + cleaned.drop(1)
            else -> cleaned
        }
    }
}
