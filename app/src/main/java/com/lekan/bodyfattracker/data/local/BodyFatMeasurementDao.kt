package com.lekan.bodyfattracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lekan.bodyfattracker.model.BodyFatMeasurement
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyFatMeasurementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: BodyFatMeasurement): Long

    @Query("SELECT * FROM body_fat_measurements ORDER BY timeStamp DESC")
    fun getAllMeasurements(): Flow<List<BodyFatMeasurement>>

    @Query("SELECT * FROM body_fat_measurements ORDER BY timeStamp DESC LIMIT 1")
    fun getLatestMeasurement(): Flow<BodyFatMeasurement?>

    @Query("DELETE FROM body_fat_measurements WHERE id = :id") // Added
    suspend fun deleteMeasurementById(id: Long) // Added

    @Query("DELETE FROM body_fat_measurements")
    suspend fun clearAllMeasurements()
}