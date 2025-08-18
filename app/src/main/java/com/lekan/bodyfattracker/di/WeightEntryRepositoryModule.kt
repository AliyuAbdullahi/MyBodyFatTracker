package com.lekan.bodyfattracker.di

import com.lekan.bodyfattracker.data.WeightEntryRepository
import com.lekan.bodyfattracker.domain.IWeightEntryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeightEntryRepositoryModule {

    @Binds
    @Singleton // Ensures the binding provides a singleton instance of the repository
    abstract fun bindWeightEntryRepository(
        weightEntryRepository: WeightEntryRepository // Hilt knows how to create this
        // as it has an @Inject constructor
        // and its dependencies (WeightEntryDao) are provided by Hilt.
    ): IWeightEntryRepository
}

