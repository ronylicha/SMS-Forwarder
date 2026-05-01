package com.qrcommunication.smsforwarder.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RetryPolicyTest {

    @Test
    fun delayFor_attempt0_returnsInitialDelay() {
        val policy = RetryPolicy(initialDelayMs = 1_000L, backoffMultiplier = 2.0, maxDelayMs = 60_000L)
        assertEquals(1_000L, policy.delayFor(0))
    }

    @Test
    fun delayFor_attempt1_appliesBackoffOnce() {
        val policy = RetryPolicy(initialDelayMs = 1_000L, backoffMultiplier = 2.0, maxDelayMs = 60_000L)
        assertEquals(2_000L, policy.delayFor(1))
    }

    @Test
    fun delayFor_attempt3_appliesBackoffThreeTimes() {
        val policy = RetryPolicy(initialDelayMs = 1_000L, backoffMultiplier = 2.0, maxDelayMs = 60_000L)
        assertEquals(8_000L, policy.delayFor(3))
    }

    @Test
    fun delayFor_clampsToMaxDelay() {
        val policy = RetryPolicy(initialDelayMs = 1_000L, backoffMultiplier = 10.0, maxDelayMs = 5_000L)
        assertEquals(5_000L, policy.delayFor(5))
    }

    @Test
    fun delayFor_negativeAttempt_returnsZero() {
        val policy = RetryPolicy()
        assertEquals(0L, policy.delayFor(-1))
    }

    @Test
    fun shouldRetry_belowMax_true() {
        val policy = RetryPolicy(maxAttempts = 3)
        assertTrue(policy.shouldRetry(0))
        assertTrue(policy.shouldRetry(1))
        assertTrue(policy.shouldRetry(2))
    }

    @Test
    fun shouldRetry_atOrAboveMax_false() {
        val policy = RetryPolicy(maxAttempts = 3)
        assertFalse(policy.shouldRetry(3))
        assertFalse(policy.shouldRetry(10))
    }

    @Test
    fun defaultPolicy_hasReasonableValues() {
        val policy = RetryPolicy()
        assertEquals(RetryPolicy.DEFAULT_MAX_ATTEMPTS, policy.maxAttempts)
        assertEquals(RetryPolicy.DEFAULT_INITIAL_DELAY_MS, policy.initialDelayMs)
        assertEquals(RetryPolicy.DEFAULT_BACKOFF, policy.backoffMultiplier, 0.001)
        assertEquals(RetryPolicy.DEFAULT_MAX_DELAY_MS, policy.maxDelayMs)
    }
}
