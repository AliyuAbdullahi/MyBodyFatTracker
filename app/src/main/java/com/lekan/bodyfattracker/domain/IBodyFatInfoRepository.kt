package com.lekan.bodyfattracker.domain

import com.lekan.bodyfattracker.model.BodyFatMeasurement
import kotlinx.coroutines.flow.Flow

interface IBodyFatInfoRepository {
    fun getLatestMeasurement(): Flow<BodyFatMeasurement?>
    fun getAllMeasurements(): Flow<List<BodyFatMeasurement>>
    suspend fun saveMeasurement(measurement: BodyFatMeasurement): Long
    suspend fun deleteMeasurementById(id: Long) // Added
    suspend fun clearAllMeasurements()
}