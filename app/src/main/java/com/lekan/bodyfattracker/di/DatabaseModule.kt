package com.lekan.bodyfattracker.di

import android.content.Context
import androidx.room.Room
import com.lekan.bodyfattracker.data.local.AppDatabase
import com.lekan.bodyfattracker.data.local.BodyFatMeasurementDao
import com.lekan.bodyfattracker.data.local.ProfileDao
import com.lekan.bodyfattracker.data.local.SavedVideoDao
import com.lekan.bodyfattracker.data.local.WeightEntryDao // Import the new DAO
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "body_fat_tracker_db" // Name of your database file
        )
            // This will clear the database if versions don't match and no migration is found.
            // Crucial because we incremented the AppDatabase version.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideProfileDao(appDatabase: AppDatabase): ProfileDao {
        return appDatabase.profileDao()
    }

    @Provides
    @Singleton
    fun provideBodyFatMeasurementDao(appDatabase: AppDatabase): BodyFatMeasurementDao {
        return appDatabase.bodyFatMeasurementDao()
    }

    @Provides
    @Singleton // Ensure DAO is a singleton
    fun provideWeightEntryDao(appDatabase: AppDatabase): WeightEntryDao {
        return appDatabase.weightEntryDao()
    }

    @Provides
    @Singleton
    fun provideSavedVideoDao(appDatabase: AppDatabase): SavedVideoDao {
        return appDatabase.savedVideoDao()
    }
}

