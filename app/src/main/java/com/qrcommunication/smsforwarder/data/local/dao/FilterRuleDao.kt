package com.qrcommunication.smsforwarder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.qrcommunication.smsforwarder.data.local.entity.FilterRule
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterRuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: FilterRule): Long

    @Update
    suspend fun updateRule(rule: FilterRule)

    @Delete
    suspend fun deleteRule(rule: FilterRule)

    @Query("SELECT * FROM filter_rules ORDER BY created_at DESC")
    fun getAllRules(): Flow<List<FilterRule>>

    @Query("SELECT * FROM filter_rules WHERE is_active = 1")
    suspend fun getActiveRules(): List<FilterRule>

    @Query("SELECT * FROM filter_rules WHERE type = :type ORDER BY created_at DESC")
    fun getRulesByType(type: String): Flow<List<FilterRule>>

    @Query("DELETE FROM filter_rules")
    suspend fun deleteAllRules()
}
