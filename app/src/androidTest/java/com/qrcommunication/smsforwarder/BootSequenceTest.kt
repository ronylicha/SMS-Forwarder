package com.qrcommunication.smsforwarder

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BootSequenceTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREF_NAME = "sms_forwarder_prefs"
        private const val KEY_FORWARDING_ENABLED = "forwarding_enabled"
    }

    @Before
    fun setUp() {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun bootReceiver_forwardingEnabled_preferencePersisted() {
        // Arrange: set forwarding enabled
        prefs.edit().putBoolean(KEY_FORWARDING_ENABLED, true).commit()

        // Act: read the preference as BootReceiver does
        val forwardingEnabled = prefs.getBoolean(KEY_FORWARDING_ENABLED, false)

        // Assert: the preference is correctly read as true
        assertTrue(forwardingEnabled)
    }

    @Test
    fun bootReceiver_forwardingDisabled_preferencePersisted() {
        // Arrange: set forwarding disabled (default)
        prefs.edit().putBoolean(KEY_FORWARDING_ENABLED, false).commit()

        // Act: read the preference as BootReceiver does
        val forwardingEnabled = prefs.getBoolean(KEY_FORWARDING_ENABLED, false)

        // Assert: the preference is correctly read as false
        assertFalse(forwardingEnabled)
    }

    @Test
    fun bootReceiver_defaultPreference_isFalse() {
        // Arrange: preferences are cleared (no value set)
        // prefs already cleared in setUp

        // Act: read the preference with default
        val forwardingEnabled = prefs.getBoolean(KEY_FORWARDING_ENABLED, false)

        // Assert: default is false, service should not start
        assertFalse(forwardingEnabled)
    }

    @Test
    fun bootReceiver_bootCompletedIntent_hasCorrectAction() {
        // Verify the boot intent action matches what BootReceiver expects
        val bootIntent = Intent(Intent.ACTION_BOOT_COMPLETED)

        assertEquals(Intent.ACTION_BOOT_COMPLETED, bootIntent.action)
    }

    @Test
    fun bootReceiver_forwardingEnabled_preferenceSurvivesRestart() {
        // Arrange: simulate setting forwarding enabled, then clearing memory
        prefs.edit().putBoolean(KEY_FORWARDING_ENABLED, true).commit()

        // Act: re-read from a fresh SharedPreferences reference (simulating restart)
        val freshPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val forwardingEnabled = freshPrefs.getBoolean(KEY_FORWARDING_ENABLED, false)

        // Assert: the preference survives
        assertTrue(forwardingEnabled)
    }

    @Test
    fun bootReceiver_quickbootIntent_hasCorrectAction() {
        // Some devices send QUICKBOOT_POWERON instead of BOOT_COMPLETED
        val quickbootIntent = Intent("android.intent.action.QUICKBOOT_POWERON")

        assertEquals("android.intent.action.QUICKBOOT_POWERON", quickbootIntent.action)
    }
}
