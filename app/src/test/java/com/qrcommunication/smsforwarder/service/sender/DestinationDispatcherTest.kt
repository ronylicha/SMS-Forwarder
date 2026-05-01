package com.qrcommunication.smsforwarder.service.sender

import com.qrcommunication.smsforwarder.data.local.entity.DestinationType
import com.qrcommunication.smsforwarder.service.SmsSender
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.junit.Assert.assertTrue

class DestinationDispatcherTest {

    private val smsSender: SmsSender = mock()
    private val webhookSender: WebhookSender = mock()
    private lateinit var dispatcher: DestinationDispatcher

    @Before
    fun setUp() {
        dispatcher = DestinationDispatcher(smsSender, webhookSender)
    }

    private val payload = MessagePayload(
        sender = "+33612345678",
        content = "Hello",
        receivedAt = 1709564400000L,
    )

    @Test
    fun dispatch_sms_callsSmsSenderWithFormattedMessage() = runTest {
        dispatcher.dispatch(DestinationType.SMS, "+33699999999", payload)

        val captor = argumentCaptor<String>()
        verify(smsSender).sendSms(eq("+33699999999"), captor.capture())
        // Le message formaté doit contenir le sender et le content
        val formatted = captor.firstValue
        assertTrue(formatted.contains("+33612345678"))
        assertTrue(formatted.contains("Hello"))
        verifyNoInteractions(webhookSender)
    }

    @Test
    fun dispatch_smsWithAppLabel_usesNotifPrefix() = runTest {
        val notifPayload = payload.copy(sourceLabel = "WhatsApp")
        dispatcher.dispatch(DestinationType.SMS, "+33699999999", notifPayload)

        val captor = argumentCaptor<String>()
        verify(smsSender).sendSms(eq("+33699999999"), captor.capture())
        assertTrue(captor.firstValue.contains("Notif WhatsApp"))
    }

    @Test
    fun dispatch_webhook_callsWebhookSender() = runTest {
        val url = "https://example.com/webhook"
        dispatcher.dispatch(DestinationType.WEBHOOK, url, payload)

        verify(webhookSender).send(eq(url), eq(payload))
        verifyNoInteractions(smsSender)
    }
}
