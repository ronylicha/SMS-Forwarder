package com.qrcommunication.smsforwarder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.qrcommunication.smsforwarder.data.local.entity.AppNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface AppNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: AppNotification): Long

    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<AppNotification>>

    @Query("SELECT * FROM app_notifications WHERE is_read = 0 ORDER BY timestamp DESC")
    fun observeUnread(): Flow<List<AppNotification>>

    @Query("SELECT COUNT(*) FROM app_notifications WHERE is_read = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("UPDATE app_notifications SET is_read = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE app_notifications SET is_read = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM app_notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM app_notifications")
    suspend fun deleteAll()

    @Query("DELETE FROM app_notifications WHERE timestamp < :olderThanMs")
    suspend fun deleteOlderThan(olderThanMs: Long)
}
