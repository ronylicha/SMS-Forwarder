package com.qrcommunication.smsforwarder.data.repository

import com.qrcommunication.smsforwarder.data.local.dao.ForwardingRuleDao
import com.qrcommunication.smsforwarder.data.local.entity.DestinationType
import com.qrcommunication.smsforwarder.data.local.entity.ForwardingRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ForwardingRuleRepositoryImplTest {

    private val dao: ForwardingRuleDao = mock()
    private lateinit var repository: ForwardingRuleRepositoryImpl

    @Before
    fun setUp() {
        repository = ForwardingRuleRepositoryImpl(dao)
    }

    private fun newRule(id: Long = 0, name: String = "rule"): ForwardingRule = ForwardingRule(
        id = id,
        name = name,
        destinationType = DestinationType.SMS.name,
        destination = "+33600000000",
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun upsert_newRule_insertsWithTimestamps() = runTest {
        val captor = argumentCaptor<ForwardingRule>()
        whenever(dao.insert(captor.capture())).thenReturn(123L)

        val id = repository.upsert(newRule(id = 0, name = "n"))

        assertEquals(123L, id)
        val inserted = captor.firstValue
        assertTrue("createdAt should be set", inserted.createdAt > 0)
        assertTrue("updatedAt should be set", inserted.updatedAt > 0)
        assertEquals("n", inserted.name)
    }

    @Test
    fun upsert_existingRule_updatesWithFreshTimestamp() = runTest {
        val original = newRule(id = 5, name = "old").copy(createdAt = 100L)
        val captor = argumentCaptor<ForwardingRule>()

        val id = repository.upsert(original)

        verify(dao).update(captor.capture())
        assertEquals(5L, id)
        val updated = captor.firstValue
        assertEquals(100L, updated.createdAt)
        assertTrue("updatedAt refreshed", updated.updatedAt > 0)
    }

    @Test
    fun setEnabled_passesTimestamp() = runTest {
        repository.setEnabled(7L, false)
        val tsCaptor = argumentCaptor<Long>()
        verify(dao).setEnabled(eq(7L), eq(false), tsCaptor.capture())
        assertTrue(tsCaptor.firstValue > 0)
    }

    @Test
    fun recordSuccess_callsDaoWithTimestamp() = runTest {
        repository.recordSuccess(2L)
        val tsCaptor = argumentCaptor<Long>()
        verify(dao).recordSuccess(eq(2L), tsCaptor.capture())
        assertTrue(tsCaptor.firstValue > 0)
    }

    @Test
    fun recordFailure_passesError() = runTest {
        repository.recordFailure(3L, "boom")
        val tsCaptor = argumentCaptor<Long>()
        verify(dao).recordFailure(eq(3L), tsCaptor.capture(), eq("boom"))
        assertTrue(tsCaptor.firstValue > 0)
    }
}
