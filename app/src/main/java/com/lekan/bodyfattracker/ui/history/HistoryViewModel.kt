package com.lekan.bodyfattracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository
import com.lekan.bodyfattracker.domain.IWeightEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class HistoryFilterOption {
    ALL, BODY_FAT, WEIGHT
}

enum class HistorySortOption {
    NEWEST_FIRST, OLDEST_FIRST
}

data class HistoryUiState(
    val isLoading: Boolean = true,
    val historyItems: List<HistoryListItem> = emptyList(),
    val groupedHistoryItems: Map<String, List<HistoryListItem>> = emptyMap(),
    val error: String? = null,
    val selectedFilter: HistoryFilterOption = HistoryFilterOption.ALL,
    val selectedSortOption: HistorySortOption = HistorySortOption.NEWEST_FIRST,
    val itemPendingDelete: HistoryListItem? = null,
    val showConfirmDeleteDialog: Boolean = false,
    val bodyFatChartEntries: List<Pair<Long, Float>> = emptyList(), // Updated
    val weightChartEntries: List<Pair<Long, Float>> = emptyList()    // Updated
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val bodyFatInfoRepository: IBodyFatInfoRepository,
    private val weightEntryRepository: IWeightEntryRepository
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(HistoryFilterOption.ALL)
    private val _currentSortOption = MutableStateFlow(HistorySortOption.NEWEST_FIRST)

    private val _uiState = MutableStateFlow(
        HistoryUiState(
            isLoading = true,
            itemPendingDelete = null,
            showConfirmDeleteDialog = false,
            bodyFatChartEntries = emptyList(), // Initialized
            weightChartEntries = emptyList()    // Initialized
        )
    )

    val uiState: StateFlow<HistoryUiState> = combine(
        bodyFatInfoRepository.getAllMeasurements(), // Flow<List<BodyFatMeasurement>>
        weightEntryRepository.getAllWeightEntries(), // Flow<List<WeightEntry>>
        _currentFilter,
        _currentSortOption,
        _uiState
    ) { rawMeasurements, rawWeightEntries, currentFilter, currentSortOption, currentUiInternalState ->

        // Prepare data for list display
        val preliminaryList = mutableListOf<HistoryListItem>()
        rawMeasurements.forEach { preliminaryList.add(HistoryListItem.MeasurementItem(it)) }
        rawWeightEntries.forEach { preliminaryList.add(HistoryListItem.WeightItem(it)) }

        val filteredList = when (currentFilter) {
            HistoryFilterOption.ALL -> preliminaryList
            HistoryFilterOption.BODY_FAT -> preliminaryList.filterIsInstance<HistoryListItem.MeasurementItem>()
            HistoryFilterOption.WEIGHT -> preliminaryList.filterIsInstance<HistoryListItem.WeightItem>()
        }

        val sortedList = if (currentSortOption == HistorySortOption.NEWEST_FIRST) {
            filteredList.sortedByDescending { it.timestamp }
        } else {
            filteredList.sortedBy { it.timestamp }
        }
        val groupedItems = groupHistoryItemsByDate(sortedList)

        // Prepare data for Body Fat Chart (for ProgressChart)
        val bodyFatDataForChart = rawMeasurements
            .sortedBy { it.timeStamp }
            .map { Pair(it.timeStamp, it.percentage.toFloat()) }
        // ProgressChart's default minPointsToShowChart is 3, but 2 is the minimum for a line.
        val finalBodyFatChartEntries = if (bodyFatDataForChart.size >= 2) bodyFatDataForChart else emptyList()

        // Prepare data for Weight Chart (for ProgressChart)
        val weightDataForChart = rawWeightEntries
            .sortedBy { it.timeStamp }
            .map { Pair(it.timeStamp, it.weight.toFloat()) }
        val finalWeightChartEntries = if (weightDataForChart.size >= 2) weightDataForChart else emptyList()

        currentUiInternalState.copy(
            isLoading = false,
            historyItems = sortedList,
            groupedHistoryItems = groupedItems,
            selectedFilter = currentFilter,
            selectedSortOption = currentSortOption,
            bodyFatChartEntries = finalBodyFatChartEntries, // Updated
            weightChartEntries = finalWeightChartEntries    // Updated
            // itemPendingDelete, showConfirmDeleteDialog, and error are preserved from currentUiInternalState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _uiState.value
    )

    fun setFilter(filter: HistoryFilterOption) {
        _currentFilter.value = filter
    }

    fun setSortOption(sortOption: HistorySortOption) {
        _currentSortOption.value = sortOption
    }

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

    fun requestDeleteConfirmation(item: HistoryListItem) {
        _uiState.update { currentState ->
            currentState.copy(itemPendingDelete = item, showConfirmDeleteDialog = true, isLoading = false)
        }
    }

    fun confirmPendingDelete() {
        viewModelScope.launch {
            val itemToDelete = _uiState.value.itemPendingDelete
            var errorOccurred = false
            var errorMessage: String? = null

            if (itemToDelete != null) {
                try {
                    when (itemToDelete) {
                        is HistoryListItem.MeasurementItem -> {
                            bodyFatInfoRepository.deleteMeasurementById(itemToDelete.measurement.id)
                        }
                        is HistoryListItem.WeightItem -> {
                            weightEntryRepository.deleteWeightEntryById(itemToDelete.entry.id)
                        }
                    }
                } catch (e: Exception) {
                    errorOccurred = true
                    errorMessage = "Failed to delete item: ${e.message}"
                }
            }
            _uiState.update { currentState ->
                currentState.copy(
                    itemPendingDelete = null,
                    showConfirmDeleteDialog = false,
                    error = if (errorOccurred) errorMessage else currentState.error,
                    isLoading = false
                )
            }
        }
    }

    fun cancelDeleteConfirmation() {
        _uiState.update { currentState ->
            currentState.copy(itemPendingDelete = null, showConfirmDeleteDialog = false, isLoading = false, error = null)
        }
    }
}
