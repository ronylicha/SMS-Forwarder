package com.qrcommunication.smsforwarder.ui

import com.qrcommunication.smsforwarder.data.local.entity.AppNotification
import com.qrcommunication.smsforwarder.data.repository.AppNotificationRepository
import com.qrcommunication.smsforwarder.ui.notifications.NotificationCenterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationCenterViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: AppNotificationRepository = mock()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun createVm(): NotificationCenterViewModel {
        whenever(repository.observeAll()).thenReturn(flowOf(emptyList()))
        whenever(repository.observeUnreadCount()).thenReturn(flowOf(0))
        return NotificationCenterViewModel(repository)
    }

    @Test
    fun markAllAsRead_delegatesToRepository() = runTest(dispatcher) {
        val vm = createVm()
        vm.markAllAsRead()
        advanceUntilIdle()
        verify(repository).markAllAsRead()
    }

    @Test
    fun markAsRead_delegatesToRepository() = runTest(dispatcher) {
        val vm = createVm()
        vm.markAsRead(7L)
        advanceUntilIdle()
        verify(repository).markAsRead(7L)
    }

    @Test
    fun delete_delegatesToRepository() = runTest(dispatcher) {
        val vm = createVm()
        vm.delete(3L)
        advanceUntilIdle()
        verify(repository).delete(3L)
    }

    @Test
    fun deleteAll_delegatesToRepository() = runTest(dispatcher) {
        val vm = createVm()
        vm.deleteAll()
        advanceUntilIdle()
        verify(repository).deleteAll()
    }

    @Test
    fun observeAll_emitsCombinedState() = runTest(dispatcher) {
        val notif = AppNotification(
            id = 1, type = "INFO", severity = "INFO",
            title = "T", message = "M", timestamp = 100L,
        )
        whenever(repository.observeAll()).thenReturn(flowOf(listOf(notif)))
        whenever(repository.observeUnreadCount()).thenReturn(flowOf(1))
        val vm = NotificationCenterViewModel(repository)

        // Trigger SharingStarted.WhileSubscribed by accessing the flow's first emission
        val state = vm.uiState.first { it.notifications.isNotEmpty() }
        advanceUntilIdle()

        assertEquals(1, state.notifications.size)
        assertEquals(1, state.unreadCount)
    }
}
