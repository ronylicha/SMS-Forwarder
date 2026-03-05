package com.qrcommunication.smsforwarder.util

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateFormatterTest {

    @Test
    fun formatFull_returnsCorrectFormat() {
        // Use a known timestamp and verify format pattern dd/MM/yyyy HH:mm:ss
        val timestamp = 1709564400000L
        val result = DateFormatter.formatFull(timestamp)
        // Verify format matches dd/MM/yyyy HH:mm:ss pattern
        val pattern = Regex("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}")
        assertTrue("Format should match dd/MM/yyyy HH:mm:ss, got: $result", pattern.matches(result))
    }

    @Test
    fun formatShort_returnsCorrectFormat() {
        val timestamp = 1709564400000L
        val result = DateFormatter.formatShort(timestamp)
        val pattern = Regex("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}")
        assertTrue("Format should match dd/MM/yyyy HH:mm, got: $result", pattern.matches(result))
    }

    @Test
    fun formatForCsv_isoFormat() {
        val timestamp = 1709564400000L
        val result = DateFormatter.formatForCsv(timestamp)
        // Format yyyy-MM-dd HH:mm:ss
        val pattern = Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")
        assertTrue("Format should match yyyy-MM-dd HH:mm:ss, got: $result", pattern.matches(result))
    }

    @Test
    fun formatFull_matchesExpectedValue() {
        val expectedFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE)
        val timestamp = 1709564400000L
        val expected = expectedFormat.format(Date(timestamp))
        assertEquals(expected, DateFormatter.formatFull(timestamp))
    }

    @Test
    fun formatShort_matchesExpectedValue() {
        val expectedFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
        val timestamp = 1709564400000L
        val expected = expectedFormat.format(Date(timestamp))
        assertEquals(expected, DateFormatter.formatShort(timestamp))
    }

    @Test
    fun formatRelative_justNow() {
        val now = System.currentTimeMillis()
        val result = DateFormatter.formatRelative(now)
        assertEquals("A l'instant", result)
    }

    @Test
    fun formatRelative_secondsAgo() {
        val thirtySecondsAgo = System.currentTimeMillis() - 30_000L
        val result = DateFormatter.formatRelative(thirtySecondsAgo)
        assertEquals("A l'instant", result)
    }

    @Test
    fun formatRelative_minutesAgo() {
        val fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000L
        val result = DateFormatter.formatRelative(fiveMinutesAgo)
        assertEquals("Il y a 5min", result)
    }

    @Test
    fun formatRelative_hoursAgo() {
        val threeHoursAgo = System.currentTimeMillis() - 3 * 60 * 60 * 1000L
        val result = DateFormatter.formatRelative(threeHoursAgo)
        assertEquals("Il y a 3h", result)
    }

    @Test
    fun formatRelative_daysAgo() {
        val twoDaysAgo = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L
        val result = DateFormatter.formatRelative(twoDaysAgo)
        assertEquals("Il y a 2j", result)
    }

    @Test
    fun formatRelative_olderThanWeek() {
        val tenDaysAgo = System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L
        val result = DateFormatter.formatRelative(tenDaysAgo)
        // Should fall back to formatShort
        val expected = DateFormatter.formatShort(tenDaysAgo)
        assertEquals(expected, result)
    }

    @Test
    fun isToday_currentTimestamp() {
        val now = System.currentTimeMillis()
        assertTrue(DateFormatter.isToday(now))
    }

    @Test
    fun isToday_yesterday() {
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        // Could still be today if we're near midnight, so use a safe offset
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 12)
        }
        assertFalse(DateFormatter.isToday(cal.timeInMillis))
    }

    @Test
    fun isToday_startOfToday() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 1)
        }
        assertTrue(DateFormatter.isToday(cal.timeInMillis))
    }

    @Test
    fun getStartOfDay_returnsZeroedTime() {
        val now = System.currentTimeMillis()
        val startOfDay = DateFormatter.getStartOfDay(now)
        val cal = Calendar.getInstance().apply { timeInMillis = startOfDay }
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
        assertEquals(0, cal.get(Calendar.MILLISECOND))
    }

    @Test
    fun getEndOfDay_returns235959() {
        val now = System.currentTimeMillis()
        val endOfDay = DateFormatter.getEndOfDay(now)
        val cal = Calendar.getInstance().apply { timeInMillis = endOfDay }
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, cal.get(Calendar.MINUTE))
        assertEquals(59, cal.get(Calendar.SECOND))
        assertEquals(999, cal.get(Calendar.MILLISECOND))
    }
}
