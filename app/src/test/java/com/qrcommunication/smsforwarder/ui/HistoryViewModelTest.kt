package com.qrcommunication.smsforwarder.ui

import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.domain.usecase.GetHistoryUseCase
import com.qrcommunication.smsforwarder.ui.history.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val getHistoryUseCase: GetHistoryUseCase = mock()
    private val smsRepository: SmsRepository = mock()
    private val testDispatcher = StandardTestDispatcher()

    private val sampleRecords = listOf(
        SmsRecord(
            id = 1L,
            sender = "+33612345678",
            content = "Hello world",
            receivedAt = System.currentTimeMillis(),
            status = SmsStatus.SENT.value,
            destination = "+33699999999"
        ),
        SmsRecord(
            id = 2L,
            sender = "+33611111111",
            content = "Test message",
            receivedAt = System.currentTimeMillis(),
            status = SmsStatus.FAILED.value,
            destination = "+33699999999"
        ),
        SmsRecord(
            id = 3L,
            sender = "+33622222222",
            content = "Filtered content",
            receivedAt = System.currentTimeMillis(),
            status = SmsStatus.FILTERED.value,
            destination = "+33699999999"
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HistoryViewModel {
        return HistoryViewModel(getHistoryUseCase, smsRepository)
    }

    @Test
    fun initialLoad_populatesRecords() = runTest {
        // Arrange
        whenever(getHistoryUseCase.getAllRecords()).thenReturn(flowOf(sampleRecords))
        whenever(getHistoryUseCase.getRecordCount()).thenReturn(flowOf(3))

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(3, state.records.size)
        assertFalse(state.isLoading)
        assertEquals(3, state.totalCount)
    }

    @Test
    fun initialLoad_emptyList() = runTest {
        // Arrange
        whenever(getHistoryUseCase.getAllRecords()).thenReturn(flowOf(emptyList()))
        whenever(getHistoryUseCase.getRecordCount()).thenReturn(flowOf(0))

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.records.isEmpty())
        assertFalse(state.isLoading)
        assertEquals(0, state.totalCount)
    }

    @Test
    fun search_filtersRecords() = runTest {
        // Arrange
        val searchResults = listOf(sampleRecords[0])
        whenever(getHistoryUseCase.getAllRecords()).thenReturn(flowOf(sampleRecords))
        whenever(getHistoryUseCase.getRecordCount()).thenReturn(flowOf(3))
        whenever(getHistoryUseCase.searchRecords("Hello")).thenReturn(flowOf(searchResults))

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.search("Hello")
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(1, state.records.size)
        assertEquals("Hello world", state.records[0].content)
        assertEquals("Hello", state.searchQuery)
        assertNull(state.selectedStatusFilter)
    }

    @Test
    fun search_emptyQuery_showsAll() = runTest {
        // Arrange
        whenever(getHistoryUseCase.getAllRecords()).thenReturn(flowOf(sampleRecords))
        whenever(getHistoryUseCase.getRecordCount()).thenReturn(flowOf(3))

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.search("")
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(3, state.records.size)
        assertEquals("", state.searchQuery)
    }

    @Test
    fun filterByStatus_filtersCorrectly() = runTest {
        // Arrange
        val failedRecords = listOf(sampleRecords[1])
        whenever(getHistoryUseCase.getAllRecords()).thenReturn(flowOf(sampleRecords))
        whenever(getHistoryUseCase.getRecordCount()).thenReturn(flowOf(3))
        whenever(getHistoryUseCase.getRecordsByStatus(SmsStatus.FAILED))
            .thenReturn(flowOf(failedRecords))

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByStatus(SmsStatus.FAILED)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(1, state.records.size)
        assertEquals(SmsStatus.FAILED.value, state.records[0].status)
        assertEquals(SmsStatus.FAILED, state.selectedStatusFilter)
        assertEquals("", state.searchQuery)
    }

    @Test
    fun filterByStatus_nullClearsFilter() = runTest {
        // Arrange
        whenever(getHistoryUseCase.getAllRecords()).thenReturn(flowOf(sampleRecords))
        whenever(getHistoryUseCase.getRecordCount()).thenReturn(flowOf(3))
        whenever(getHistoryUseCase.getRecordsByStatus(SmsStatus.SENT))
            .thenReturn(flowOf(listOf(sampleRecords[0])))

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Apply filter
        viewModel.filterByStatus(SmsStatus.SENT)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.records.size)

        // Clear filter
        viewModel.filterByStatus(null)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(3, state.records.size)
        assertNull(state.selectedStatusFilter)
    }

    @Test
    fun search_resetsStatusFilter() = runTest {
        // Arrange
        whenever(getHistoryUseCase.getAllRecords()).thenReturn(flowOf(sampleRecords))
        whenever(getHistoryUseCase.getRecordCount()).thenReturn(flowOf(3))
        whenever(getHistoryUseCase.getRecordsByStatus(any())).thenReturn(flowOf(emptyList()))
        whenever(getHistoryUseCase.searchRecords(any())).thenReturn(flowOf(emptyList()))

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.filterByStatus(SmsStatus.SENT)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.selectedStatusFilter)

        viewModel.search("query")
        advanceUntilIdle()

        // Assert
        assertNull(viewModel.uiState.value.selectedStatusFilter)
        assertEquals("query", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun filterByStatus_resetsSearchQuery() = runTest {
        // Arrange
        whenever(getHistoryUseCase.getAllRecords()).thenReturn(flowOf(sampleRecords))
        whenever(getHistoryUseCase.getRecordCount()).thenReturn(flowOf(3))
        whenever(getHistoryUseCase.searchRecords(any())).thenReturn(flowOf(emptyList()))
        whenever(getHistoryUseCase.getRecordsByStatus(any())).thenReturn(flowOf(emptyList()))

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.search("some query")
        advanceUntilIdle()
        assertEquals("some query", viewModel.uiState.value.searchQuery)

        viewModel.filterByStatus(SmsStatus.FAILED)
        advanceUntilIdle()

        // Assert
        assertEquals("", viewModel.uiState.value.searchQuery)
        assertEquals(SmsStatus.FAILED, viewModel.uiState.value.selectedStatusFilter)
    }
}
