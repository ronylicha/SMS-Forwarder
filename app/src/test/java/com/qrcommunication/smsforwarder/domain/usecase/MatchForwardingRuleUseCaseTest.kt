package com.qrcommunication.smsforwarder.domain.usecase

import com.qrcommunication.smsforwarder.data.local.entity.DestinationType
import com.qrcommunication.smsforwarder.data.local.entity.ForwardingRule
import com.qrcommunication.smsforwarder.data.repository.ForwardingRuleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MatchForwardingRuleUseCaseTest {

    private val repository: ForwardingRuleRepository = mock()
    private lateinit var useCase: MatchForwardingRuleUseCase

    @Before
    fun setUp() {
        useCase = MatchForwardingRuleUseCase(repository)
    }

    private fun rule(
        id: Long,
        priority: Int = 0,
        senderPattern: String? = null,
        keywordPattern: String? = null,
        name: String = "rule$id",
    ): ForwardingRule = ForwardingRule(
        id = id,
        name = name,
        priority = priority,
        senderPattern = senderPattern,
        keywordPattern = keywordPattern,
        destinationType = DestinationType.SMS.name,
        destination = "+33600000000",
        createdAt = 0,
        updatedAt = 0,
    )

    @Test
    fun match_noRules_returnsNull() = runTest {
        whenever(repository.getEnabledOrdered()).thenReturn(emptyList())
        assertNull(useCase("+33612345678", "Hello"))
    }

    @Test
    fun match_ruleWithoutPattern_matchesAnything() = runTest {
        val r = rule(1)
        whenever(repository.getEnabledOrdered()).thenReturn(listOf(r))
        val match = useCase("+33611111111", "random content")
        assertNotNull(match)
        assertEquals(1L, match!!.id)
    }

    @Test
    fun match_senderPattern_matchesByRegex() = runTest {
        val r = rule(1, senderPattern = "^\\+336")
        whenever(repository.getEnabledOrdered()).thenReturn(listOf(r))
        assertNotNull(useCase("+33611111111", "any"))
        assertNull(useCase("+33712345678", "any"))
    }

    @Test
    fun match_keywordPattern_matchesContent() = runTest {
        val r = rule(1, keywordPattern = "code|otp")
        whenever(repository.getEnabledOrdered()).thenReturn(listOf(r))
        assertNotNull(useCase("anyone", "Your verification CODE is 1234"))
        assertNotNull(useCase("anyone", "Your OTP is 9876"))
        assertNull(useCase("anyone", "Hello world"))
    }

    @Test
    fun match_invalidRegex_fallsBackToSubstringMatch() = runTest {
        // "[unclosed" est une regex invalide, doit fallback sur contains
        val r = rule(1, senderPattern = "[unclosed")
        whenever(repository.getEnabledOrdered()).thenReturn(listOf(r))
        assertNotNull(useCase("[unclosed sender", "any"))
        assertNull(useCase("normal sender", "any"))
    }

    @Test
    fun match_higherPriorityWinsFirst() = runTest {
        // Repository retourne déjà ordonné par priorité DESC
        val high = rule(1, priority = 10, name = "high")
        val low = rule(2, priority = 1, name = "low")
        whenever(repository.getEnabledOrdered()).thenReturn(listOf(high, low))
        val match = useCase("any", "any")
        assertEquals("high", match?.name)
    }

    @Test
    fun match_bothPatterns_requireBothToMatch() = runTest {
        val r = rule(1, senderPattern = "service", keywordPattern = "alert")
        whenever(repository.getEnabledOrdered()).thenReturn(listOf(r))
        assertNotNull(useCase("service-x", "an alert from us"))
        assertNull(useCase("service-x", "no match here"))
        assertNull(useCase("user", "alert message"))
    }

    @Test
    fun match_blankPatternIgnored() = runTest {
        val r = rule(1, senderPattern = "  ", keywordPattern = "")
        whenever(repository.getEnabledOrdered()).thenReturn(listOf(r))
        assertNotNull(useCase("anyone", "anything"))
    }
}
