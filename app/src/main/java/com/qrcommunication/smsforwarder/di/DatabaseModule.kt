package com.qrcommunication.smsforwarder.di

import android.content.Context
import androidx.room.Room
import com.qrcommunication.smsforwarder.data.local.AppDatabase
import com.qrcommunication.smsforwarder.data.local.dao.AppNotificationDao
import com.qrcommunication.smsforwarder.data.local.dao.FilterRuleDao
import com.qrcommunication.smsforwarder.data.local.dao.ForwardingRuleDao
import com.qrcommunication.smsforwarder.data.local.dao.SmsRecordDao
import com.qrcommunication.smsforwarder.data.local.migrations.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "sms_forwarder_db",
    )
        .addMigrations(MIGRATION_1_2)
        .build()

    @Provides
    fun provideSmsRecordDao(database: AppDatabase): SmsRecordDao = database.smsRecordDao()

    @Provides
    fun provideFilterRuleDao(database: AppDatabase): FilterRuleDao = database.filterRuleDao()

    @Provides
    fun provideForwardingRuleDao(database: AppDatabase): ForwardingRuleDao =
        database.forwardingRuleDao()

    @Provides
    fun provideAppNotificationDao(database: AppDatabase): AppNotificationDao =
        database.appNotificationDao()
}
