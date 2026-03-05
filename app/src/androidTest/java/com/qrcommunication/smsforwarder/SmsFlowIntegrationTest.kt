package com.qrcommunication.smsforwarder

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qrcommunication.smsforwarder.data.local.AppDatabase
import com.qrcommunication.smsforwarder.data.local.entity.FilterRule
import com.qrcommunication.smsforwarder.data.local.entity.FilterType
import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmsFlowIntegrationTest {

    private lateinit var database: AppDatabase
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun smsReceived_isStoredInDatabase() = runTest {
        // Arrange
        val dao = database.smsRecordDao()
        val record = SmsRecord(
            sender = "+33612345678",
            content = "Test message received",
            receivedAt = System.currentTimeMillis(),
            status = SmsStatus.PENDING.value,
            destination = "+33699999999"
        )

        // Act
        val recordId = dao.insertRecord(record)

        // Assert
        assertTrue(recordId > 0)
        val saved = dao.getRecordById(recordId)
        assertNotNull(saved)
        assertEquals("+33612345678", saved!!.sender)
        assertEquals("Test message received", saved.content)
        assertEquals(SmsStatus.PENDING.value, saved.status)
        assertEquals("+33699999999", saved.destination)
    }

    @Test
    fun smsReceived_isForwardedToDestination() = runTest {
        // Arrange
        val dao = database.smsRecordDao()
        val record = SmsRecord(
            sender = "+33612345678",
            content = "Message to forward",
            receivedAt = System.currentTimeMillis(),
            status = SmsStatus.PENDING.value,
            destination = "+33699999999"
        )
        val recordId = dao.insertRecord(record)

        // Act: simulate successful send by updating the record
        val inserted = dao.getRecordById(recordId)!!
        dao.updateRecord(inserted.copy(
            status = SmsStatus.SENT.value,
            forwardedAt = System.currentTimeMillis()
        ))

        // Assert
        val updated = dao.getRecordById(recordId)
        assertNotNull(updated)
        assertEquals(SmsStatus.SENT.value, updated!!.status)
        assertNotNull(updated.forwardedAt)
        assertNull(updated.errorMessage)
    }

    @Test
    fun smsFiltered_isStoredWithFilteredStatus() = runTest {
        // Arrange
        val dao = database.smsRecordDao()
        val record = SmsRecord(
            sender = "+33612345678",
            content = "Filtered message",
            receivedAt = System.currentTimeMillis(),
            status = SmsStatus.FILTERED.value,
            destination = "+33699999999",
            errorMessage = "Not in whitelist"
        )

        // Act
        val recordId = dao.insertRecord(record)

        // Assert
        val saved = dao.getRecordById(recordId)
        assertNotNull(saved)
        assertEquals(SmsStatus.FILTERED.value, saved!!.status)
        assertEquals("Not in whitelist", saved.errorMessage)
    }

    @Test
    fun smsFailed_statusUpdatedCorrectly() = runTest {
        // Arrange
        val dao = database.smsRecordDao()
        val record = SmsRecord(
            sender = "+33612345678",
            content = "Failed message",
            receivedAt = System.currentTimeMillis(),
            status = SmsStatus.PENDING.value,
            destination = "+33699999999"
        )
        val recordId = dao.insertRecord(record)

        // Act: simulate failure by updating the record
        val inserted = dao.getRecordById(recordId)!!
        dao.updateRecord(inserted.copy(
            status = SmsStatus.FAILED.value,
            errorMessage = "Network error"
        ))

        // Assert
        val updated = dao.getRecordById(recordId)
        assertNotNull(updated)
        assertEquals(SmsStatus.FAILED.value, updated!!.status)
        assertEquals("Network error", updated.errorMessage)
    }

    @Test
    fun multipleRecords_retrievedInOrder() = runTest {
        // Arrange
        val dao = database.smsRecordDao()
        val now = System.currentTimeMillis()
        val records = listOf(
            SmsRecord(sender = "+33611111111", content = "First", receivedAt = now - 2000, status = SmsStatus.SENT.value, destination = "+33699999999"),
            SmsRecord(sender = "+33622222222", content = "Second", receivedAt = now - 1000, status = SmsStatus.SENT.value, destination = "+33699999999"),
            SmsRecord(sender = "+33633333333", content = "Third", receivedAt = now, status = SmsStatus.SENT.value, destination = "+33699999999")
        )
        records.forEach { dao.insertRecord(it) }

        // Act
        val allRecords = dao.getAllRecords().first()

        // Assert
        assertEquals(3, allRecords.size)
        // Ordered by received_at DESC, so Third is first
        assertEquals("Third", allRecords[0].content)
        assertEquals("Second", allRecords[1].content)
        assertEquals("First", allRecords[2].content)
    }

    @Test
    fun filterRules_insertAndRetrieve() = runTest {
        // Arrange
        val filterDao = database.filterRuleDao()
        val rule = FilterRule(
            pattern = "+33612345678",
            type = FilterType.WHITELIST.value,
            isActive = true,
            createdAt = System.currentTimeMillis()
        )

        // Act
        val ruleId = filterDao.insertRule(rule)

        // Assert
        assertTrue(ruleId > 0)
        val allRules = filterDao.getAllRules().first()
        assertEquals(1, allRules.size)
        assertEquals("+33612345678", allRules[0].pattern)
        assertEquals(FilterType.WHITELIST.value, allRules[0].type)
        assertTrue(allRules[0].isActive)
    }

    @Test
    fun activeFilterRules_onlyReturnsActive() = runTest {
        // Arrange
        val filterDao = database.filterRuleDao()
        filterDao.insertRule(FilterRule(pattern = "+33611111111", type = FilterType.WHITELIST.value, isActive = true, createdAt = System.currentTimeMillis()))
        filterDao.insertRule(FilterRule(pattern = "+33622222222", type = FilterType.WHITELIST.value, isActive = false, createdAt = System.currentTimeMillis()))
        filterDao.insertRule(FilterRule(pattern = "+33633333333", type = FilterType.BLACKLIST.value, isActive = true, createdAt = System.currentTimeMillis()))

        // Act
        val activeRules = filterDao.getActiveRules()

        // Assert
        assertEquals(2, activeRules.size)
        assertTrue(activeRules.all { it.isActive })
    }

    @Test
    fun searchRecords_findsMatchingContent() = runTest {
        // Arrange
        val dao = database.smsRecordDao()
        dao.insertRecord(SmsRecord(sender = "+33611111111", content = "Hello world", receivedAt = System.currentTimeMillis(), status = SmsStatus.SENT.value, destination = "+33699999999"))
        dao.insertRecord(SmsRecord(sender = "+33622222222", content = "Goodbye world", receivedAt = System.currentTimeMillis(), status = SmsStatus.SENT.value, destination = "+33699999999"))
        dao.insertRecord(SmsRecord(sender = "+33633333333", content = "Nothing here", receivedAt = System.currentTimeMillis(), status = SmsStatus.SENT.value, destination = "+33699999999"))

        // Act
        val results = dao.searchRecords("world").first()

        // Assert
        assertEquals(2, results.size)
    }
}
