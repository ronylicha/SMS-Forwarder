package com.qrcommunication.smsforwarder.service.sender

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Envoie un message via HTTP POST JSON vers une URL webhook.
 * Utilise HttpURLConnection (pas de dependance externe).
 */
@Singleton
class WebhookSender @Inject constructor() {

    companion object {
        private const val TAG = "WebhookSender"
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 15_000
        private const val USER_AGENT = "SMSForwarder-Android/1.0"
    }

    /**
     * @throws WebhookException si HTTP code >= 400 ou erreur reseau.
     */
    suspend fun send(url: String, payload: MessagePayload) = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("sender", payload.sender)
            put("content", payload.content)
            put("receivedAt", payload.receivedAt)
            payload.sourceLabel?.let { put("sourceLabel", it) }
            payload.originalDestination?.let { put("originalDestination", it) }
        }.toString()

        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept", "application/json")
        }

        try {
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            Log.d(TAG, "POST $url -> $code")
            if (code >= 400) {
                val errorBody = runCatching {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }
                }.getOrNull().orEmpty()
                throw WebhookException("HTTP $code: ${errorBody.take(200)}")
            }
        } catch (e: WebhookException) {
            throw e
        } catch (e: Exception) {
            throw WebhookException("Network error: ${e.message ?: e.javaClass.simpleName}", e)
        } finally {
            connection.disconnect()
        }
    }
}

class WebhookException(message: String, cause: Throwable? = null) : Exception(message, cause)
