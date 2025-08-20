package com.lekan.bodyfattracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lekan.bodyfattracker.ui.education.VideoInfo

@Entity(tableName = "saved_videos")
data class SavedVideoEntity(
    @PrimaryKey val id: String, // Firebase Document ID
    val title: String,
    val youtubeVideoId: String,
    val thumbnailUrl: String
)

// Helper function to map from the domain/UI VideoInfo to SavedVideoEntity
fun VideoInfo.toEntity(): SavedVideoEntity {
    return SavedVideoEntity(
        id = this.id, // Ensure your VideoInfo has this id, corresponding to Firestore doc id
        title = this.title,
        youtubeVideoId = this.youtubeVideoId,
        thumbnailUrl = this.thumbnailUrl
    )
}

// Helper function to map from SavedVideoEntity to the domain/UI VideoInfo
fun SavedVideoEntity.toDomain(): VideoInfo {
    return VideoInfo(
        id = this.id,
        title = this.title,
        youtubeVideoId = this.youtubeVideoId,
        thumbnailUrl = this.thumbnailUrl
    )
}
