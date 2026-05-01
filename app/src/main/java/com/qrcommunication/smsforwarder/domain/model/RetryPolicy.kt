package com.qrcommunication.smsforwarder.domain.model

import kotlin.math.min
import kotlin.math.pow

/**
 * Politique de retry configurable par l'utilisateur.
 * Backoff exponentiel : delay(n) = min(initial * multiplier^n, max).
 */
data class RetryPolicy(
    val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
    val initialDelayMs: Long = DEFAULT_INITIAL_DELAY_MS,
    val backoffMultiplier: Double = DEFAULT_BACKOFF,
    val maxDelayMs: Long = DEFAULT_MAX_DELAY_MS,
) {
    /** Delay avant la tentative numero `attempt` (0-indexed : 0 = premiere retry). */
    fun delayFor(attempt: Int): Long {
        if (attempt < 0) return 0L
        val computed = (initialDelayMs * backoffMultiplier.pow(attempt)).toLong()
        return min(computed, maxDelayMs)
    }

    fun shouldRetry(currentAttempt: Int): Boolean = currentAttempt < maxAttempts

    companion object {
        const val DEFAULT_MAX_ATTEMPTS = 3
        const val DEFAULT_INITIAL_DELAY_MS = 60_000L
        const val DEFAULT_BACKOFF = 2.0
        const val DEFAULT_MAX_DELAY_MS = 30 * 60_000L
    }
}
