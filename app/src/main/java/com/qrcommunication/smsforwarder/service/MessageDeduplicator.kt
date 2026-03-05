package com.qrcommunication.smsforwarder.service

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deduplication des messages captures par plusieurs sources (SmsReceiver, ContentObserver, NotificationListener).
 * Utilise un hash sender+contenu+timestamp (arrondi a 5s) pour identifier les doublons.
 */
@Singleton
class MessageDeduplicator @Inject constructor() {

    companion object {
        private const val TAG = "MessageDeduplicator"
        private const val TIMESTAMP_WINDOW_MS = 5000L
        private const val MAX_CACHE_SIZE = 500
        private const val EXPIRY_MS = 60_000L
    }

    private val processedMessages = ConcurrentHashMap<String, Long>()

    /**
     * Retourne true si le message n'a PAS encore ete traite (donc doit etre forwarde).
     * Retourne false si c'est un doublon.
     */
    fun shouldProcess(sender: String, content: String, timestampMs: Long): Boolean {
        cleanup()
        val hash = generateHash(sender, content, timestampMs)
        val now = System.currentTimeMillis()

        val existing = processedMessages.putIfAbsent(hash, now)
        if (existing != null) {
            Log.d(TAG, "Duplicate message detected from $sender, skipping")
            return false
        }

        Log.d(TAG, "New message from $sender, will process")
        return true
    }

    private fun generateHash(sender: String, content: String, timestampMs: Long): String {
        val roundedTimestamp = (timestampMs / TIMESTAMP_WINDOW_MS) * TIMESTAMP_WINDOW_MS
        val raw = "$sender|${content.take(100)}|$roundedTimestamp"
        return raw.hashCode().toString()
    }

    private fun cleanup() {
        if (processedMessages.size > MAX_CACHE_SIZE) {
            val now = System.currentTimeMillis()
            processedMessages.entries.removeIf { now - it.value > EXPIRY_MS }
        }
    }
}
