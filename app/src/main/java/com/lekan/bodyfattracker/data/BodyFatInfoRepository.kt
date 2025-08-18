package com.lekan.bodyfattracker.data

import com.lekan.bodyfattracker.data.local.BodyFatMeasurementDao
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository
import com.lekan.bodyfattracker.model.BodyFatMeasurement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyFatInfoRepository @Inject constructor(
    private val bodyFatMeasurementDao: BodyFatMeasurementDao
) : IBodyFatInfoRepository {

    override fun getLatestMeasurement(): Flow<BodyFatMeasurement?> {
        return bodyFatMeasurementDao.getLatestMeasurement()
    }

    override fun getAllMeasurements(): Flow<List<BodyFatMeasurement>> {
        return bodyFatMeasurementDao.getAllMeasurements()
    }

    override suspend fun saveMeasurement(measurement: BodyFatMeasurement): Long {
        return bodyFatMeasurementDao.insertMeasurement(measurement)
    }

    override suspend fun deleteMeasurementById(id: Long) { // Added
        bodyFatMeasurementDao.deleteMeasurementById(id) // Added
    }

    override suspend fun clearAllMeasurements() {
        bodyFatMeasurementDao.clearAllMeasurements()
    }
}