package com.qrcommunication.smsforwarder.service

import android.content.Context
import android.telephony.TelephonyManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LoopProtectionTest {

    private val context: Context = mock()
    private val telephonyManager: TelephonyManager = mock()
    private lateinit var loopProtection: LoopProtection

    @Before
    fun setUp() {
        whenever(context.getSystemService(TelephonyManager::class.java))
            .thenReturn(telephonyManager)
        // Default: no SIM line number available
        whenever(telephonyManager.line1Number).thenReturn(null)

        loopProtection = LoopProtection(context)
    }

    @Test
    fun isLoopDetected_sameNumber_returnsTrue() {
        val result = loopProtection.isLoopDetected("+33612345678", "+33612345678")

        assertTrue(result)
    }

    @Test
    fun isLoopDetected_differentNumbers_returnsFalse() {
        val result = loopProtection.isLoopDetected("+33612345678", "+33699999999")

        assertFalse(result)
    }

    @Test
    fun isLoopDetected_normalizedMatch_returnsTrue() {
        // 0612345678 normalizes to +33612345678
        val result = loopProtection.isLoopDetected("0612345678", "+33612345678")

        assertTrue(result)
    }

    @Test
    fun isLoopDetected_localVsInternational_returnsTrue() {
        // Both should normalize to +33612345678
        val result = loopProtection.isLoopDetected("+33612345678", "0612345678")

        assertTrue(result)
    }

    @Test
    fun isLoopDetected_doubleZeroVsE164_returnsTrue() {
        val result = loopProtection.isLoopDetected("0033612345678", "+33612345678")

        assertTrue(result)
    }

    @Test
    fun isLoopDetected_completelyDifferent_returnsFalse() {
        val result = loopProtection.isLoopDetected("+33612345678", "+1234567890")

        assertFalse(result)
    }

    @Test
    fun isLoopDetected_withSpaces_normalizes() {
        // Spaces and dashes are stripped during normalization
        val result = loopProtection.isLoopDetected("+33 6 12 34 56 78", "+33612345678")

        assertTrue(result)
    }

    @Test
    fun isLoopDetected_simNumberMatchesDestination_returnsTrue() {
        // SIM card has number +33611111111
        whenever(telephonyManager.line1Number).thenReturn("+33611111111")
        // Recreate LoopProtection to pick up the new mock
        loopProtection = LoopProtection(context)

        // Sender is different, but destination matches the SIM number
        val result = loopProtection.isLoopDetected("+33699999999", "+33611111111")

        assertTrue(result)
    }

    @Test
    fun isLoopDetected_simNumberDoesNotMatchDestination_returnsFalse() {
        whenever(telephonyManager.line1Number).thenReturn("+33611111111")
        loopProtection = LoopProtection(context)

        val result = loopProtection.isLoopDetected("+33612345678", "+33699999999")

        assertFalse(result)
    }

    @Test
    fun isLoopDetected_simSecurityException_gracefullyHandled() {
        whenever(telephonyManager.line1Number).thenThrow(SecurityException("No permission"))
        loopProtection = LoopProtection(context)

        // Should not crash; just rely on direct comparison
        val result = loopProtection.isLoopDetected("+33612345678", "+33699999999")

        assertFalse(result)
    }
}
