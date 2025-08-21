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
        WeightEntry::class,
        SavedVideoEntity::class
    ],
    version = 3, // This will likely need to be incremented later due to schema changes
    exportSchema = false
)
@TypeConverters(
    DateConverter::class,
    GenderConverter::class,
    MeasurementMethodConverter::class
    // WeightUnitConverter::class, // <-- REMOVED THIS LINE
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun bodyFatMeasurementDao(): BodyFatMeasurementDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun savedVideoDao(): SavedVideoDao
}
