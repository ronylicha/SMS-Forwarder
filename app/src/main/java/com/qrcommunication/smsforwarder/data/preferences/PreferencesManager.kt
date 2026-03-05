package com.qrcommunication.smsforwarder.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("sms_forwarder_prefs", Context.MODE_PRIVATE)

    var destinationNumber: String
        get() = prefs.getString(KEY_DESTINATION, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DESTINATION, value).apply()

    var isForwardingEnabled: Boolean
        get() = prefs.getBoolean(KEY_FORWARDING_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_FORWARDING_ENABLED, value).apply()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    var filterMode: String
        get() = prefs.getString(KEY_FILTER_MODE, "NONE") ?: "NONE"
        set(value) = prefs.edit().putString(KEY_FILTER_MODE, value).apply()

    var smsForwardedCount: Int
        get() = prefs.getInt(KEY_SMS_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_SMS_COUNT, value).apply()

    var selectedSimSlot: Int
        get() = prefs.getInt(KEY_SIM_SLOT, -1)
        set(value) = prefs.edit().putInt(KEY_SIM_SLOT, value).apply()

    fun incrementSmsCount() {
        smsForwardedCount = smsForwardedCount + 1
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_DESTINATION = "destination_number"
        private const val KEY_FORWARDING_ENABLED = "forwarding_enabled"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_FILTER_MODE = "filter_mode"
        private const val KEY_SMS_COUNT = "sms_forwarded_count"
        private const val KEY_SIM_SLOT = "selected_sim_slot"
    }
}
