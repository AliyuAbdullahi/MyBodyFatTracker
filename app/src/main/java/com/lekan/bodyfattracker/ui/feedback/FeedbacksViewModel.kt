package com.lekan.bodyfattracker.ui.feedback

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.lekan.bodyfattracker.model.UserFeedback
import com.lekan.bodyfattracker.ui.core.CoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class FeedbacksState(
    val feedbackList: List<UserFeedback> = emptyList(),
    val isLoading: Boolean = false, // For initial data loading
    val isDeleting: Boolean = false, // For delete operations
    val selectedFeedbackIds: Set<String> = emptySet(),
    val operationMessage: String? = null
)

@HiltViewModel
class FeedbacksViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : CoreViewModel<FeedbacksState>() {

    override fun initialize(): FeedbacksState {
        return FeedbacksState(isLoading = true)
    }

    init {
        fetchFeedbacks() // Fetch on init
    }

    fun fetchFeedbacks() {
        Log.d("FeedbacksVM", "fetchFeedbacks called")
        launch {
            updateState { copy(isLoading = true, operationMessage = null) }
            try {
                val result = firestore.collection("feedback")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                Log.d("FeedbacksVM", "Fetched ${result.documents.size} documents from Firestore.")

                val feedbacks = result.documents.mapNotNull { doc ->
                    val feedback = try {
                        // Ensure your UserFeedback model has @DocumentId val id: String
                        // and a no-argument constructor for Firestore deserialization
                        doc.toObject<UserFeedback>()?.apply {
                           // Manually assign ID if @DocumentId is not working or if you want to be explicit
                           // val explicitId = doc.id // This is already handled by @DocumentId
                        }
                    } catch (e: Exception) {
                        Log.e("FeedbacksVM", "Error deserializing document ${doc.id}", e)
                        null
                    }
                    if (feedback == null) {
                        Log.w("FeedbacksVM", "Failed to map document: ${doc.id}, data: ${doc.data}")
                    } else if (feedback.id.isBlank()) {
                        Log.w("FeedbacksVM", "Mapped document ${doc.id} but UserFeedback.id is blank. Feedback: $feedback")
                    }
                    feedback
                }
                Log.d("FeedbacksVM", "Mapped to ${feedbacks.size} UserFeedback objects.")
                updateState {
                    copy(
                        feedbackList = feedbacks,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("FeedbacksVM", "Error fetching feedbacks", e)
                updateState {
                    copy(
                        isLoading = false,
                        operationMessage = "Failed to fetch feedbacks: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleFeedbackSelection(feedbackId: String) {
        launch {
            val currentSelection = state.value.selectedFeedbackIds
            val newSelection = if (currentSelection.contains(feedbackId)) {
                currentSelection - feedbackId
            } else {
                currentSelection + feedbackId
            }
            updateState { copy(selectedFeedbackIds = newSelection) }
        }
    }

    fun clearSelection() {
        launch {
            updateState { copy(selectedFeedbackIds = emptySet()) }
        }
    }

    fun deleteSingleFeedback(feedbackId: String) {
        launch {
            updateState { copy(isDeleting = true, operationMessage = null) }
            try {
                firestore.collection("feedback").document(feedbackId).delete().await()
                updateState {
                    copy(
                        isDeleting = false,
                        operationMessage = "Feedback deleted successfully.",
                        selectedFeedbackIds = state.value.selectedFeedbackIds - feedbackId // Remove if it was selected
                    )
                }
                fetchFeedbacks() // Refresh list
            } catch (e: Exception) {
                Log.e("FeedbacksVM", "Error deleting single feedback $feedbackId", e)
                updateState {
                    copy(
                        isDeleting = false,
                        operationMessage = "Failed to delete feedback: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteSelectedFeedbacks() {
        launch {
            val selectedIds = state.value.selectedFeedbackIds
            if (selectedIds.isEmpty()) {
                updateState { copy(operationMessage = "No feedback selected.") }
                return@launch
            }
            updateState { copy(isDeleting = true, operationMessage = null) }
            try {
                val batch = firestore.batch()
                selectedIds.forEach { id ->
                    batch.delete(firestore.collection("feedback").document(id))
                }
                batch.commit().await()
                updateState {
                    copy(
                        isDeleting = false,
                        operationMessage = "${selectedIds.size} feedback(s) deleted successfully.",
                        selectedFeedbackIds = emptySet()
                    )
                }
                fetchFeedbacks() // Refresh list
            } catch (e: Exception) {
                Log.e("FeedbacksVM", "Error deleting selected feedbacks", e)
                updateState {
                    copy(
                        isDeleting = false,
                        operationMessage = "Failed to delete selected feedbacks: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearAllFeedbacks() {
        launch {
            updateState { copy(isDeleting = true, operationMessage = null) }
            try {
                // Client-side collection deletion: fetch all docs then delete in batches.
                // For very large collections, a Cloud Function is more robust.
                val allDocsSnapshot = firestore.collection("feedback").get().await()
                if (allDocsSnapshot.isEmpty) {
                    updateState { copy(isDeleting = false, operationMessage = "No feedbacks to clear.") }
                    return@launch
                }

                val batch = firestore.batch()
                allDocsSnapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()

                updateState {
                    copy(
                        isDeleting = false,
                        operationMessage = "All feedbacks cleared successfully.",
                        selectedFeedbackIds = emptySet(),
                        feedbackList = emptyList() // Clear list immediately
                    )
                }
                // fetchFeedbacks() // Optionally fetch to confirm empty or rely on immediate clear
            } catch (e: Exception) {
                Log.e("FeedbacksVM", "Error clearing all feedbacks", e)
                updateState {
                    copy(
                        isDeleting = false,
                        operationMessage = "Failed to clear all feedbacks: ${e.message}"
                    )
                }
            }
        }
    }


    fun clearOperationMessage() {
        launch {
            updateState { copy(operationMessage = null) }
        }
    }
}
