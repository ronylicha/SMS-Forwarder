package com.qrcommunication.smsforwarder.ui

import android.app.Application
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.data.repository.AppNotificationRepository
import com.qrcommunication.smsforwarder.data.repository.ForwardingRuleRepository
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.ui.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val application: Application = mock()
    private val preferencesManager: PreferencesManager = mock()
    private val smsRepository: SmsRepository = mock()
    private val ruleRepository: ForwardingRuleRepository = mock()
    private val notificationRepository: AppNotificationRepository = mock()
    private lateinit var viewModel: MainViewModel

    private fun createViewModel(): MainViewModel = MainViewModel(
        application,
        preferencesManager,
        smsRepository,
        ruleRepository,
        notificationRepository,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        runBlocking {
            whenever(preferencesManager.destinationNumber).thenReturn("+33612345678")
            whenever(preferencesManager.isForwardingEnabled).thenReturn(false)
            whenever(smsRepository.getRecordCountByStatus(any())).thenReturn(flowOf(0))
            whenever(smsRepository.getRecordsForDateRange(any(), any())).thenReturn(emptyList())
            whenever(ruleRepository.observeAll()).thenReturn(flowOf(emptyList()))
            whenever(notificationRepository.observeUnreadCount()).thenReturn(flowOf(0))
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsFromPreferences() {
        whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(true)

        viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals("+33699999999", state.destinationNumber)
        assertTrue(state.isForwardingEnabled)
        assertTrue(state.isDestinationConfigured)
    }

    @Test
    fun initialState_emptyDestination_notConfigured() {
        whenever(preferencesManager.destinationNumber).thenReturn("")

        viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals("", state.destinationNumber)
        assertFalse(state.isDestinationConfigured)
    }

    @Test
    fun toggleForwarding_updatesState() {
        viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.isForwardingEnabled)

        viewModel.toggleForwarding()

        assertTrue(viewModel.uiState.value.isForwardingEnabled)
        verify(preferencesManager).isForwardingEnabled = true
    }

    @Test
    fun toggleForwarding_enableThenDisable() {
        viewModel = createViewModel()
        viewModel.toggleForwarding()
        assertTrue(viewModel.uiState.value.isForwardingEnabled)

        viewModel.toggleForwarding()
        assertFalse(viewModel.uiState.value.isForwardingEnabled)
    }

    @Test
    fun destinationNotConfigured_disablesToggle() {
        whenever(preferencesManager.destinationNumber).thenReturn("")
        viewModel = createViewModel()

        viewModel.toggleForwarding()

        assertFalse(viewModel.uiState.value.isForwardingEnabled)
        verify(preferencesManager, never()).isForwardingEnabled = true
    }

    @Test
    fun blankDestination_treatedAsNotConfigured() {
        whenever(preferencesManager.destinationNumber).thenReturn("   ")
        viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.isDestinationConfigured)
    }
}
