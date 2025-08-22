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
import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.isEmpty
import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.BuildConfig
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
    val isSuperUser: Boolean = BuildConfig.IS_SUPER_USER, // Placeholder
    val showAddVideoDialog: Boolean = false,
    val newVideoTitle: String = "",
    val newVideoUrl: String = "",
    val newVideoTitleError: String? = null,
    val newVideoUrlError: String? = null,
    val addVideoInProgress: Boolean = false,
    val addVideoMessage: String? = null,

    // Search Feature States
    val searchQuery: String = "",
    val filteredCloudVideos: List<VideoInfo> = emptyList(),

    // SuperUser Delete Feature States
    val isSelectionModeActive: Boolean = false,
    val selectedVideoIds: Set<String> = emptySet(), // IDs of cloud videos selected for deletion
    val showConfirmDeleteDialog: Boolean = false,
    val confirmDeleteDialogTitle: String = "",
    val confirmDeleteDialogMessage: String = "",
    val videoIdsPendingDeletion: List<String> = emptyList(),
    val isDeleteAllOperation: Boolean = false,
    val isDeletingVideos: Boolean = false,
    val deleteOperationMessage: String? = null
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

        // Collect saved video IDs (from Room)
        launch {
            repository.getSavedVideoIds()
                .catch { exception ->
                    updateState {
                        copy(error = "Failed to load saved video IDs: ${exception.message}")
                    }
                }
                .collect { ids ->
                    updateState { copy(savedVideoIds = ids) } // This is for bookmarked videos in Room
                }
        }
    }

    fun fetchCloudVideos() {
        launch {
            updateState { copy(isLoadingCloud = true, error = null) }
            repository.getCloudVideos()
                .catch { e ->
                    updateState {
                        copy(
                            isLoadingCloud = false,
                            error = "Error fetching cloud videos: ${e.localizedMessage}",
                            isSelectionModeActive = false, // Reset selection mode on error
                            selectedVideoIds = emptySet()   // Clear selected IDs on error
                        )
                    }
                }
                .collect { videos ->
                    Log.d("EducationViewModel", "Cloud videos fetched: ${videos.size} items")
                    updateState {
                        val currentQuery = state.value.searchQuery
                        val filteredList = if (currentQuery.isBlank()) {
                            videos
                        } else {
                            videos.filter { video ->
                                video.title.contains(currentQuery, ignoreCase = true)
                            }
                        }
                        val currentSelectedIds = state.value.selectedVideoIds
                        copy(
                            isLoadingCloud = false,
                            cloudVideos = videos,
                            filteredCloudVideos = filteredList,
                            // Reset selection mode if list is empty or selected items are no longer present
                            isSelectionModeActive = if (videos.isEmpty()) false else state.value.isSelectionModeActive,
                            selectedVideoIds = if (videos.isEmpty()) emptySet() else currentSelectedIds.filter { id -> videos.any { v -> v.id == id } }.toSet()
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        launch {
            updateState {
                val filteredList = if (query.isBlank()) {
                    state.value.cloudVideos
                } else {
                    state.value.cloudVideos.filter { video ->
                        video.title.contains(query, ignoreCase = true)
                    }
                }
                // Use `this.copy` or just `copy` if inside the `updateState` lambda for `CoreViewModel`
                copy(
                    searchQuery = query,
                    filteredCloudVideos = filteredList
                )
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
            } catch (e: Exception) {
                updateState { copy(error = "Error bookmarking video: ${e.message}") }
            }
        }
    }

    fun unbookmarkVideo(video: VideoInfo) {
        launch {
            try {
                repository.deleteVideoFromLocal(video)
            } catch (e: Exception) {
                updateState { copy(error = "Error unbookmarking video: ${e.message}") }
            }
        }
    }

    fun deleteSavedVideo(video: VideoInfo) { // This is for Room videos
        launch {
            try {
                repository.deleteVideoFromLocal(video)
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
            viewModelScope.launch { // Use viewModelScope for operations not directly tied to a single state update sequence
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
            repository.addVideoToFirestore(currentTitle, currentUrl)
                .catch { exception ->
                    updateState {
                        copy(
                            addVideoInProgress = false,
                            addVideoMessage = exception.message ?: "Failed to add video"
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { message ->
                            updateState {
                                val closeDialog = message.contains("added successfully", ignoreCase = true)
                                copy(
                                    addVideoInProgress = false,
                                    addVideoMessage = message,
                                    newVideoTitle = if (closeDialog) "" else currentTitle,
                                    newVideoUrl = if (closeDialog) "" else currentUrl,
                                    showAddVideoDialog = !closeDialog,
                                    // Reset selection mode on successful add
                                    isSelectionModeActive = false,
                                    selectedVideoIds = emptySet()
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

    fun clearErrorMessage() {
        launch {
            updateState { copy(error = null) }
        }
    }

    // --- SuperUser: Delete Cloud Video Features ---

    fun onVideoLongPress(videoInfo: VideoInfo) {
        launch {
            updateState {
                copy(
                    isSelectionModeActive = true,
                    selectedVideoIds = selectedVideoIds + videoInfo.id // Add to the set
                )
            }
        }
    }

    fun onVideoShortPressSelection(videoInfo: VideoInfo) {
        launch {
            updateState {
                val currentSelectedIds = selectedVideoIds
                copy(
                    selectedVideoIds = if (videoInfo.id in currentSelectedIds) {
                        currentSelectedIds - videoInfo.id // Remove if already selected
                    } else {
                        currentSelectedIds + videoInfo.id // Add if not selected
                    }
                )
            }
        }
    }

    fun clearSelectionMode() {
        launch {
            updateState {
                copy(
                    isSelectionModeActive = false,
                    selectedVideoIds = emptySet()
                )
            }
        }
    }

    fun prepareDeleteSelectedVideos(title: String, messageFormat: String) {
        launch {
            val currentSelectedVideoIds = state.value.selectedVideoIds
            if (currentSelectedVideoIds.isEmpty()) return@launch
            val message = messageFormat.format(currentSelectedVideoIds.size)
            updateState {
                copy(
                    showConfirmDeleteDialog = true,
                    confirmDeleteDialogTitle = title,
                    confirmDeleteDialogMessage = message,
                    videoIdsPendingDeletion = currentSelectedVideoIds.toList(),
                    isDeleteAllOperation = false
                )
            }
        }
    }

    fun prepareDeleteSingleVideoFromTap(videoInfo: VideoInfo, title: String, messageFormat: String) {
        launch {
            val message = messageFormat.format(videoInfo.title) // Format with video title
            updateState {
                copy(
                    showConfirmDeleteDialog = true,
                    confirmDeleteDialogTitle = title,
                    confirmDeleteDialogMessage = message,
                    videoIdsPendingDeletion = listOf(videoInfo.id),
                    isDeleteAllOperation = false
                )
            }
        }
    }

    fun prepareDeleteAllCloudVideos(title: String, message: String) {
        launch {
            updateState {
                copy(
                    showConfirmDeleteDialog = true,
                    confirmDeleteDialogTitle = title,
                    confirmDeleteDialogMessage = message,
                    videoIdsPendingDeletion = emptyList(), // Not strictly needed for 'all'
                    isDeleteAllOperation = true
                )
            }
        }
    }

    fun executeConfirmedDeletion(successMessageSelected: String, successMessageAll: String, errorMessageFormat: String) {
        launch {
            updateState { copy(isDeletingVideos = true, showConfirmDeleteDialog = false, deleteOperationMessage = null) }

            val operationFlow = if (state.value.isDeleteAllOperation) {
                repository.deleteAllCloudVideosFromFirestore()
            } else {
                if (state.value.videoIdsPendingDeletion.isEmpty()) {
                    updateState { copy(isDeletingVideos = false, deleteOperationMessage = "No videos selected for deletion.") }
                    return@launch
                }
                repository.deleteVideosFromFirestore(state.value.videoIdsPendingDeletion)
            }

            operationFlow
                .catch { e ->
                    updateState {
                        copy(
                            isDeletingVideos = false,
                            deleteOperationMessage = errorMessageFormat.format(e.localizedMessage ?: "Unknown error")
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { repoMessage ->
                            val finalMessage = if (state.value.isDeleteAllOperation) successMessageAll else successMessageSelected.format(state.value.videoIdsPendingDeletion.size)
                            updateState {
                                copy(
                                    isDeletingVideos = false,
                                    deleteOperationMessage = finalMessage,
                                    selectedVideoIds = emptySet(),
                                    isSelectionModeActive = false,
                                    videoIdsPendingDeletion = emptyList(), // Clear pending
                                    isDeleteAllOperation = false          // Reset flag
                                )
                            }
                            fetchCloudVideos() // Refresh the video list
                        },
                        onFailure = { e ->
                            updateState {
                                copy(
                                    isDeletingVideos = false,
                                    deleteOperationMessage = errorMessageFormat.format(e.localizedMessage ?: "Operation failed"),
                                    videoIdsPendingDeletion = emptyList(), // Clear pending
                                    isDeleteAllOperation = false          // Reset flag
                                )
                            }
                        }
                    )
                }
        }
    }

    fun cancelVideoDeletion() {
        launch {
            updateState {
                copy(
                    showConfirmDeleteDialog = false,
                    videoIdsPendingDeletion = emptyList(),
                    isDeleteAllOperation = false,
                    confirmDeleteDialogTitle = "",
                    confirmDeleteDialogMessage = ""
                )
            }
        }
    }

    fun clearDeleteOperationMessage() {
        launch {
            updateState { copy(deleteOperationMessage = null) }
        }
    }
}
