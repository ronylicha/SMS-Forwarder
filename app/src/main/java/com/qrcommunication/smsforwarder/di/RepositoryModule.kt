package com.qrcommunication.smsforwarder.di

import com.qrcommunication.smsforwarder.data.repository.FilterRepository
import com.qrcommunication.smsforwarder.data.repository.FilterRepositoryImpl
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.data.repository.SmsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSmsRepository(
        impl: SmsRepositoryImpl
    ): SmsRepository

    @Binds
    @Singleton
    abstract fun bindFilterRepository(
        impl: FilterRepositoryImpl
    ): FilterRepository
}
