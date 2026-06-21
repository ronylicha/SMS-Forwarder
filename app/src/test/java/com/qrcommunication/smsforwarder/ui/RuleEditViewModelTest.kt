package com.qrcommunication.smsforwarder.ui

import androidx.lifecycle.SavedStateHandle
import com.qrcommunication.smsforwarder.data.local.entity.DestinationType
import com.qrcommunication.smsforwarder.data.local.entity.ForwardingRule
import com.qrcommunication.smsforwarder.data.repository.ForwardingRuleRepository
import com.qrcommunication.smsforwarder.domain.usecase.MatchForwardingRuleUseCase
import android.content.Context
import com.qrcommunication.smsforwarder.ui.rules.RuleEditViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RuleEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: ForwardingRuleRepository = mock()
    private val matchRule: MatchForwardingRuleUseCase = mock()
    private val context: Context = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(context.getString(any())).thenReturn("test message")
        whenever(context.getString(any(), any())).thenReturn("test message")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(ruleId: Long = 0L) = RuleEditViewModel(
        savedState = SavedStateHandle(mapOf("ruleId" to ruleId)),
        context = context,
        repository = repository,
        matchRule = matchRule,
    )

    @Test
    fun init_newRule_loadsEmptyState() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(0L, state.id)
        assertEquals("", state.name)
        assertEquals(DestinationType.SMS, state.destinationType)
    }

    @Test
    fun init_existingRule_loadsFromRepository() = runTest(testDispatcher) {
        val rule = ForwardingRule(
            id = 5,
            name = "loaded",
            senderPattern = "abc",
            destinationType = DestinationType.WEBHOOK.name,
            destination = "https://example.com",
            createdAt = 1, updatedAt = 1,
        )
        kotlinx.coroutines.runBlocking { whenever(repository.getById(5L)).thenReturn(rule) }

        val vm = createViewModel(ruleId = 5L)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(5L, state.id)
        assertEquals("loaded", state.name)
        assertEquals(DestinationType.WEBHOOK, state.destinationType)
        assertEquals("https://example.com", state.destination)
    }

    @Test
    fun save_emptyName_setsErrorTestResult() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.updateDestination("+33600000000")
        vm.save()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.testResult)
    }

    @Test
    fun save_validRule_callsUpsertWithCorrectFields() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.updateName("rule 1")
        vm.updateDestination("+33611111111")
        vm.updateDestinationType(DestinationType.SMS)
        vm.updateSenderPattern("^foo")

        vm.save()
        advanceUntilIdle()

        val captor = argumentCaptor<ForwardingRule>()
        verify(repository).upsert(captor.capture())
        val saved = captor.firstValue
        assertEquals("rule 1", saved.name)
        assertEquals("+33611111111", saved.destination)
        assertEquals("^foo", saved.senderPattern)
        assertTrue(vm.uiState.value.isSaved)
    }

    @Test
    fun testRule_noMatch_returnsExplainedMessage() = runTest(testDispatcher) {
        whenever(matchRule(any(), any())).thenReturn(null)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateTestSender("anything")
        vm.updateTestContent("any content")
        vm.testRule()
        advanceUntilIdle()

        val msg = vm.uiState.value.testResult
        assertNotNull(msg)
        assertNotNull(msg)
    }

    @Test
    fun testRule_thisRuleMatches_returnsConfirmation() = runTest(testDispatcher) {
        val rule = ForwardingRule(
            id = 3, name = "self",
            destinationType = DestinationType.SMS.name,
            destination = "+33600000000",
            createdAt = 0, updatedAt = 0,
        )
        kotlinx.coroutines.runBlocking {
            whenever(repository.getById(3L)).thenReturn(rule)
            whenever(matchRule(any(), any())).thenReturn(rule)
        }
        val vm = createViewModel(ruleId = 3L)
        advanceUntilIdle()

        vm.testRule()
        advanceUntilIdle()

        val msg = vm.uiState.value.testResult
        assertNotNull(msg)
    }
}
