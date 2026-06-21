package com.qrcommunication.smsforwarder.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Manages app-wide locale override.
 *
 * Strategy: FR by default if the phone is in French, otherwise EN.
 * The user can override this in Settings (System / French / English).
 *
 * Usage:
 *   - In MainActivity.attachBaseContext, call LocaleManager.applyLocale(base)
 *   - To change language: LocaleManager.setLanguage(context, code) then recreate activity
 */
object LocaleManager {

    const val LANGUAGE_SYSTEM = "system"
    const val LANGUAGE_FRENCH = "fr"
    const val LANGUAGE_ENGLISH = "en"

    /**
     * Resolve the effective locale code.
     * - "system" → resolves to the device language (fr if device is French, en otherwise)
     * - "fr" / "en" → explicit override
     */
    fun resolveLanguage(preference: String): String {
        if (preference == LANGUAGE_SYSTEM) {
            val deviceLang = Locale.getDefault().language
            return if (deviceLang == LANGUAGE_FRENCH) LANGUAGE_FRENCH else LANGUAGE_ENGLISH
        }
        return preference
    }

    /**
     * Apply the saved locale preference to a context.
     * Call this in Activity.attachBaseContext.
     */
    fun applyLocale(context: Context): Context {
        val prefs = context.getSharedPreferences("sms_forwarder_prefs", Context.MODE_PRIVATE)
        val pref = prefs.getString("app_language", LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
        val lang = resolveLanguage(pref)
        return updateLocale(context, lang)
    }

    /**
     * Force a specific locale on a context.
     */
    private fun updateLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        // Prevent leaking the old locale to other configs
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    /**
     * Save the language preference. The caller should recreate the activity afterwards.
     */
    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences("sms_forwarder_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("app_language", language)
            .apply()
    }

    /**
     * Get the current language preference.
     */
    fun getLanguage(context: Context): String {
        return context.getSharedPreferences("sms_forwarder_prefs", Context.MODE_PRIVATE)
            .getString("app_language", LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
    }
}
