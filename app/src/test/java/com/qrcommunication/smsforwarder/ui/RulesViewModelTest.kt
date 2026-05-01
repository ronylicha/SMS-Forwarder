package com.qrcommunication.smsforwarder.ui

import com.qrcommunication.smsforwarder.data.local.entity.DestinationType
import com.qrcommunication.smsforwarder.data.local.entity.ForwardingRule
import com.qrcommunication.smsforwarder.data.repository.ForwardingRuleRepository
import com.qrcommunication.smsforwarder.ui.rules.RulesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RulesViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: ForwardingRuleRepository = mock()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun newRule(id: Long, enabled: Boolean = true) = ForwardingRule(
        id = id, name = "n$id", isEnabled = enabled,
        destinationType = DestinationType.SMS.name, destination = "+33600000000",
        createdAt = 0, updatedAt = 0,
    )

    @Test
    fun toggleEnabled_callsRepository() = runTest(dispatcher) {
        whenever(repository.observeAll()).thenReturn(flowOf(emptyList()))
        val vm = RulesViewModel(repository)
        val rule = newRule(1, enabled = true)

        vm.toggleEnabled(rule)
        advanceUntilIdle()

        verify(repository).setEnabled(1L, false)
    }

    @Test
    fun delete_callsRepository() = runTest(dispatcher) {
        whenever(repository.observeAll()).thenReturn(flowOf(emptyList()))
        val vm = RulesViewModel(repository)
        val rule = newRule(2)

        vm.delete(rule)
        advanceUntilIdle()

        verify(repository).delete(rule)
    }
}
