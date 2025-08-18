package com.lekan.bodyfattracker.di

import com.lekan.bodyfattracker.data.BodyFatInfoRepository // Correct implementation
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository // Interface to bind
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BodyFatInfoRepositoryModule { // Changed from 'object' to 'abstract class'

    @Binds
    @Singleton // Ensure the binding provides a singleton instance
    abstract fun bindBodyFatInfoRepository(
        bodyFatInfoRepository: BodyFatInfoRepository // Hilt knows how to create this
        // (it has @Inject constructor and Dao dependency)
    ): IBodyFatInfoRepository
}