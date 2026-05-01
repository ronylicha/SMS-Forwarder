package com.qrcommunication.smsforwarder.ui

import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.service.SmsSender
import com.qrcommunication.smsforwarder.ui.onboarding.OnboardingViewModel
import com.qrcommunication.smsforwarder.ui.onboarding.TestStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val preferencesManager: PreferencesManager = mock()
    private val smsSender: SmsSender = mock()

    private fun createViewModel() = OnboardingViewModel(preferencesManager, smsSender)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updateDestination_validNumber_marksValid() {
        val vm = createViewModel()
        vm.updateDestination("+33612345678")
        val state = vm.uiState.value
        assertEquals("+33612345678", state.destinationNumber)
        assertTrue(state.isNumberValid)
    }

    @Test
    fun updateDestination_invalidNumber_marksInvalid() {
        val vm = createViewModel()
        vm.updateDestination("not-a-number")
        assertFalse(vm.uiState.value.isNumberValid)
    }

    @Test
    fun sendTestSms_success_setsSentStatus() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.updateDestination("+33612345678")

        vm.sendTestSms()
        advanceUntilIdle()

        verify(smsSender).sendSms(eq("+33612345678"), any())
        assertEquals(TestStatus.SENT, vm.uiState.value.testStatus)
    }

    @Test
    fun sendTestSms_failure_setsFailedStatus() = runTest(testDispatcher) {
        whenever(smsSender.sendSms(any(), any())).doThrow(RuntimeException("boom"))
        val vm = createViewModel()
        vm.updateDestination("+33612345678")

        vm.sendTestSms()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(TestStatus.FAILED, state.testStatus)
        assertNotNull(state.testError)
    }

    @Test
    fun sendTestSms_invalidNumber_doesNotSend() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.updateDestination("invalid")
        vm.sendTestSms()
        advanceUntilIdle()
        assertEquals(TestStatus.NOT_RUN, vm.uiState.value.testStatus)
    }

    @Test
    fun finalize_validNumber_persistsPreferences() {
        val vm = createViewModel()
        vm.updateDestination("+33612345678")
        vm.finalize()
        verify(preferencesManager).destinationNumber = "+33612345678"
        verify(preferencesManager).isFirstLaunch = false
    }

    @Test
    fun finalize_invalidNumber_skipsDestinationButMarksLaunched() {
        val vm = createViewModel()
        vm.updateDestination("garbage")
        vm.finalize()
        verify(preferencesManager).isFirstLaunch = false
        // destinationNumber NOT set
    }
}
