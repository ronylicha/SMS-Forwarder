package com.qrcommunication.smsforwarder.util

import android.Manifest
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.qrcommunication.smsforwarder.ui.components.common.CheckSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DiagnosticsRunnerTest {

    private lateinit var context: Application
    private lateinit var shadowApp: ShadowApplication
    private lateinit var runner: DiagnosticsRunner

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        shadowApp = shadowOf(context)
        runner = DiagnosticsRunner(context)
    }

    @Test
    fun runAll_returnsAllExpectedChecks() {
        val checks = runner.runAll()
        val ids = checks.map { it.id }

        // Permissions individuelles
        assertTrue(ids.contains("perm_${Manifest.permission.RECEIVE_SMS}"))
        assertTrue(ids.contains("perm_${Manifest.permission.SEND_SMS}"))
        assertTrue(ids.contains("perm_${Manifest.permission.READ_SMS}"))
        assertTrue(ids.contains("perm_${Manifest.permission.READ_PHONE_STATE}"))

        // Checks supplementaires
        assertTrue(ids.contains("notification_listener"))
        assertTrue(ids.contains("battery_optimization"))
        assertTrue(ids.contains("network"))
    }

    @Test
    fun permissionCheck_notGranted_isErrorWithFixIntent() {
        // Robolectric : aucune permission n'est accordee par defaut
        val checks = runner.runAll()
        val sms = checks.first { it.id == "perm_${Manifest.permission.RECEIVE_SMS}" }

        assertEquals(CheckSeverity.ERROR, sms.severity)
        assertNotNull("permission missing -> fix intent disponible", sms.fixIntent)
        assertEquals("Ouvrir parametres", sms.fixLabel)
    }

    @Test
    fun permissionCheck_granted_isOkWithoutFixIntent() {
        shadowApp.grantPermissions(Manifest.permission.RECEIVE_SMS)
        val checks = runner.runAll()
        val sms = checks.first { it.id == "perm_${Manifest.permission.RECEIVE_SMS}" }

        assertEquals(CheckSeverity.OK, sms.severity)
        assertNull(sms.fixIntent)
    }

    @Test
    fun batteryOptimization_default_warningWithIgnoreIntent() {
        val checks = runner.runAll()
        val battery = checks.first { it.id == "battery_optimization" }

        // Robolectric retourne par defaut isIgnoring = false -> warning
        assertEquals(CheckSeverity.WARNING, battery.severity)
        assertEquals("Demander exception", battery.fixLabel)
        assertNotNull(battery.fixIntent)
    }

    @Test
    fun notificationListener_default_warning() {
        val checks = runner.runAll()
        val listener = checks.first { it.id == "notification_listener" }
        // Robolectric : pas dans enabled_notification_listeners par defaut
        assertEquals(CheckSeverity.WARNING, listener.severity)
        assertNotNull(listener.fixIntent)
    }

    @Test
    fun network_check_returnsOneOfExpectedSeverities() {
        val checks = runner.runAll()
        val net = checks.first { it.id == "network" }
        // Selon shadow connectivity manager, severity peut etre OK ou INFO
        assertTrue(net.severity == CheckSeverity.OK || net.severity == CheckSeverity.INFO)
    }
}
