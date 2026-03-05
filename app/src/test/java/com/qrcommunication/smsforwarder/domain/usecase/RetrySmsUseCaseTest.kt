package com.qrcommunication.smsforwarder.domain.usecase

import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.service.SmsRetryManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RetrySmsUseCaseTest {

    private val smsRepository: SmsRepository = mock()
    private val smsRetryManager: SmsRetryManager = mock()
    private lateinit var useCase: RetrySmsUseCase

    @Before
    fun setUp() {
        useCase = RetrySmsUseCase(smsRepository, smsRetryManager)
    }

    private fun createRecord(
        id: Long = 1L,
        status: String = SmsStatus.FAILED.value,
        retryCount: Int = 0
    ): SmsRecord = SmsRecord(
        id = id,
        sender = "+33612345678",
        content = "Test message",
        receivedAt = System.currentTimeMillis(),
        status = status,
        destination = "+33699999999",
        retryCount = retryCount
    )

    @Test
    fun invoke_recordNotFound_returnNotFound() = runTest {
        // Arrange
        whenever(smsRepository.getRecordById(99L)).thenReturn(null)

        // Act
        val result = useCase(99L)

        // Assert
        assertTrue(result is RetryResult.NotFound)
    }

    @Test
    fun invoke_recordNotFailed_returnFailed() = runTest {
        // Arrange
        val record = createRecord(id = 1L, status = SmsStatus.SENT.value)
        whenever(smsRepository.getRecordById(1L)).thenReturn(record)

        // Act
        val result = useCase(1L)

        // Assert
        assertTrue(result is RetryResult.Failed)
        assertEquals("Record is not in FAILED status", (result as RetryResult.Failed).error)
    }

    @Test
    fun invoke_recordPending_returnFailed() = runTest {
        // Arrange
        val record = createRecord(id = 2L, status = SmsStatus.PENDING.value)
        whenever(smsRepository.getRecordById(2L)).thenReturn(record)

        // Act
        val result = useCase(2L)

        // Assert
        assertTrue(result is RetryResult.Failed)
        assertEquals("Record is not in FAILED status", (result as RetryResult.Failed).error)
    }

    @Test
    fun invoke_maxRetriesReached_returnMaxRetries() = runTest {
        // Arrange
        val record = createRecord(id = 1L, retryCount = SmsRetryManager.MAX_RETRIES)
        whenever(smsRepository.getRecordById(1L)).thenReturn(record)

        // Act
        val result = useCase(1L)

        // Assert
        assertTrue(result is RetryResult.MaxRetriesReached)
    }

    @Test
    fun invoke_retriesExceeded_returnMaxRetries() = runTest {
        // Arrange
        val record = createRecord(id = 1L, retryCount = SmsRetryManager.MAX_RETRIES + 1)
        whenever(smsRepository.getRecordById(1L)).thenReturn(record)

        // Act
        val result = useCase(1L)

        // Assert
        assertTrue(result is RetryResult.MaxRetriesReached)
    }

    @Test
    fun invoke_retrySuccess_returnSuccess() = runTest {
        // Arrange
        val record = createRecord(id = 1L, retryCount = 1)
        whenever(smsRepository.getRecordById(1L)).thenReturn(record)
        whenever(smsRetryManager.retryFailedSms(any())).thenReturn(true)

        // Act
        val result = useCase(1L)

        // Assert
        assertTrue(result is RetryResult.Success)
    }

    @Test
    fun invoke_retryFails_returnFailed() = runTest {
        // Arrange
        val record = createRecord(id = 1L, retryCount = 0)
        whenever(smsRepository.getRecordById(1L)).thenReturn(record)
        whenever(smsRetryManager.retryFailedSms(any())).thenReturn(false)

        // Act
        val result = useCase(1L)

        // Assert
        assertTrue(result is RetryResult.Failed)
        assertEquals("Retry failed after attempt", (result as RetryResult.Failed).error)
    }

    @Test
    fun invoke_retryWithCountJustBelowMax_returnSuccess() = runTest {
        // Arrange
        val record = createRecord(id = 1L, retryCount = SmsRetryManager.MAX_RETRIES - 1)
        whenever(smsRepository.getRecordById(1L)).thenReturn(record)
        whenever(smsRetryManager.retryFailedSms(any())).thenReturn(true)

        // Act
        val result = useCase(1L)

        // Assert
        assertTrue(result is RetryResult.Success)
    }
}
