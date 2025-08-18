package com.lekan.bodyfattracker.domain

import com.lekan.bodyfattracker.model.WeightEntry
import kotlinx.coroutines.flow.Flow

interface IWeightEntryRepository {

    /**
     * Retrieves the latest weight entry as a Flow.
     * Emits null if no entry is found.
     * @return A Flow emitting the latest WeightEntry or null.
     */
    fun getLatestWeightEntry(): Flow<WeightEntry?>

    /**
     * Retrieves all weight entries as a Flow, ordered by date (newest first).
     * @return A Flow emitting a list of WeightEntries.
     */
    fun getAllWeightEntries(): Flow<List<WeightEntry>>

    /**
     * Saves a new weight entry.
     * @param entry The WeightEntry to save.
     * @return The ID of the newly saved entry.
     */
    suspend fun saveWeightEntry(entry: WeightEntry): Long

    /**
     * Deletes a specific weight entry by its ID.
     * @param entryId The ID of the weight entry to delete.
     */
    suspend fun deleteWeightEntryById(entryId: Long)

    /**
     * Clears all weight entries from the repository.
     */
    suspend fun clearAllWeightEntries()
}