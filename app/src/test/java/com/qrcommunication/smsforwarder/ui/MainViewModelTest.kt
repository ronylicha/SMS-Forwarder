package com.qrcommunication.smsforwarder.ui

import android.app.Application
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.ui.main.MainViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MainViewModelTest {

    private val application: Application = mock()
    private val preferencesManager: PreferencesManager = mock()
    private lateinit var viewModel: MainViewModel

    private fun createViewModel(): MainViewModel {
        return MainViewModel(application, preferencesManager)
    }

    @Before
    fun setUp() {
        // Default preferences state
        whenever(preferencesManager.destinationNumber).thenReturn("+33612345678")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(false)
        whenever(preferencesManager.smsForwardedCount).thenReturn(0)
    }

    @Test
    fun initialState_loadsFromPreferences() {
        whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(true)
        whenever(preferencesManager.smsForwardedCount).thenReturn(42)

        viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals("+33699999999", state.destinationNumber)
        assertTrue(state.isForwardingEnabled)
        assertEquals(42, state.smsForwardedCount)
        assertTrue(state.isDestinationConfigured)
    }

    @Test
    fun initialState_emptyDestination_notConfigured() {
        whenever(preferencesManager.destinationNumber).thenReturn("")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(false)
        whenever(preferencesManager.smsForwardedCount).thenReturn(0)

        viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals("", state.destinationNumber)
        assertFalse(state.isForwardingEnabled)
        assertFalse(state.isDestinationConfigured)
    }

    @Test
    fun toggleForwarding_updatesState() {
        whenever(preferencesManager.destinationNumber).thenReturn("+33612345678")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(false)
        whenever(preferencesManager.smsForwardedCount).thenReturn(0)
        viewModel = createViewModel()

        // Initial state: not forwarding
        assertFalse(viewModel.uiState.value.isForwardingEnabled)

        // Toggle on
        viewModel.toggleForwarding()

        assertTrue(viewModel.uiState.value.isForwardingEnabled)
        verify(preferencesManager).isForwardingEnabled = true
    }

    @Test
    fun toggleForwarding_enableThenDisable() {
        whenever(preferencesManager.destinationNumber).thenReturn("+33612345678")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(false)
        whenever(preferencesManager.smsForwardedCount).thenReturn(0)
        viewModel = createViewModel()

        // Toggle on
        viewModel.toggleForwarding()
        assertTrue(viewModel.uiState.value.isForwardingEnabled)

        // Toggle off
        viewModel.toggleForwarding()
        assertFalse(viewModel.uiState.value.isForwardingEnabled)
    }

    @Test
    fun refreshState_reloadsFromPreferences() {
        whenever(preferencesManager.destinationNumber).thenReturn("+33612345678")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(false)
        whenever(preferencesManager.smsForwardedCount).thenReturn(0)
        viewModel = createViewModel()

        // Simulate preferences changed externally
        whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(true)
        whenever(preferencesManager.smsForwardedCount).thenReturn(10)

        viewModel.refreshState()
        val state = viewModel.uiState.value

        assertEquals("+33699999999", state.destinationNumber)
        assertTrue(state.isForwardingEnabled)
        assertEquals(10, state.smsForwardedCount)
        assertTrue(state.isDestinationConfigured)
    }

    @Test
    fun destinationNotConfigured_disablesToggle() {
        whenever(preferencesManager.destinationNumber).thenReturn("")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(false)
        whenever(preferencesManager.smsForwardedCount).thenReturn(0)
        viewModel = createViewModel()

        // Try to toggle when no destination configured
        viewModel.toggleForwarding()

        // Should remain disabled
        assertFalse(viewModel.uiState.value.isForwardingEnabled)
        // isForwardingEnabled setter should NOT have been called
        verify(preferencesManager, never()).isForwardingEnabled = true
    }

    @Test
    fun blankDestination_treatedAsNotConfigured() {
        whenever(preferencesManager.destinationNumber).thenReturn("   ")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(false)
        whenever(preferencesManager.smsForwardedCount).thenReturn(0)
        viewModel = createViewModel()

        // " " is not blank (isNotBlank checks), but let's verify the state
        // Note: the code uses isNotBlank() which would return true for "   " -> false
        // Actually "   ".isNotBlank() is false in Kotlin, so isDestinationConfigured = false
        assertFalse(viewModel.uiState.value.isDestinationConfigured)
    }
}
