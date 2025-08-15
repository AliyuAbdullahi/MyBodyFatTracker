package com.lekan.bodyfattracker.di

import android.content.Context
import com.lekan.bodyfattracker.data.BodyFatInfoRepository
import com.lekan.bodyfattracker.data.ProfileRepository
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository
import com.lekan.bodyfattracker.domain.IProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BodyFatInfoRepositoryModule { // You had this object name in your file context

    @Provides
    @Singleton
    fun provideBodyFatInfoRepository(
        @ApplicationContext context: Context
    ): IBodyFatInfoRepository  = BodyFatInfoRepository(context)

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        @ApplicationContext context: Context
    ): IProfileRepository = ProfileRepository(context)
}
