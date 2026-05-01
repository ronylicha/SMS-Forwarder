package com.qrcommunication.smsforwarder.data.repository

import com.qrcommunication.smsforwarder.data.local.dao.AppNotificationDao
import com.qrcommunication.smsforwarder.data.local.entity.AppNotification
import com.qrcommunication.smsforwarder.data.local.entity.NotificationSeverity
import com.qrcommunication.smsforwarder.data.local.entity.NotificationType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationRepositoryImpl @Inject constructor(
    private val dao: AppNotificationDao,
) : AppNotificationRepository {

    override fun observeAll(): Flow<List<AppNotification>> = dao.observeAll()
    override fun observeUnread(): Flow<List<AppNotification>> = dao.observeUnread()
    override fun observeUnreadCount(): Flow<Int> = dao.observeUnreadCount()

    override suspend fun notify(
        type: NotificationType,
        severity: NotificationSeverity,
        title: String,
        message: String,
        actionRoute: String?,
        ruleId: Long?,
        recordId: Long?,
    ): Long = dao.insert(
        AppNotification(
            type = type.name,
            severity = severity.name,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            actionRoute = actionRoute,
            ruleId = ruleId,
            recordId = recordId,
        ),
    )

    override suspend fun markAsRead(id: Long) = dao.markAsRead(id)
    override suspend fun markAllAsRead() = dao.markAllAsRead()
    override suspend fun delete(id: Long) = dao.deleteById(id)
    override suspend fun deleteAll() = dao.deleteAll()

    override suspend fun purgeOlderThan(olderThanMs: Long) = dao.deleteOlderThan(olderThanMs)
}
