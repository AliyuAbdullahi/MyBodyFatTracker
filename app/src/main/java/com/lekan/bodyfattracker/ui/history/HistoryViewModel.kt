package com.lekan.bodyfattracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository
import com.lekan.bodyfattracker.domain.IWeightEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat // Added import
import java.util.Calendar // Added import
import java.util.Date // Added import
import java.util.Locale // Added import
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val historyItems: List<HistoryListItem> = emptyList(),
    val groupedHistoryItems: Map<String, List<HistoryListItem>> = emptyMap(), // Added field
    val error: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val bodyFatInfoRepository: IBodyFatInfoRepository,
    private val weightEntryRepository: IWeightEntryRepository
) : ViewModel() {

    // Helper function to group items by date string
    private fun groupHistoryItemsByDate(items: List<HistoryListItem>): Map<String, List<HistoryListItem>> {
        val sdfHeader = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val todayCalendar = Calendar.getInstance()
        val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return items.groupBy { item ->
            val itemCalendar = Calendar.getInstance().apply { timeInMillis = item.timestamp }

            when {
                itemCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                        itemCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) -> "Today"
                itemCalendar.get(Calendar.YEAR) == yesterdayCalendar.get(Calendar.YEAR) &&
                        itemCalendar.get(Calendar.DAY_OF_YEAR) == yesterdayCalendar.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
                else -> sdfHeader.format(Date(item.timestamp))
            }
        }
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        bodyFatInfoRepository.getAllMeasurements(),
        weightEntryRepository.getAllWeightEntries()
    ) { measurements, weightEntries ->
        val combinedList = mutableListOf<HistoryListItem>()
        measurements.forEach { combinedList.add(HistoryListItem.MeasurementItem(it)) }
        weightEntries.forEach { combinedList.add(HistoryListItem.WeightItem(it)) }
        combinedList.sortByDescending { it.timestamp } // Sort by timestamp

        val groupedItems = groupHistoryItemsByDate(combinedList) // Group the sorted list

        HistoryUiState( //isLoading will be false once combine emits its first value
            isLoading = false, // Set isLoading to false as data is now processed
            historyItems = combinedList,
            groupedHistoryItems = groupedItems, // Assign grouped items
            error = null // Assuming no error at this point, or handle errors from upstream flows
        )
    }
        // Removed .map { it } as it was redundant
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState(isLoading = true) // Initial state is loading
        )

    fun deleteMeasurement(id: Long) {
        viewModelScope.launch {
            try {
                bodyFatInfoRepository.deleteMeasurementById(id)
            } catch (e: Exception) {
                // Consider updating uiState.error here or emitting a specific error event
                // _uiState.update { it.copy(error = "Failed to delete measurement: ${e.message}") }
            }
        }
    }

    fun deleteWeightEntry(id: Long) {
        viewModelScope.launch {
            try {
                weightEntryRepository.deleteWeightEntryById(id)
            } catch (e: Exception) {
                // Consider updating uiState.error here or emitting a specific error event
                // _uiState.update { it.copy(error = "Failed to delete weight entry: ${e.message}") }
            }
        }
    }
}
