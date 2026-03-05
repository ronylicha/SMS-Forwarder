package com.qrcommunication.smsforwarder.domain.validator

import com.qrcommunication.smsforwarder.data.local.entity.FilterRule
import com.qrcommunication.smsforwarder.data.local.entity.FilterType
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.data.repository.FilterRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FilterEngineTest {

    private val filterRepository: FilterRepository = mock()
    private val preferencesManager: PreferencesManager = mock()
    private lateinit var filterEngine: FilterEngine

    @Before
    fun setUp() {
        filterEngine = FilterEngine(filterRepository, preferencesManager)
    }

    @Test
    fun shouldForward_noFilterActive_returnsTrue() = runTest {
        whenever(preferencesManager.filterMode).thenReturn("NONE")

        val result = filterEngine.shouldForward("+33612345678", "Hello")

        assertTrue(result.shouldForward)
        assertEquals("No filter active", result.reason)
    }

    @Test
    fun shouldForward_whitelistMatch_returnsTrue() = runTest {
        whenever(preferencesManager.filterMode).thenReturn("WHITELIST")
        whenever(filterRepository.getActiveRules()).thenReturn(
            listOf(
                FilterRule(
                    id = 1,
                    pattern = "+33612345678",
                    type = FilterType.WHITELIST.value,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        )

        val result = filterEngine.shouldForward("+33612345678", "Test message")

        assertTrue(result.shouldForward)
        assertEquals("Matches whitelist rule", result.reason)
    }

    @Test
    fun shouldForward_whitelistNoMatch_returnsFalse() = runTest {
        whenever(preferencesManager.filterMode).thenReturn("WHITELIST")
        whenever(filterRepository.getActiveRules()).thenReturn(
            listOf(
                FilterRule(
                    id = 1,
                    pattern = "+33699999999",
                    type = FilterType.WHITELIST.value,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        )

        val result = filterEngine.shouldForward("+33612345678", "Test message")

        assertFalse(result.shouldForward)
        assertEquals("Not in whitelist", result.reason)
    }

    @Test
    fun shouldForward_blacklistMatch_returnsFalse() = runTest {
        whenever(preferencesManager.filterMode).thenReturn("BLACKLIST")
        whenever(filterRepository.getActiveRules()).thenReturn(
            listOf(
                FilterRule(
                    id = 1,
                    pattern = "+33612345678",
                    type = FilterType.BLACKLIST.value,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        )

        val result = filterEngine.shouldForward("+33612345678", "Spam content")

        assertFalse(result.shouldForward)
        assertEquals("Matches blacklist rule", result.reason)
    }

    @Test
    fun shouldForward_blacklistNoMatch_returnsTrue() = runTest {
        whenever(preferencesManager.filterMode).thenReturn("BLACKLIST")
        whenever(filterRepository.getActiveRules()).thenReturn(
            listOf(
                FilterRule(
                    id = 1,
                    pattern = "+33699999999",
                    type = FilterType.BLACKLIST.value,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        )

        val result = filterEngine.shouldForward("+33612345678", "Normal message")

        assertTrue(result.shouldForward)
        assertEquals("Not in blacklist", result.reason)
    }

    @Test
    fun shouldForward_keywordMatch_inContent() = runTest {
        whenever(preferencesManager.filterMode).thenReturn("BLACKLIST")
        whenever(filterRepository.getActiveRules()).thenReturn(
            listOf(
                FilterRule(
                    id = 1,
                    pattern = "SPAM",
                    type = FilterType.BLACKLIST.value,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        )

        val result = filterEngine.shouldForward("+33612345678", "This is SPAM content")

        assertFalse(result.shouldForward)
        assertEquals("Matches blacklist rule", result.reason)
    }

    @Test
    fun shouldForward_keywordMatch_caseInsensitive() = runTest {
        whenever(preferencesManager.filterMode).thenReturn("BLACKLIST")
        whenever(filterRepository.getActiveRules()).thenReturn(
            listOf(
                FilterRule(
                    id = 1,
                    pattern = "spam",
                    type = FilterType.BLACKLIST.value,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        )

        val result = filterEngine.shouldForward("+33612345678", "This is SPAM content")

        assertFalse(result.shouldForward)
        assertEquals("Matches blacklist rule", result.reason)
    }

    @Test
    fun shouldForward_noActiveRules_returnsTrue() = runTest {
        whenever(preferencesManager.filterMode).thenReturn("WHITELIST")
        whenever(filterRepository.getActiveRules()).thenReturn(emptyList())

        val result = filterEngine.shouldForward("+33612345678", "Test")

        assertTrue(result.shouldForward)
        assertEquals("No active rules", result.reason)
    }

    @Test
    fun shouldForward_localNumberNormalized() = runTest {
        whenever(preferencesManager.filterMode).thenReturn("WHITELIST")
        whenever(filterRepository.getActiveRules()).thenReturn(
            listOf(
                FilterRule(
                    id = 1,
                    pattern = "0612345678",
                    type = FilterType.WHITELIST.value,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        )

        // Sender in E.164 format should match local format pattern after normalization
        val result = filterEngine.shouldForward("+33612345678", "Hello")

        assertTrue(result.shouldForward)
        assertEquals("Matches whitelist rule", result.reason)
    }

    @Test
    fun shouldForward_whitelistKeyword_inContent() = runTest {
        whenever(preferencesManager.filterMode).thenReturn("WHITELIST")
        whenever(filterRepository.getActiveRules()).thenReturn(
            listOf(
                FilterRule(
                    id = 1,
                    pattern = "urgent",
                    type = FilterType.WHITELIST.value,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        )

        val result = filterEngine.shouldForward("+33699999999", "This is an urgent message")

        assertTrue(result.shouldForward)
        assertEquals("Matches whitelist rule", result.reason)
    }

    @Test
    fun shouldForward_unknownFilterMode_defaultsToNone() = runTest {
        whenever(preferencesManager.filterMode).thenReturn("INVALID_MODE")

        val result = filterEngine.shouldForward("+33612345678", "Hello")

        assertTrue(result.shouldForward)
        assertEquals("No filter active", result.reason)
    }
}
