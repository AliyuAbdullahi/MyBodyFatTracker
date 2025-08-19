package com.lekan.bodyfattracker.ui.education

import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.data.EducationRepository
import com.lekan.bodyfattracker.ui.core.CoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

// id: Firestore document ID
data class VideoInfo(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val youtubeVideoId: String
)

data class EducationUiState(
    val isLoading: Boolean = false,
    val videos: List<VideoInfo> = emptyList(),
    val error: String? = null,
    // SuperUser Feature States
    val isSuperUser: Boolean = true, // Placeholder: Set to true for development
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
    private val educationRepository: EducationRepository
) : CoreViewModel<EducationUiState>() {

    override fun initialize(): EducationUiState {
        return EducationUiState(isLoading = true) // Start loading immediately
    }

    init {
        fetchEducationVideos()
    }

    private fun fetchEducationVideos() {
        viewModelScope.launch {
            educationRepository.getEducationVideos()
                .onStart {
                    // Already set isLoading to true in initialize, or set it here if preferred
                    updateState { copy(isLoading = true, error = null) }
                }
                .catch { exception ->
                    updateState { copy(isLoading = false, error = exception.message ?: "Unknown error while fetching videos") }
                }
                .collect { videoList ->
                    updateState { copy(isLoading = false, videos = videoList, error = null) }
                }
        }
    }

    // --- SuperUser: Add Video Feature ---

    fun onNewVideoTitleChanged(title: String) {
        viewModelScope.launch {
            updateState { copy(newVideoTitle = title, newVideoTitleError = null) }
        }
    }

    fun onNewVideoUrlChanged(url: String) {
       viewModelScope.launch {
           updateState { copy(newVideoUrl = url, newVideoUrlError = null) }
       }
    }

    fun onShowAddVideoDialog() {
        viewModelScope.launch {
            updateState {
                copy(
                    showAddVideoDialog = true,
                    newVideoTitle = "", // Reset fields when dialog is shown
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
        viewModelScope.launch {
            updateState {
                copy(
                    showAddVideoDialog = false,
                    newVideoTitle = "",
                    newVideoUrl = "",
                    newVideoTitleError = null,
                    newVideoUrlError = null,
                    addVideoInProgress = false
                    // Keep addVideoMessage to allow Toast to show if it was just set
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
        // Basic URL validation (not exhaustive)
        if (currentUrl.isEmpty() || (!currentUrl.startsWith("http://") && !currentUrl.startsWith("https://"))) {
            viewModelScope.launch {
               updateState { copy(newVideoUrlError = "Please enter a valid URL") }
           }
            hasError = true
        }

        if (hasError) {
            return
        }

        viewModelScope.launch {
            updateState { copy(addVideoInProgress = true, addVideoMessage = null) }
        }

        viewModelScope.launch {
            educationRepository.addVideo(currentTitle, currentUrl)
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
                                copy(
                                    addVideoInProgress = false,
                                    addVideoMessage = message,
                                    // Reset fields only if successfully added, not if "already exists" but keep dialog open for correction
                                    newVideoTitle = if (message.contains("added successfully", ignoreCase = true)) "" else currentTitle,
                                    newVideoUrl = if (message.contains("added successfully", ignoreCase = true)) "" else currentUrl,
                                    showAddVideoDialog = !message.contains("added successfully", ignoreCase = true) // Close dialog on success
                                )
                            }
                            if (message.contains("added successfully", ignoreCase = true)) {
                                fetchEducationVideos() // Refresh video list
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
        viewModelScope.launch {
            updateState { copy(addVideoMessage = null) }
        }
    }
}
