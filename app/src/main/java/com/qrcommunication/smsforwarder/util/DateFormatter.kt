package com.qrcommunication.smsforwarder.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateFormatter {
    private val fullFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE)
    private val shortFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
    private val timeOnlyFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    private val csvFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE)

    fun formatFull(timestampMs: Long): String = fullFormat.format(Date(timestampMs))
    fun formatShort(timestampMs: Long): String = shortFormat.format(Date(timestampMs))
    fun formatTimeOnly(timestampMs: Long): String = timeOnlyFormat.format(Date(timestampMs))
    fun formatDateOnly(timestampMs: Long): String = dateOnlyFormat.format(Date(timestampMs))
    fun formatForCsv(timestampMs: Long): String = csvFormat.format(Date(timestampMs))

    fun formatRelative(timestampMs: Long): String {
        val now = System.currentTimeMillis()
        val diffMs = now - timestampMs
        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
        val diffHours = TimeUnit.MILLISECONDS.toHours(diffMs)
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)

        return when {
            diffMinutes < 1 -> "A l'instant"
            diffMinutes < 60 -> "Il y a ${diffMinutes}min"
            diffHours < 24 -> "Il y a ${diffHours}h"
            diffDays < 7 -> "Il y a ${diffDays}j"
            else -> formatShort(timestampMs)
        }
    }

    fun isToday(timestampMs: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestampMs }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun getStartOfDay(timestampMs: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestampMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getEndOfDay(timestampMs: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestampMs
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }
}
