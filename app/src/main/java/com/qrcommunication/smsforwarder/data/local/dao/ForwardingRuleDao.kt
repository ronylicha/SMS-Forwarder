package com.qrcommunication.smsforwarder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.qrcommunication.smsforwarder.data.local.entity.ForwardingRule
import kotlinx.coroutines.flow.Flow

@Dao
interface ForwardingRuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: ForwardingRule): Long

    @Update
    suspend fun update(rule: ForwardingRule)

    @Delete
    suspend fun delete(rule: ForwardingRule)

    @Query("SELECT * FROM forwarding_rules ORDER BY priority DESC, created_at ASC")
    fun observeAll(): Flow<List<ForwardingRule>>

    @Query("SELECT * FROM forwarding_rules WHERE is_enabled = 1 ORDER BY priority DESC, created_at ASC")
    suspend fun getEnabledOrdered(): List<ForwardingRule>

    @Query("SELECT * FROM forwarding_rules WHERE id = :id")
    suspend fun getById(id: Long): ForwardingRule?

    @Query("UPDATE forwarding_rules SET is_enabled = :enabled, updated_at = :updatedAt WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean, updatedAt: Long)

    @Query(
        "UPDATE forwarding_rules SET success_count = success_count + 1, " +
            "last_success_at = :timestamp, last_error_at = NULL, last_error_message = NULL " +
            "WHERE id = :id"
    )
    suspend fun recordSuccess(id: Long, timestamp: Long)

    @Query(
        "UPDATE forwarding_rules SET failure_count = failure_count + 1, " +
            "last_error_at = :timestamp, last_error_message = :error WHERE id = :id"
    )
    suspend fun recordFailure(id: Long, timestamp: Long, error: String?)

    @Query("DELETE FROM forwarding_rules")
    suspend fun deleteAll()
}
