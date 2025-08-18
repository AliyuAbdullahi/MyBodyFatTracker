package com.lekan.bodyfattracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository
import com.lekan.bodyfattracker.domain.IWeightEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val historyItems: List<HistoryListItem> = emptyList(),
    val error: String? = null
    // Consider adding a showConfirmDeleteDialog: Pair<HistoryListItem?, () -> Unit>? = null
    // Where Pair.first is the item to delete, and Pair.second is the confirm action.
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val bodyFatInfoRepository: IBodyFatInfoRepository,
    private val weightEntryRepository: IWeightEntryRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = combine(
        bodyFatInfoRepository.getAllMeasurements(),
        weightEntryRepository.getAllWeightEntries()
    ) { measurements, weightEntries ->
        val combinedList = mutableListOf<HistoryListItem>()
        measurements.forEach { combinedList.add(HistoryListItem.MeasurementItem(it)) }
        weightEntries.forEach { combinedList.add(HistoryListItem.WeightItem(it)) }
        combinedList.sortByDescending { it.timestamp }
        HistoryUiState(isLoading = false, historyItems = combinedList)
    }
        .map { historyUiState ->
            historyUiState
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState(isLoading = true)
        )

    fun deleteMeasurement(id: Long) {
        viewModelScope.launch {
            try {
                bodyFatInfoRepository.deleteMeasurementById(id)
            } catch (e: Exception) {
                // Handle error, e.g., update uiState.error
                // For now, errors are not explicitly surfaced in this simple version
            }
        }
    }

    fun deleteWeightEntry(id: Long) {
        viewModelScope.launch {
            try {
                weightEntryRepository.deleteWeightEntryById(id)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // TODO: Consider adding methods for showing/hiding a confirmation dialog
    // fun requestConfirmDelete(item: HistoryListItem) { ... }
    // fun cancelDelete() { ... }
    // fun confirmDelete() { ... }
}

