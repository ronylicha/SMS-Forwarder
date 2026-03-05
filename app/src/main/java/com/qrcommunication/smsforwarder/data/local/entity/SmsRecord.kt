package com.qrcommunication.smsforwarder.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SmsStatus(val value: String) {
    PENDING("PENDING"),
    SENT("SENT"),
    FAILED("FAILED"),
    FILTERED("FILTERED");

    companion object {
        fun fromValue(value: String): SmsStatus {
            return entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown SmsStatus: $value")
        }
    }
}

@Entity(tableName = "sms_records")
data class SmsRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "sender")
    val sender: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "received_at")
    val receivedAt: Long,

    @ColumnInfo(name = "forwarded_at")
    val forwardedAt: Long? = null,

    @ColumnInfo(name = "status")
    val status: String = SmsStatus.PENDING.value,

    @ColumnInfo(name = "destination")
    val destination: String,

    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0
)
