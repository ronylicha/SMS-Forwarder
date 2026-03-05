package com.qrcommunication.smsforwarder.domain.usecase

import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.domain.validator.FilterEngine
import com.qrcommunication.smsforwarder.domain.validator.FilterResult
import com.qrcommunication.smsforwarder.service.LoopProtection
import com.qrcommunication.smsforwarder.service.SmsSender
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ForwardSmsUseCaseTest {

    private val smsRepository: SmsRepository = mock()
    private val smsSender: SmsSender = mock()
    private val preferencesManager: PreferencesManager = mock()
    private val filterEngine: FilterEngine = mock()
    private val loopProtection: LoopProtection = mock()
    private lateinit var useCase: ForwardSmsUseCase

    private val sender = "+33612345678"
    private val content = "Test message"
    private val timestamp = 1709564400000L

    @Before
    fun setUp() {
        useCase = ForwardSmsUseCase(
            smsRepository, smsSender, preferencesManager, filterEngine, loopProtection
        )
    }

    @Test
    fun invoke_success_returnSuccess() = runTest {
        // Arrange
        whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(true)
        whenever(loopProtection.isLoopDetected(any(), any())).thenReturn(false)
        whenever(filterEngine.shouldForward(any(), any())).thenReturn(
            FilterResult(shouldForward = true, reason = "No filter active")
        )
        whenever(smsRepository.insertRecord(any())).thenReturn(1L)

        // Act
        val result = useCase(sender, content, timestamp)

        // Assert
        assertTrue(result is ForwardResult.Success)
        assertEquals(1L, (result as ForwardResult.Success).recordId)
    }

    @Test
    fun invoke_noDestination_returnSkipped() = runTest {
        // Arrange
        whenever(preferencesManager.destinationNumber).thenReturn("")

        // Act
        val result = useCase(sender, content, timestamp)

        // Assert
        assertTrue(result is ForwardResult.Skipped)
        assertEquals("No destination configured", (result as ForwardResult.Skipped).reason)
    }

    @Test
    fun invoke_blankDestination_returnSkipped() = runTest {
        // Arrange
        whenever(preferencesManager.destinationNumber).thenReturn("   ")

        // Act
        val result = useCase(sender, content, timestamp)

        // Assert
        assertTrue(result is ForwardResult.Skipped)
        assertEquals("No destination configured", (result as ForwardResult.Skipped).reason)
    }

    @Test
    fun invoke_forwardingDisabled_returnSkipped() = runTest {
        // Arrange
        whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(false)

        // Act
        val result = useCase(sender, content, timestamp)

        // Assert
        assertTrue(result is ForwardResult.Skipped)
        assertEquals("Forwarding disabled", (result as ForwardResult.Skipped).reason)
    }

    @Test
    fun invoke_loopDetected_returnSkipped() = runTest {
        // Arrange
        whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(true)
        whenever(loopProtection.isLoopDetected(any(), any())).thenReturn(true)

        // Act
        val result = useCase(sender, content, timestamp)

        // Assert
        assertTrue(result is ForwardResult.Skipped)
        assertEquals("Loop detected", (result as ForwardResult.Skipped).reason)
    }

    @Test
    fun invoke_filtered_returnFiltered() = runTest {
        // Arrange
        whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(true)
        whenever(loopProtection.isLoopDetected(any(), any())).thenReturn(false)
        whenever(filterEngine.shouldForward(any(), any())).thenReturn(
            FilterResult(shouldForward = false, reason = "Not in whitelist")
        )
        whenever(smsRepository.insertRecord(any())).thenReturn(5L)

        // Act
        val result = useCase(sender, content, timestamp)

        // Assert
        assertTrue(result is ForwardResult.Filtered)
        val filtered = result as ForwardResult.Filtered
        assertEquals(5L, filtered.recordId)
        assertEquals("Not in whitelist", filtered.reason)
    }

    @Test
    fun invoke_sendFails_returnFailed() = runTest {
        // Arrange
        whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(true)
        whenever(loopProtection.isLoopDetected(any(), any())).thenReturn(false)
        whenever(filterEngine.shouldForward(any(), any())).thenReturn(
            FilterResult(shouldForward = true, reason = "No filter active")
        )
        whenever(smsRepository.insertRecord(any())).thenReturn(3L)
        whenever(smsSender.sendSms(any(), any())).thenThrow(RuntimeException("SMS service unavailable"))

        // Act
        val result = useCase(sender, content, timestamp)

        // Assert
        assertTrue(result is ForwardResult.Failed)
        val failed = result as ForwardResult.Failed
        assertEquals(3L, failed.recordId)
        assertEquals("SMS service unavailable", failed.error)
    }

    @Test
    fun invoke_success_incrementsCount() = runTest {
        // Arrange
        whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(true)
        whenever(loopProtection.isLoopDetected(any(), any())).thenReturn(false)
        whenever(filterEngine.shouldForward(any(), any())).thenReturn(
            FilterResult(shouldForward = true, reason = "No filter active")
        )
        whenever(smsRepository.insertRecord(any())).thenReturn(1L)

        // Act
        useCase(sender, content, timestamp)

        // Assert
        verify(preferencesManager).incrementSmsCount()
    }

    @Test
    fun invoke_success_insertsRecordAsSent() = runTest {
        // Arrange
        whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(true)
        whenever(loopProtection.isLoopDetected(any(), any())).thenReturn(false)
        whenever(filterEngine.shouldForward(any(), any())).thenReturn(
            FilterResult(shouldForward = true, reason = "No filter active")
        )
        whenever(smsRepository.insertRecord(any())).thenReturn(7L)

        // Act
        useCase(sender, content, timestamp)

        // Assert
        verify(smsRepository).updateStatus(eq(7L), eq(SmsStatus.SENT), eq(null))
    }

    @Test
    fun invoke_sendFails_updatesStatusToFailed() = runTest {
        // Arrange
        whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(true)
        whenever(loopProtection.isLoopDetected(any(), any())).thenReturn(false)
        whenever(filterEngine.shouldForward(any(), any())).thenReturn(
            FilterResult(shouldForward = true, reason = "No filter active")
        )
        whenever(smsRepository.insertRecord(any())).thenReturn(4L)
        whenever(smsSender.sendSms(any(), any())).thenThrow(RuntimeException("Network error"))

        // Act
        useCase(sender, content, timestamp)

        // Assert
        verify(smsRepository).updateStatus(eq(4L), eq(SmsStatus.FAILED), eq("Network error"))
    }

    @Test
    fun invoke_sendFails_doesNotIncrementCount() = runTest {
        // Arrange
        whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
        whenever(preferencesManager.isForwardingEnabled).thenReturn(true)
        whenever(loopProtection.isLoopDetected(any(), any())).thenReturn(false)
        whenever(filterEngine.shouldForward(any(), any())).thenReturn(
            FilterResult(shouldForward = true, reason = "No filter active")
        )
        whenever(smsRepository.insertRecord(any())).thenReturn(4L)
        whenever(smsSender.sendSms(any(), any())).thenThrow(RuntimeException("Error"))

        // Act
        useCase(sender, content, timestamp)

        // Assert
        verify(preferencesManager, never()).incrementSmsCount()
    }
}
