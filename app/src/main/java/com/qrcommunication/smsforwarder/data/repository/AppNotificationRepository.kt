package com.qrcommunication.smsforwarder.data.repository

import com.qrcommunication.smsforwarder.data.local.entity.AppNotification
import com.qrcommunication.smsforwarder.data.local.entity.NotificationSeverity
import com.qrcommunication.smsforwarder.data.local.entity.NotificationType
import kotlinx.coroutines.flow.Flow

interface AppNotificationRepository {
    fun observeAll(): Flow<List<AppNotification>>
    fun observeUnread(): Flow<List<AppNotification>>
    fun observeUnreadCount(): Flow<Int>

    suspend fun notify(
        type: NotificationType,
        severity: NotificationSeverity,
        title: String,
        message: String,
        actionRoute: String? = null,
        ruleId: Long? = null,
        recordId: Long? = null,
    ): Long

    suspend fun markAsRead(id: Long)
    suspend fun markAllAsRead()
    suspend fun delete(id: Long)
    suspend fun deleteAll()

    /** Purge les notifications anciennes pour eviter une croissance illimitee. */
    suspend fun purgeOlderThan(olderThanMs: Long)
}
