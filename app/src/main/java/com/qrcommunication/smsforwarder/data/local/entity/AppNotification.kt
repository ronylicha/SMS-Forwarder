package com.qrcommunication.smsforwarder.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class NotificationType {
    RULE_ERROR,
    DESTINATION_UNREACHABLE,
    QUOTA_WARNING,
    RETRY_EXHAUSTED,
    PERMISSION_REVOKED,
    BATTERY_OPTIMIZATION,
    INFO,
}

enum class NotificationSeverity { INFO, WARNING, ERROR }

@Entity(
    tableName = "app_notifications",
    indices = [Index(value = ["is_read", "timestamp"])],
)
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "severity")
    val severity: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "action_route")
    val actionRoute: String? = null,

    @ColumnInfo(name = "rule_id")
    val ruleId: Long? = null,

    @ColumnInfo(name = "record_id")
    val recordId: Long? = null,
)
