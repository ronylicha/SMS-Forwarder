package com.qrcommunication.smsforwarder.service

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoopProtection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LoopProtection"
    }

    fun isLoopDetected(sender: String, destination: String): Boolean {
        if (normalizeNumber(sender) == normalizeNumber(destination)) {
            Log.w(TAG, "Direct loop detected: sender matches destination")
            return true
        }

        val localNumbers = getLocalPhoneNumbers()
        val normalizedDestination = normalizeNumber(destination)
        for (localNumber in localNumbers) {
            if (normalizeNumber(localNumber) == normalizedDestination) {
                Log.w(TAG, "SIM loop detected: destination matches local SIM number")
                return true
            }
        }

        return false
    }

    private fun getLocalPhoneNumbers(): List<String> {
        val numbers = mutableListOf<String>()
        try {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            val line1 = telephonyManager?.line1Number
            if (!line1.isNullOrBlank()) {
                numbers.add(line1)
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
