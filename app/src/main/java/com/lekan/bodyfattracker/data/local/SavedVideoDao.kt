package com.lekan.bodyfattracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedVideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: SavedVideoEntity)

    @Delete
    suspend fun deleteVideo(video: SavedVideoEntity)

    // Used to check if a specific video (by its Firebase ID) is saved
    @Query("SELECT EXISTS(SELECT 1 FROM saved_videos WHERE id = :videoId LIMIT 1)")
    fun isVideoSaved(videoId: String): Flow<Boolean>

    // Optimized query to get only the IDs of all saved videos
    @Query("SELECT id FROM saved_videos")
    fun getAllSavedVideoIds(): Flow<List<String>> // Changed to List<String>

    @Query("SELECT * FROM saved_videos ORDER BY title ASC") // Or any other order you prefer
    fun getAllSavedVideos(): Flow<List<SavedVideoEntity>>

    // Potentially useful for direct lookups, though getAllSavedVideoIds might be better for UI checks
    @Query("SELECT * FROM saved_videos WHERE id = :videoId")
    suspend fun getVideoById(videoId: String): SavedVideoEntity?

    @Query("DELETE FROM saved_videos WHERE id = :videoId")
    suspend fun deleteVideoById(videoId: String)
}
