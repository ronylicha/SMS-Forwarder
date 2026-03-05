package com.qrcommunication.smsforwarder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SmsRecord): Long

    @Update
    suspend fun updateRecord(record: SmsRecord)

    @Query("SELECT * FROM sms_records ORDER BY received_at DESC")
    fun getAllRecords(): Flow<List<SmsRecord>>

    @Query("SELECT * FROM sms_records WHERE id = :id")
    suspend fun getRecordById(id: Long): SmsRecord?

    @Query("SELECT * FROM sms_records WHERE status = :status ORDER BY received_at DESC")
    fun getRecordsByStatus(status: String): Flow<List<SmsRecord>>

    @Query("SELECT * FROM sms_records ORDER BY received_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getRecordsPaginated(limit: Int, offset: Int): List<SmsRecord>

    @Query("SELECT COUNT(*) FROM sms_records")
    fun getRecordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sms_records WHERE status = :status")
    fun getRecordCountByStatus(status: String): Flow<Int>

    @Query("SELECT * FROM sms_records WHERE received_at BETWEEN :startMs AND :endMs ORDER BY received_at DESC")
    suspend fun getRecordsForDateRange(startMs: Long, endMs: Long): List<SmsRecord>

    @Query("DELETE FROM sms_records")
    suspend fun deleteAllRecords()

    @Query("SELECT * FROM sms_records WHERE sender LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY received_at DESC")
    fun searchRecords(query: String): Flow<List<SmsRecord>>
}
