package com.qrcommunication.smsforwarder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.qrcommunication.smsforwarder.data.local.dao.AppNotificationDao
import com.qrcommunication.smsforwarder.data.local.dao.FilterRuleDao
import com.qrcommunication.smsforwarder.data.local.dao.ForwardingRuleDao
import com.qrcommunication.smsforwarder.data.local.dao.SmsRecordDao
import com.qrcommunication.smsforwarder.data.local.entity.AppNotification
import com.qrcommunication.smsforwarder.data.local.entity.FilterRule
import com.qrcommunication.smsforwarder.data.local.entity.ForwardingRule
import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord

@Database(
    entities = [
        SmsRecord::class,
        FilterRule::class,
        ForwardingRule::class,
        AppNotification::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smsRecordDao(): SmsRecordDao
    abstract fun filterRuleDao(): FilterRuleDao
    abstract fun forwardingRuleDao(): ForwardingRuleDao
    abstract fun appNotificationDao(): AppNotificationDao
}
