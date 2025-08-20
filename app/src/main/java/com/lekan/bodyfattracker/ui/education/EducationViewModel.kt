package com.lekan.bodyfattracker.ui.education

// Make sure VideoInfo is defined correctly, assuming it's here or imported
// For simplicity, defining it here if it's not elsewhere globally accessible in this structure
// data class VideoInfo(
// val id: String, // Firestore document ID
// val title: String,
// val thumbnailUrl: String,
// val youtubeVideoId: String
// )
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.data.EducationRepository
import com.lekan.bodyfattracker.ui.core.CoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// This should ideally be in a shared location or EducationRepository if it's the definitive source
// Ensure this matches the definition used by SavedVideoEntity mappers and Repository
data class VideoInfo(
    val id: String, // Firestore document ID
    val title: String,
    val thumbnailUrl: String,
    val youtubeVideoId: String
)

data class EducationUiState(
    val currentTab: Int = 0, // 0 for Cloud, 1 for Saved
    val isLoadingCloud: Boolean = false,
    val cloudVideos: List<VideoInfo> = emptyList(),
    val savedVideosList: List<VideoInfo> = emptyList(),
    val savedVideoIds: Set<String> = emptySet(), // IDs of videos saved in Room
    val error: String? = null,

    // SuperUser Feature States
    val isSuperUser: Boolean = true, // Placeholder
    val showAddVideoDialog: Boolean = false,
    val newVideoTitle: String = "",
    val newVideoUrl: String = "",
    val newVideoTitleError: String? = null,
    val newVideoUrlError: String? = null,
    val addVideoInProgress: Boolean = false,
    val addVideoMessage: String? = null
)

@HiltViewModel
class EducationViewModel @Inject constructor(
    private val repository: EducationRepository
) : CoreViewModel<EducationUiState>() {

    override fun initialize(): EducationUiState = EducationUiState()


    init {
        fetchCloudVideos() // Initial fetch for cloud videos

        // Collect saved videos
        launch {
            repository.getSavedVideos()
                .catch { exception ->
                    updateState {
                        copy(error = "Failed to load saved videos: ${exception.message}")
                    }
                }
                .collect { savedVideos ->
                    updateState { copy(savedVideosList = savedVideos) }
                }
        }

        // Collect saved video IDs
        launch {
            repository.getSavedVideoIds()
                .catch { exception ->
                    updateState {
                        copy(error = "Failed to load saved video IDs: ${exception.message}")
                    }
                }
                .collect { ids ->
                    updateState { copy(savedVideoIds = ids) }
                }
        }
    }

    // In EducationViewModel.kt
    fun fetchCloudVideos() {
        launch {
            updateState { copy(isLoadingCloud = true, error = null) } // Sets isLoadingCloud = true
            repository.getCloudVideos()
                .catch { e ->
                    updateState {
                        copy(
                            isLoadingCloud = false, // Sets isLoadingCloud = false on error
                            error = "Error fetching cloud videos: ${e.localizedMessage}"
                        )
                    }
                }
                .collect { videos ->
                    Log.d("EducationViewModel", "Cloud videos fetched: ${videos.size} items") // Add this log
                    updateState {
                        copy(
                            isLoadingCloud = false,
                            cloudVideos = videos
                        )
                    }
                }
        }
    }


    fun onTabSelected(tabIndex: Int) {
       launch {
           updateState { copy(currentTab = tabIndex) }
       }
    }

    fun bookmarkVideo(video: VideoInfo) {
        launch {
            try {
                repository.saveVideoToLocal(video)
                // UI will update reactively via the flow collecting savedVideoIds
            } catch (e: Exception) {
                updateState { copy(error = "Error bookmarking video: ${e.message}") }
            }
        }
    }

    fun unbookmarkVideo(video: VideoInfo) {
        launch {
            try {
                repository.deleteVideoFromLocal(video)
                // UI will update reactively
            } catch (e: Exception) {
                updateState { copy(error = "Error unbookmarking video: ${e.message}") }
            }
        }
    }

    fun deleteSavedVideo(video: VideoInfo) {
        launch {
            try {
                repository.deleteVideoFromLocal(video) // Same as unbookmarking
            } catch (e: Exception) {
                updateState { copy(error = "Error deleting saved video: ${e.message}") }
            }
        }
    }

    // --- SuperUser: Add Video Feature ---

    fun onNewVideoTitleChanged(title: String) {
        launch {
            updateState { copy(newVideoTitle = title, newVideoTitleError = null) }
        }
    }

    fun onNewVideoUrlChanged(url: String) {
        launch {
            updateState { copy(newVideoUrl = url, newVideoUrlError = null) }
        }
    }

    fun onShowAddVideoDialog() {
        launch {
            updateState {
                copy(
                    showAddVideoDialog = true,
                    newVideoTitle = "",
                    newVideoUrl = "",
                    newVideoTitleError = null,
                    newVideoUrlError = null,
                    addVideoInProgress = false,
                    addVideoMessage = null
                )
            }
        }
    }

    fun onDismissAddVideoDialog() {
        launch {
            updateState {
                copy(
                    showAddVideoDialog = false,
                    newVideoTitle = "",
                    newVideoUrl = "",
                    newVideoTitleError = null,
                    newVideoUrlError = null,
                    addVideoInProgress = false
                )
            }
        }
    }

    fun submitNewVideo() {
        val currentTitle = state.value.newVideoTitle.trim()
        val currentUrl = state.value.newVideoUrl.trim()

        var hasError = false
        if (currentTitle.isEmpty()) {
            viewModelScope.launch {
                updateState { copy(newVideoTitleError = "Title cannot be empty") }
            }
            hasError = true
        }
        if (currentUrl.isEmpty() || (!currentUrl.startsWith("http://") && !currentUrl.startsWith("https://"))) {
            viewModelScope.launch {
                updateState { copy(newVideoUrlError = "Please enter a valid URL") }
            }
            hasError = true
        }

        if (hasError) {
            return
        }

        launch {
            updateState { copy(addVideoInProgress = true, addVideoMessage = null) }
        }

        launch {
            repository.addVideoToFirestore(currentTitle, currentUrl) // Changed to addVideoToFirestore
                .catch { exception ->
                    updateState {
                        copy(
                            addVideoInProgress = false,
                            addVideoMessage = exception.message ?: "Failed to add video"
                        )
                    }
                }
                .collect { result -> // Assuming addVideoToFirestore returns Flow<Result<String>>
                    result.fold(
                        onSuccess = { message ->
                            updateState {
                                val closeDialog = message.contains("added successfully", ignoreCase = true)
                                copy(
                                    addVideoInProgress = false,
                                    addVideoMessage = message,
                                    newVideoTitle = if (closeDialog) "" else currentTitle,
                                    newVideoUrl = if (closeDialog) "" else currentUrl,
                                    showAddVideoDialog = !closeDialog
                                )
                            }
                            if (message.contains("added successfully", ignoreCase = true)) {
                                fetchCloudVideos() // Refresh cloud video list
                            }
                        },
                        onFailure = { exception ->
                            updateState {
                                copy(
                                    addVideoInProgress = false,
                                    addVideoMessage = exception.message ?: "An unexpected error occurred"
                                )
                            }
                        }
                    )
                }
        }
    }

    fun clearAddVideoMessage() {
        launch {
            updateState { copy(addVideoMessage = null) }
        }
    }
}
