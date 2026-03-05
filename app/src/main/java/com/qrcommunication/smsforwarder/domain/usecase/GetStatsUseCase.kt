package com.qrcommunication.smsforwarder.domain.usecase

import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.util.DateFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

data class SmsStats(
    val totalCount: Int,
    val sentCount: Int,
    val failedCount: Int,
    val filteredCount: Int,
    val pendingCount: Int,
    val successRate: Float // percentage 0-100
)

data class DailyStats(
    val date: String, // dd/MM/yyyy
    val dateMs: Long,
    val received: Int,
    val forwarded: Int,
    val failed: Int
)

class GetStatsUseCase @Inject constructor(
    private val smsRepository: SmsRepository
) {
    fun getOverallStats(): Flow<SmsStats> {
        return combine(
            smsRepository.getRecordCount(),
            smsRepository.getRecordCountByStatus(SmsStatus.SENT),
            smsRepository.getRecordCountByStatus(SmsStatus.FAILED),
            smsRepository.getRecordCountByStatus(SmsStatus.FILTERED),
            smsRepository.getRecordCountByStatus(SmsStatus.PENDING)
        ) { total, sent, failed, filtered, pending ->
            val successRate = if (total > 0) (sent.toFloat() / total * 100) else 0f
            SmsStats(
                totalCount = total,
                sentCount = sent,
                failedCount = failed,
                filteredCount = filtered,
                pendingCount = pending,
                successRate = successRate
            )
        }
    }

    suspend fun getDailyStats(days: Int = 7): List<DailyStats> {
        val calendar = Calendar.getInstance()
        val result = mutableListOf<DailyStats>()

        for (i in days - 1 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)

            val startMs = DateFormatter.getStartOfDay(calendar.timeInMillis)
            val endMs = DateFormatter.getEndOfDay(calendar.timeInMillis)
            val dateStr = DateFormatter.formatDateOnly(calendar.timeInMillis)

            val records = smsRepository.getRecordsForDateRange(startMs, endMs)
            val received = records.size
            val forwarded = records.count { it.status == SmsStatus.SENT.value }
            val failed = records.count { it.status == SmsStatus.FAILED.value }

            result.add(DailyStats(dateStr, startMs, received, forwarded, failed))
        }

        return result
    }
}
