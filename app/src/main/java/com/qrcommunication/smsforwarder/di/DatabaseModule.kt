package com.qrcommunication.smsforwarder.di

import android.content.Context
import androidx.room.Room
import com.qrcommunication.smsforwarder.data.local.AppDatabase
import com.qrcommunication.smsforwarder.data.local.dao.FilterRuleDao
import com.qrcommunication.smsforwarder.data.local.dao.SmsRecordDao
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
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sms_forwarder_db"
        ).build()
    }

    @Provides
    fun provideSmsRecordDao(database: AppDatabase): SmsRecordDao {
        return database.smsRecordDao()
    }

    @Provides
    fun provideFilterRuleDao(database: AppDatabase): FilterRuleDao {
        return database.filterRuleDao()
    }
}
