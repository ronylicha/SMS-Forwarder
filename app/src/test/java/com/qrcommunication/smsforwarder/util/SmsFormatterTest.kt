package com.qrcommunication.smsforwarder.util

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsFormatterTest {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)

    // --- formatForwardedSms ---

    @Test
    fun formatForwardedSms_simpleMessage() {
        val timestamp = 1709564400000L // fixed timestamp
        val expectedDate = dateFormat.format(Date(timestamp))
        val result = SmsFormatter.formatForwardedSms("+33612345678", timestamp, "Bonjour")
        assertEquals("[De: +33612345678 | $expectedDate] Bonjour", result)
    }

    @Test
    fun formatForwardedSms_emptyContent() {
        val timestamp = 1709564400000L
        val expectedDate = dateFormat.format(Date(timestamp))
        val result = SmsFormatter.formatForwardedSms("+33612345678", timestamp, "")
        assertEquals("[De: +33612345678 | $expectedDate] ", result)
    }

    @Test
    fun formatForwardedSms_specialCharacters() {
        val timestamp = 1709564400000L
        val expectedDate = dateFormat.format(Date(timestamp))
        val content = "Bonjour! C'est un test avec des accents: e, a, u et des emojis"
        val result = SmsFormatter.formatForwardedSms("+33612345678", timestamp, content)
        assertEquals("[De: +33612345678 | $expectedDate] $content", result)
    }

    // --- getPreview ---

    @Test
    fun getPreview_shortMessage() {
        val shortMessage = "Hello world"
        val result = SmsFormatter.getPreview(shortMessage)
        assertEquals(shortMessage, result)
    }

    @Test
    fun getPreview_longMessage() {
        val longMessage = "A".repeat(100)
        val result = SmsFormatter.getPreview(longMessage)
        assertEquals("A".repeat(50) + "...", result)
    }

    @Test
    fun getPreview_exactlyMaxLength() {
        val exactMessage = "B".repeat(50)
        val result = SmsFormatter.getPreview(exactMessage)
        assertEquals(exactMessage, result)
    }

    @Test
    fun getPreview_customLength() {
        val message = "Hello world, this is a longer message"
        val result = SmsFormatter.getPreview(message, 11)
        assertEquals("Hello world...", result)
    }

    @Test
    fun getPreview_customLength_messageWithinLimit() {
        val message = "Short"
        val result = SmsFormatter.getPreview(message, 10)
        assertEquals("Short", result)
    }

    // --- estimatePartCount ---

    @Test
    fun estimatePartCount_singlePart() {
        val message = "A".repeat(160)
        assertEquals(1, SmsFormatter.estimatePartCount(message))
    }

    @Test
    fun estimatePartCount_shortMessage() {
        val message = "Hello"
        assertEquals(1, SmsFormatter.estimatePartCount(message))
    }

    @Test
    fun estimatePartCount_multiPart() {
        // 306 chars -> ceil(306/153) = 2
        val message = "A".repeat(306)
        assertEquals(2, SmsFormatter.estimatePartCount(message))
    }

    @Test
    fun estimatePartCount_threeParts() {
        // 460 chars -> ceil(460/153) = 4 (153*3=459, so 460 needs 4 parts... let's use 459)
        val message = "A".repeat(459)
        assertEquals(3, SmsFormatter.estimatePartCount(message))
    }

    @Test
    fun estimatePartCount_exactBoundary_160() {
        val message = "A".repeat(160)
        assertEquals(1, SmsFormatter.estimatePartCount(message))
    }

    @Test
    fun estimatePartCount_exactBoundary_161() {
        // 161 chars -> ceil(161/153) = 2
        val message = "A".repeat(161)
        assertEquals(2, SmsFormatter.estimatePartCount(message))
    }

    @Test
    fun estimatePartCount_emptyMessage() {
        assertEquals(1, SmsFormatter.estimatePartCount(""))
    }
}
