package com.qrcommunication.smsforwarder.data.repository

import com.qrcommunication.smsforwarder.data.local.dao.AppNotificationDao
import com.qrcommunication.smsforwarder.data.local.entity.AppNotification
import com.qrcommunication.smsforwarder.data.local.entity.NotificationSeverity
import com.qrcommunication.smsforwarder.data.local.entity.NotificationType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AppNotificationRepositoryImplTest {

    private val dao: AppNotificationDao = mock()
    private lateinit var repository: AppNotificationRepositoryImpl

    @Before
    fun setUp() {
        repository = AppNotificationRepositoryImpl(dao)
    }

    @Test
    fun notify_insertsNotificationWithCorrectFields() = runTest {
        val captor = argumentCaptor<AppNotification>()
        whenever(dao.insert(captor.capture())).thenReturn(42L)

        val id = repository.notify(
            type = NotificationType.RULE_ERROR,
            severity = NotificationSeverity.ERROR,
            title = "Boom",
            message = "Webhook timeout",
            actionRoute = "rules/1",
            ruleId = 1L,
            recordId = 99L,
        )

        assertEquals(42L, id)
        val saved = captor.firstValue
        assertEquals(NotificationType.RULE_ERROR.name, saved.type)
        assertEquals(NotificationSeverity.ERROR.name, saved.severity)
        assertEquals("Boom", saved.title)
        assertEquals("Webhook timeout", saved.message)
        assertEquals("rules/1", saved.actionRoute)
        assertEquals(1L, saved.ruleId)
        assertEquals(99L, saved.recordId)
        assertTrue("timestamp should be recent", saved.timestamp > 0)
    }

    @Test
    fun observeAll_delegatesToDao() = runTest {
        val records = listOf(
            AppNotification(
                id = 1, type = "INFO", severity = "INFO",
                title = "T", message = "M", timestamp = 0,
            ),
        )
        whenever(dao.observeAll()).thenReturn(flowOf(records))

        val result = mutableListOf<List<AppNotification>>()
        repository.observeAll().collect { result.add(it) }

        assertEquals(records, result.first())
    }

    @Test
    fun markAsRead_delegatesToDao() = runTest {
        repository.markAsRead(7L)
        verify(dao).markAsRead(7L)
    }

    @Test
    fun markAllAsRead_delegatesToDao() = runTest {
        repository.markAllAsRead()
        verify(dao).markAllAsRead()
    }

    @Test
    fun delete_delegatesToDao() = runTest {
        repository.delete(5L)
        verify(dao).deleteById(5L)
    }

    @Test
    fun deleteAll_delegatesToDao() = runTest {
        repository.deleteAll()
        verify(dao).deleteAll()
    }

    @Test
    fun purgeOlderThan_delegatesToDao() = runTest {
        repository.purgeOlderThan(123L)
        verify(dao).deleteOlderThan(123L)
    }
}
