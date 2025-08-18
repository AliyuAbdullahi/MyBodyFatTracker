package com.lekan.bodyfattracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lekan.bodyfattracker.model.BodyFatMeasurement
import com.lekan.bodyfattracker.model.UserProfile
import com.lekan.bodyfattracker.model.WeightEntry // Import WeightEntry

@Database(
    entities = [
        UserProfile::class,
        BodyFatMeasurement::class,
        WeightEntry::class // Added WeightEntry entity
    ],
    version = 2, // Incremented version from 1 to 2
    exportSchema = false
)
@TypeConverters(
    DateConverter::class,
    GenderConverter::class,
    MeasurementMethodConverter::class,
    WeightUnitConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun bodyFatMeasurementDao(): BodyFatMeasurementDao
    abstract fun weightEntryDao(): WeightEntryDao // Ensure this is not commented out
}
