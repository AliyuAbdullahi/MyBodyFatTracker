package com.lekan.bodyfattracker.data

import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lekan.bodyfattracker.ui.education.VideoInfo // Assuming VideoInfo is here
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EducationRepository @Inject constructor(private val firestore: FirebaseFirestore) {

    fun getEducationVideos(): Flow<List<VideoInfo>> = callbackFlow {
        val collectionRef = firestore.collection("education_videos")
            .orderBy("title", Query.Direction.ASCENDING) // You might want to order by 'createdAt' or a specific 'order' field

        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Close the flow with an error
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val videos = snapshot.documents.mapNotNull { document ->
                    val title = document.getString("title") ?: return@mapNotNull null
                    val youtubeVideoUrl = document.getString("youtubeVideoUrl") ?: return@mapNotNull null
                    val videoId = extractYouTubeVideoId(youtubeVideoUrl)
                        ?: return@mapNotNull null // Skip if ID can't be extracted

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
    }

    fun addVideo(title: String, youtubeVideoUrl: String): Flow<Result<String>> = callbackFlow {
        trySend(Result.success("Checking existing...")) // Optional: for immediate feedback

        val querySnapshot = firestore.collection("education_videos")
            .whereEqualTo("title", title)
            .get()
            .await() // Use await for one-time get

        if (!querySnapshot.isEmpty) {
            trySend(Result.success("Video with this title already exists."))
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
                trySend(Result.success("Video added successfully!")).isSuccess
                close() // Close the flow on success
            }
            .addOnFailureListener { e ->
                trySend(Result.failure(e)).isSuccess
                close() // Close the flow on failure
            }

        awaitClose { /* No specific cleanup needed for addOnSuccessListener/FailureListener here */ }
    }

    private fun extractYouTubeVideoId(youtubeUrl: String): String? {
        return try {
            val uri = youtubeUrl.toUri()
            var videoId = uri.getQueryParameter("v")
            if (videoId.isNullOrEmpty()) {
                if (uri.host == "youtu.be") { // Check host for youtu.be links
                    videoId = uri.lastPathSegment
                }
            }
            videoId
        } catch (e: Exception) {
            // Log error or handle malformed URL
            null
        }
    }

    private fun generateYouTubeThumbnailUrl(videoId: String): String {
        return "https://img.youtube.com/vi/$videoId/mqdefault.jpg"
    }
}
