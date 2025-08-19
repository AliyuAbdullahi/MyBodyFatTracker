package com.lekan.bodyfattracker.di

import android.content.Context
import com.lekan.bodyfattracker.data.local.ReminderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReminderRepositoryModule {

    @Provides
    @Singleton
    fun provideReminderRepository(@ApplicationContext context: Context): ReminderRepository = ReminderRepository(context)
}