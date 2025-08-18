package com.lekan.bodyfattracker.di

import com.lekan.bodyfattracker.data.ProfileRepository
import com.lekan.bodyfattracker.domain.IProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileRepositoryModule {

    @Binds
    @Singleton // Ensure the binding provides a singleton instance, consistent with ProfileRepository
    abstract fun bindProfileRepository(
        profileRepository: ProfileRepository
    ): IProfileRepository
}
