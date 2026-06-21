package com.qrcommunication.smsforwarder.data.preferences

import android.content.Context
import com.qrcommunication.smsforwarder.domain.model.RetryPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
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

    var receivingSimSlot: Int
        get() = prefs.getInt(KEY_RECEIVING_SIM_SLOT, -1)
        set(value) = prefs.edit().putInt(KEY_RECEIVING_SIM_SLOT, value).apply()

    var isAppWhitelistEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_WHITELIST_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_APP_WHITELIST_ENABLED, value).apply()

    var appWhitelistPackages: Set<String>
        get() = prefs.getStringSet(KEY_APP_WHITELIST_PACKAGES, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_APP_WHITELIST_PACKAGES, value).apply()

    /** Politique de retry serialisee en 4 cles distinctes (pas de JSON). */
    var retryPolicy: RetryPolicy
        get() = RetryPolicy(
            maxAttempts = prefs.getInt(KEY_RETRY_MAX_ATTEMPTS, RetryPolicy.DEFAULT_MAX_ATTEMPTS),
            initialDelayMs = prefs.getLong(KEY_RETRY_INITIAL_DELAY, RetryPolicy.DEFAULT_INITIAL_DELAY_MS),
            backoffMultiplier = prefs.getFloat(
                KEY_RETRY_BACKOFF,
                RetryPolicy.DEFAULT_BACKOFF.toFloat(),
            ).toDouble(),
            maxDelayMs = prefs.getLong(KEY_RETRY_MAX_DELAY, RetryPolicy.DEFAULT_MAX_DELAY_MS),
        )
        set(value) = prefs.edit()
            .putInt(KEY_RETRY_MAX_ATTEMPTS, value.maxAttempts)
            .putLong(KEY_RETRY_INITIAL_DELAY, value.initialDelayMs)
            .putFloat(KEY_RETRY_BACKOFF, value.backoffMultiplier.toFloat())
            .putLong(KEY_RETRY_MAX_DELAY, value.maxDelayMs)
            .apply()

    var appLanguage: String
        get() = prefs.getString(KEY_APP_LANGUAGE, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_APP_LANGUAGE, value).apply()

    fun addAppToWhitelist(packageName: String) {
        appWhitelistPackages = appWhitelistPackages + packageName
    }

    fun removeAppFromWhitelist(packageName: String) {
        appWhitelistPackages = appWhitelistPackages - packageName
    }

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
        private const val KEY_RECEIVING_SIM_SLOT = "receiving_sim_slot"
        private const val KEY_APP_WHITELIST_ENABLED = "app_whitelist_enabled"
        private const val KEY_APP_WHITELIST_PACKAGES = "app_whitelist_packages"
        private const val KEY_RETRY_MAX_ATTEMPTS = "retry_max_attempts"
        private const val KEY_RETRY_INITIAL_DELAY = "retry_initial_delay_ms"
        private const val KEY_RETRY_BACKOFF = "retry_backoff"
        private const val KEY_RETRY_MAX_DELAY = "retry_max_delay_ms"
        private const val KEY_APP_LANGUAGE = "app_language"
    }
}
