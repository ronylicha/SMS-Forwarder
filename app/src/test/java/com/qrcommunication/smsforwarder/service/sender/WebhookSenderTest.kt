package com.qrcommunication.smsforwarder.service.sender

import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Mini-serveur HTTP plain socket pour tester WebhookSender sans dependance externe
 * (com.sun.net.httpserver n'est pas dispo cote Android).
 */
private class TestHttpServer {

    private val executor = Executors.newSingleThreadExecutor()
    private val workerPool = Executors.newCachedThreadPool()
    private lateinit var serverSocket: ServerSocket
    @Volatile private var running = false

    val captured: ConcurrentLinkedQueue<CapturedRequest> = ConcurrentLinkedQueue()
    @Volatile var responseCode: Int = 200
    @Volatile var responseBody: String = "ok"

    val port: Int get() = serverSocket.localPort

    fun start() {
        serverSocket = ServerSocket(0)
        running = true
        executor.submit { acceptLoop() }
    }

    fun stop() {
        running = false
        runCatching { serverSocket.close() }
        executor.shutdownNow()
        workerPool.shutdownNow()
        executor.awaitTermination(2, TimeUnit.SECONDS)
        workerPool.awaitTermination(2, TimeUnit.SECONDS)
    }

    private fun acceptLoop() {
        while (running) {
            val socket = runCatching { serverSocket.accept() }.getOrNull() ?: return
            workerPool.submit { handle(socket) }
        }
    }

    private fun handle(socket: Socket) {
        socket.use { s ->
            val reader = s.getInputStream().bufferedReader()
            val statusLine = reader.readLine() ?: return
            val parts = statusLine.split(" ")
            val method = parts.getOrNull(0) ?: ""
            val headers = mutableMapOf<String, String>()
            var contentLength = 0
            while (true) {
                val line = reader.readLine() ?: break
                if (line.isEmpty()) break
                val idx = line.indexOf(':')
                if (idx > 0) {
                    val key = line.substring(0, idx).trim()
                    val value = line.substring(idx + 1).trim()
                    headers[key.lowercase()] = value
                    if (key.equals("Content-Length", ignoreCase = true)) {
                        contentLength = value.toIntOrNull() ?: 0
                    }
                }
            }
            val bodyChars = CharArray(contentLength)
            if (contentLength > 0) reader.read(bodyChars, 0, contentLength)
            captured.add(
                CapturedRequest(
                    method = method,
                    contentType = headers["content-type"],
                    userAgent = headers["user-agent"],
                    body = String(bodyChars),
                ),
            )

            val payloadBytes = responseBody.toByteArray()
            val response = buildString {
                append("HTTP/1.1 $responseCode ${statusText(responseCode)}\r\n")
                append("Content-Type: text/plain\r\n")
                append("Content-Length: ${payloadBytes.size}\r\n")
                append("Connection: close\r\n")
                append("\r\n")
            }
            s.getOutputStream().apply {
                write(response.toByteArray())
                write(payloadBytes)
                flush()
            }
        }
    }

    private fun statusText(code: Int): String = when (code) {
        200 -> "OK"
        404 -> "Not Found"
        500 -> "Internal Server Error"
        else -> "Status"
    }
}

private data class CapturedRequest(
    val method: String,
    val contentType: String?,
    val userAgent: String?,
    val body: String,
)

class WebhookSenderTest {

    private lateinit var server: TestHttpServer
    private lateinit var sender: WebhookSender

    private val payload = MessagePayload(
        sender = "+33612345678",
        content = "Hello world",
        receivedAt = 1709564400000L,
        sourceLabel = "WhatsApp",
    )

    @Before
    fun setUp() {
        server = TestHttpServer().also { it.start() }
        sender = WebhookSender()
    }

    @After
    fun tearDown() {
        server.stop()
    }

    private fun url(): String = "http://127.0.0.1:${server.port}/hook"

    @Test
    fun send_postsJsonWithExpectedHeaders() = runTest {
        sender.send(url(), payload)
        val req = server.captured.poll() ?: fail("no request captured")
        req as CapturedRequest
        assertEquals("POST", req.method)
        assertTrue(req.contentType!!.contains("application/json"))
        assertTrue(req.userAgent!!.startsWith("SMSForwarder-Android"))
    }

    @Test
    fun send_serializesPayloadFields() = runTest {
        sender.send(url(), payload)
        val req = server.captured.poll()!!
        val body = JSONObject(req.body)
        assertEquals("+33612345678", body.getString("sender"))
        assertEquals("Hello world", body.getString("content"))
        assertEquals(1709564400000L, body.getLong("receivedAt"))
        assertEquals("WhatsApp", body.getString("sourceLabel"))
    }

    @Test
    fun send_omitsNullableFieldsWhenAbsent() = runTest {
        val minimal = MessagePayload(sender = "+331", content = "x", receivedAt = 0L)
        sender.send(url(), minimal)
        val body = JSONObject(server.captured.poll()!!.body)
        assertTrue(!body.has("sourceLabel"))
        assertTrue(!body.has("originalDestination"))
    }

    @Test
    fun send_4xx_throwsWebhookExceptionWithCode() = runTest {
        server.responseCode = 404
        server.responseBody = "not found"
        try {
            sender.send(url(), payload)
            fail("expected WebhookException")
        } catch (e: WebhookException) {
            assertTrue(e.message!!.contains("404"))
        }
    }

    @Test
    fun send_5xx_throwsWebhookException() = runTest {
        server.responseCode = 500
        try {
            sender.send(url(), payload)
            fail("expected WebhookException")
        } catch (e: WebhookException) {
            assertTrue(e.message!!.contains("500"))
        }
    }

    @Test
    fun send_unreachableHost_throwsWebhookException() = runTest {
        try {
            sender.send("http://127.0.0.1:1/dead", payload)
            fail("expected WebhookException")
        } catch (e: WebhookException) {
            assertTrue("should mention error", e.message!!.isNotBlank())
        }
    }
}
