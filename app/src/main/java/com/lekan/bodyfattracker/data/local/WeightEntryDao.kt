package com.lekan.bodyfattracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lekan.bodyfattracker.model.WeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(entry: WeightEntry): Long // Return Long for the new ID

    @Query("SELECT * FROM weight_entries ORDER BY timeStamp DESC")
    fun getAllWeightEntries(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries ORDER BY timeStamp DESC LIMIT 1")
    fun getLatestWeightEntry(): Flow<WeightEntry?> // Nullable if no entries

    @Query("SELECT * FROM weight_entries WHERE id = :id")
    fun getWeightEntryById(id: Long): Flow<WeightEntry?>

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteWeightEntryById(id: Long)

    @Query("DELETE FROM weight_entries")
    suspend fun clearAllWeightEntries()
}

