package com.lekan.bodyfattracker.data

import com.lekan.bodyfattracker.data.local.WeightEntryDao
import com.lekan.bodyfattracker.domain.IWeightEntryRepository
import com.lekan.bodyfattracker.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Marks this repository as a singleton, managed by Hilt
class WeightEntryRepository @Inject constructor(
    private val weightEntryDao: WeightEntryDao // Hilt injects the DAO
) : IWeightEntryRepository {

    override fun getLatestWeightEntry(): Flow<WeightEntry?> {
        return weightEntryDao.getLatestWeightEntry()
    }

    override fun getAllWeightEntries(): Flow<List<WeightEntry>> {
        return weightEntryDao.getAllWeightEntries()
    }

    override suspend fun saveWeightEntry(entry: WeightEntry): Long {
        return weightEntryDao.insertWeightEntry(entry)
    }

    override suspend fun deleteWeightEntryById(entryId: Long) {
        weightEntryDao.deleteWeightEntryById(entryId)
    }

    override suspend fun clearAllWeightEntries() {
        weightEntryDao.clearAllWeightEntries()
    }
}

