package com.qrcommunication.smsforwarder.data

import android.content.Context
import android.content.SharedPreferences
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PreferencesManagerTest {

    private val context: Context = mock()
    private val sharedPreferences: SharedPreferences = mock()
    private val editor: SharedPreferences.Editor = mock()
    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setUp() {
        whenever(context.getSharedPreferences(any(), eq(Context.MODE_PRIVATE)))
            .thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        whenever(editor.putBoolean(any(), any())).thenReturn(editor)
        whenever(editor.putInt(any(), any())).thenReturn(editor)
        whenever(editor.clear()).thenReturn(editor)

        preferencesManager = PreferencesManager(context)
    }

    @Test
    fun destinationNumber_defaultEmpty() {
        whenever(sharedPreferences.getString(eq("destination_number"), eq(""))).thenReturn("")

        val result = preferencesManager.destinationNumber

        assertEquals("", result)
    }

    @Test
    fun destinationNumber_setAndGet() {
        whenever(sharedPreferences.getString(eq("destination_number"), eq("")))
            .thenReturn("+33612345678")

        val result = preferencesManager.destinationNumber

        assertEquals("+33612345678", result)
    }

    @Test
    fun destinationNumber_set_callsPutString() {
        preferencesManager.destinationNumber = "+33699999999"

        verify(editor).putString("destination_number", "+33699999999")
        verify(editor).apply()
    }

    @Test
    fun isForwardingEnabled_defaultFalse() {
        whenever(sharedPreferences.getBoolean(eq("forwarding_enabled"), eq(false)))
            .thenReturn(false)

        val result = preferencesManager.isForwardingEnabled

        assertFalse(result)
    }

    @Test
    fun isForwardingEnabled_setTrue() {
        preferencesManager.isForwardingEnabled = true

        verify(editor).putBoolean("forwarding_enabled", true)
        verify(editor).apply()
    }

    @Test
    fun isFirstLaunch_defaultTrue() {
        whenever(sharedPreferences.getBoolean(eq("first_launch"), eq(true)))
            .thenReturn(true)

        val result = preferencesManager.isFirstLaunch

        assertTrue(result)
    }

    @Test
    fun filterMode_defaultNone() {
        whenever(sharedPreferences.getString(eq("filter_mode"), eq("NONE")))
            .thenReturn("NONE")

        val result = preferencesManager.filterMode

        assertEquals("NONE", result)
    }

    @Test
    fun smsForwardedCount_defaultZero() {
        whenever(sharedPreferences.getInt(eq("sms_forwarded_count"), eq(0)))
            .thenReturn(0)

        val result = preferencesManager.smsForwardedCount

        assertEquals(0, result)
    }

    @Test
    fun incrementSmsCount_incrementsByOne() {
        whenever(sharedPreferences.getInt(eq("sms_forwarded_count"), eq(0)))
            .thenReturn(5)

        preferencesManager.incrementSmsCount()

        verify(editor).putInt("sms_forwarded_count", 6)
        verify(editor).apply()
    }

    @Test
    fun incrementSmsCount_fromZero() {
        whenever(sharedPreferences.getInt(eq("sms_forwarded_count"), eq(0)))
            .thenReturn(0)

        preferencesManager.incrementSmsCount()

        verify(editor).putInt("sms_forwarded_count", 1)
        verify(editor).apply()
    }

    @Test
    fun selectedSimSlot_defaultMinusOne() {
        whenever(sharedPreferences.getInt(eq("selected_sim_slot"), eq(-1)))
            .thenReturn(-1)

        val result = preferencesManager.selectedSimSlot

        assertEquals(-1, result)
    }

    @Test
    fun clear_resetsAll() {
        preferencesManager.clear()

        verify(editor).clear()
        verify(editor).apply()
    }
}
