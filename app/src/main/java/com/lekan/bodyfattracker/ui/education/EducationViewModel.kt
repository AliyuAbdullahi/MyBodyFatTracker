package com.lekan.bodyfattracker.ui.education

import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.ui.core.CoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VideoInfo(
    val id: String, // Typically the YouTube video ID
    val title: String,
    val thumbnailUrl: String,
    val youtubeVideoId: String
)

data class EducationUiState(
    val isLoading: Boolean = false,
    val videos: List<VideoInfo> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class EducationViewModel @Inject constructor() : CoreViewModel<EducationUiState>() {

    override fun initialize(): EducationUiState {
        // Load dummy data initially
        return EducationUiState(isLoading = true)
    }

    init {
        loadDummyVideos()
    }

    private fun loadDummyVideos() {
        val dummyVideos = listOf(
            VideoInfo(
                id = "dQw4w9WgXcQ",
                title = "Understanding Body Fat vs. Muscle",
                youtubeVideoId = "dQw4w9WgXcQ", // Example Video ID
                thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/mqdefault.jpg"
            ),
            VideoInfo(
                id = "rokGy0huYEA",
                title = "Top 5 Exercises to Reduce Belly Fat",
                youtubeVideoId = "rokGy0huYEA", // Example Video ID
                thumbnailUrl = "https://img.youtube.com/vi/rokGy0huYEA/mqdefault.jpg"
            ),
            VideoInfo(
                id = "SboPCxvNOmQ",
                title = "Healthy Eating for Fat Loss - Nutrition Guide",
                youtubeVideoId = "SboPCxvNOmQ", // Example Video ID
                thumbnailUrl = "https://img.youtube.com/vi/SboPCxvNOmQ/mqdefault.jpg"
            ),
            VideoInfo(
                id = "gC_L9qAHVJ8",
                title = "Fitness Motivation: Stay Consistent!",
                youtubeVideoId = "gC_L9qAHVJ8", // Example Video ID
                thumbnailUrl = "https://img.youtube.com/vi/gC_L9qAHVJ8/mqdefault.jpg"
            )
        )
        viewModelScope.launch {
            updateState {
                copy(isLoading = false, videos = dummyVideos)
            }
        }
    }

    // Later, you can add functions to fetch videos from Firebase
    // fun fetchVideosFromFirebase() { ... }
}
