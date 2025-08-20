package com.lekan.bodyfattracker.data

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lekan.bodyfattracker.data.local.SavedVideoDao
import com.lekan.bodyfattracker.data.local.toDomain
import com.lekan.bodyfattracker.data.local.toEntity
import com.lekan.bodyfattracker.ui.education.VideoInfo // Ensure this is the correct path to VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map // Make sure this import is present
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EducationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val savedVideoDao: SavedVideoDao
) {

    /**
     * Fetches the list of all available education videos from Firestore.
     */
    fun getCloudVideos(): Flow<List<VideoInfo>> = callbackFlow {
        val collectionRef = firestore.collection("education_videos")
            .orderBy("title", Query.Direction.ASCENDING)

        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Close the flow with an error
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val videos = snapshot.documents.mapNotNull { document ->
                    val title = document.getString("title") ?: return@mapNotNull null
                    val youtubeVideoUrl = document.getString("youtubeVideoUrl") ?: return@mapNotNull null
                    Log.d("EducationRepo", "Mapping doc: ${document.id}, title: $title, url: $youtubeVideoUrl")

                    val videoId = extractYouTubeVideoId(youtubeVideoUrl)
                        ?: return@mapNotNull null

                    VideoInfo(
                        id = document.id, // Use Firestore document ID as the VideoInfo id
                        title = title,
                        youtubeVideoId = videoId,
                        thumbnailUrl = generateYouTubeThumbnailUrl(videoId)
                    )
                }
                trySend(videos).isSuccess // Send the latest data to the flow
            }
        }
        awaitClose { listenerRegistration.remove() }
    }.flowOn(Dispatchers.IO)

    /**
     * Adds a new video definition to Firestore (Superuser function).
     */
    fun addVideoToFirestore(title: String, youtubeVideoUrl: String): Flow<Result<String>> = callbackFlow<Result<String>> {
        trySend(Result.success("Checking existing in Firestore..."))

        val querySnapshot = firestore.collection("education_videos")
            .whereEqualTo("title", title)
            .get()
            .await()

        if (!querySnapshot.isEmpty) {
            trySend(Result.success("Video with this title already exists in Firestore."))
            close()
            return@callbackFlow
        }

        val videoData = hashMapOf(
            "title" to title,
            "youtubeVideoUrl" to youtubeVideoUrl,
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("education_videos")
            .add(videoData)
            .addOnSuccessListener {
                trySend(Result.success("Video added successfully to Firestore!")).isSuccess
                close()
            }
            .addOnFailureListener { e ->
                trySend(Result.failure(e)).isSuccess
                close()
            }
        awaitClose { }
    }.flowOn(Dispatchers.IO)


    // --- Local (Room) Database Operations ---

    /**
     * Retrieves all videos saved/bookmarked by the user from the local Room database.
     */
    fun getSavedVideos(): Flow<List<VideoInfo>> {
        return savedVideoDao.getAllSavedVideos().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Retrieves the IDs of all videos saved/bookmarked by the user.
     * Useful for efficiently checking if a cloud video is already saved.
     */
    fun getSavedVideoIds(): Flow<Set<String>> {
        return savedVideoDao.getAllSavedVideoIds().map { list ->
            list.toSet() // Convert List<String> to Set<String>
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Saves a video to the local Room database.
     */
    suspend fun saveVideoToLocal(videoInfo: VideoInfo) {
        withContext(Dispatchers.IO) {
            savedVideoDao.insertVideo(videoInfo.toEntity())
        }
    }

    /**
     * Deletes a video from the local Room database using its ID.
     */
    suspend fun deleteVideoFromLocal(videoInfo: VideoInfo) {
        withContext(Dispatchers.IO) {
            // Assuming SavedVideoEntity uses the same ID as VideoInfo for its primary key
            savedVideoDao.deleteVideoById(videoInfo.id)
        }
    }

    // --- Helper Functions ---
    private fun extractYouTubeVideoId(youtubeUrl: String): String? {
        return try {
            val uri = youtubeUrl.toUri()
            var videoId = uri.getQueryParameter("v")
            if (videoId.isNullOrEmpty()) {
                if (uri.host == "youtu.be") {
                    videoId = uri.lastPathSegment
                }
            }
            videoId
        } catch (e: Exception) {
            null
        }
    }

    // In EducationRepository.kt

// ... (other existing methods) ...

    /**
     * Deletes a list of specified videos from Firestore (Superuser function).
     */
    fun deleteVideosFromFirestore(videoIds: List<String>): Flow<Result<String>> = callbackFlow {
        if (videoIds.isEmpty()) {
            trySend(Result.success("No videos selected for deletion.")).isSuccess
            close()
            return@callbackFlow
        }

        val batch = firestore.batch()
        videoIds.forEach { videoId ->
            val docRef = firestore.collection("education_videos").document(videoId)
            batch.delete(docRef)
        }

        batch.commit()
            .addOnSuccessListener {
                trySend(Result.success("${videoIds.size} video(s) deleted successfully from Firestore.")).isSuccess
                close()
            }
            .addOnFailureListener { e ->
                trySend(Result.failure(e)).isSuccess
                close()
            }
        awaitClose { }
    }.flowOn(Dispatchers.IO)

    /**
     * Deletes all videos from the 'education_videos' collection in Firestore (Superuser function).
     * Note: This handles up to 500 videos in a single batch. For more, chunking would be needed.
     */
    fun deleteAllCloudVideosFromFirestore(): Flow<Result<String>> = callbackFlow {
        val collectionRef = firestore.collection("education_videos")

        collectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    trySend(Result.success("No cloud videos found to delete.")).isSuccess
                    close()
                    return@addOnSuccessListener
                }

                // Firestore batch writes are limited (e.g., 500 operations).
                // If more than 500 docs, this needs to be chunked.
                // For now, proceeding with a single batch.
                if (querySnapshot.size() > 500) {
                    // Log or indicate that not all videos might be deleted due to batch limit.
                    // Or implement chunking here.
                    Log.w("EducationRepository", "Attempting to delete more than 500 videos in a single batch. This might fail or be incomplete.")
                }

                val batch = firestore.batch()
                querySnapshot.documents.forEach { documentSnapshot ->
                    batch.delete(documentSnapshot.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        trySend(Result.success("All cloud videos (${querySnapshot.size()}) deleted successfully from Firestore.")).isSuccess
                        close()
                    }
                    .addOnFailureListener { e ->
                        trySend(Result.failure(e)).isSuccess
                        close()
                    }
            }
            .addOnFailureListener { e ->
                trySend(Result.failure(e)).isSuccess
                close()
            }
        awaitClose { }
    }.flowOn(Dispatchers.IO)


    private fun generateYouTubeThumbnailUrl(videoId: String): String {
        return "https://img.youtube.com/vi/$videoId/mqdefault.jpg"
    }
}
