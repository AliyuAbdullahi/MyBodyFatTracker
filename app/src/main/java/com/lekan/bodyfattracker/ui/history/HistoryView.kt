package com.lekan.bodyfattracker.ui.history

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.model.BodyFatMeasurement
import com.lekan.bodyfattracker.model.MeasurementMethod
import com.lekan.bodyfattracker.model.WeightEntry
import com.lekan.bodyfattracker.model.WeightUnit
import com.lekan.bodyfattracker.ui.core.ui.ProgressChart // Added import for ProgressChart
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    HistoryViewContent(
        uiState = uiState,
        onSetFilter = viewModel::setFilter,
        onSetSortOption = viewModel::setSortOption,
        onRequestDeleteConfirmation = viewModel::requestDeleteConfirmation,
        onConfirmPendingDelete = viewModel::confirmPendingDelete,
        onCancelDeleteConfirmation = viewModel::cancelDeleteConfirmation
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryViewContent(
    uiState: HistoryUiState,
    onSetFilter: (HistoryFilterOption) -> Unit,
    onSetSortOption: (HistorySortOption) -> Unit,
    onRequestDeleteConfirmation: (HistoryListItem) -> Unit,
    onConfirmPendingDelete: () -> Unit,
    onCancelDeleteConfirmation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.your_measurement_history),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // Charts Section
        if (uiState.bodyFatChartEntries.isNotEmpty() || uiState.weightChartEntries.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .animateContentSize(), // Animate appearance/disappearance of chart section
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.bodyFatChartEntries.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.body_fat_trend_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    ProgressChart(
                        entries = uiState.bodyFatChartEntries,
                        yAxisTitle = "%",
                        lineColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp) // Adjusted height
                    )
                }

                if (uiState.weightChartEntries.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.weight_trend_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(
                            top = if (uiState.bodyFatChartEntries.isNotEmpty()) 8.dp else 0.dp,
                            bottom = 4.dp
                        )
                    )
                    ProgressChart(
                        entries = uiState.weightChartEntries,
                        yAxisTitle = "Weight", // Using a generic title for now
                        lineColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp) // Adjusted height
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HistoryFilterOption.entries.forEach { filterOption ->
                FilterChip(
                    selected = uiState.selectedFilter == filterOption,
                    onClick = { onSetFilter(filterOption) },
                    label = {
                        Text(
                            when (filterOption) {
                                HistoryFilterOption.ALL -> stringResource(R.string.filter_all)
                                HistoryFilterOption.BODY_FAT -> stringResource(R.string.filter_body_fat)
                                HistoryFilterOption.WEIGHT -> stringResource(R.string.filter_weight)
                            }
                        )
                    },
                    leadingIcon = if (uiState.selectedFilter == filterOption) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = stringResource(R.string.filter_selected_icon_desc),
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                val newSortOption =
                    if (uiState.selectedSortOption == HistorySortOption.NEWEST_FIRST) {
                        HistorySortOption.OLDEST_FIRST
                    } else {
                        HistorySortOption.NEWEST_FIRST
                    }
                onSetSortOption(newSortOption)
            }) {
                Icon(
                    imageVector = if (uiState.selectedSortOption == HistorySortOption.NEWEST_FIRST) {
                        Icons.Filled.ArrowDownward
                    } else {
                        Icons.Filled.ArrowUpward
                    },
                    contentDescription = stringResource(R.string.sort_action_desc)
                )
            }
        }

        if (uiState.showConfirmDeleteDialog) {
            AlertDialog(
                onDismissRequest = { onCancelDeleteConfirmation() },
                title = { Text(text = stringResource(R.string.confirm_delete_title)) },
                text = {
                    val itemDescription = when (val item = uiState.itemPendingDelete) {
                        is HistoryListItem.MeasurementItem ->
                            stringResource(
                                R.string.confirm_delete_measurement_message_format,
                                String.format(Locale.US, "%.1f%%", item.measurement.percentage)
                            )

                        is HistoryListItem.WeightItem ->
                            stringResource(
                                R.string.confirm_delete_weight_message_format,
                                String.format(Locale.US, "%.1f", item.entry.weight),
                                item.entry.unit.name.lowercase(Locale.getDefault())
                            )

                        null -> stringResource(R.string.confirm_delete_generic_message) // Fallback
                        else -> stringResource(R.string.confirm_delete_generic_message) // Fallback for unknown types
                    }
                    Text(
                        text = stringResource(
                            R.string.confirm_delete_are_you_sure,
                            itemDescription
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onConfirmPendingDelete() }) {
                        Text(stringResource(R.string.action_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onCancelDeleteConfirmation() }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        Box(modifier = Modifier
            .weight(1f)
            .fillMaxSize()) {
            if (uiState.isLoading && !uiState.showConfirmDeleteDialog) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.groupedHistoryItems.isEmpty() && !uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditNote,
                        contentDescription = stringResource(R.string.no_history_icon_description),
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.no_history_yet_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.start_tracking_invitation),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    uiState.groupedHistoryItems.entries.toList().forEach { dateGroup ->
                        stickyHeader {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.97f))
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = dateGroup.key,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                )
                                HorizontalDivider()
                            }
                        }
                        items(
                            items = dateGroup.value,
                            key = { item ->
                                when (item) {
                                    is HistoryListItem.MeasurementItem -> "bf-${item.measurement.id}"
                                    is HistoryListItem.WeightItem -> "wt-${item.entry.id}"
                                }
                            },
                        ) { historyItem ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        onRequestDeleteConfirmation(historyItem)
                                        false // Item will snap back, dialog will show
                                    } else {
                                        false
                                    }
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = { SwipeBackground(dismissState = dismissState) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .animateItem() // Correct animation modifier
                            ) {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    when (historyItem) {
                                        is HistoryListItem.MeasurementItem -> MeasurementHistoryCard(
                                            measurement = historyItem.measurement,
                                            onDelete = { onRequestDeleteConfirmation(historyItem) }
                                        )

                                        is HistoryListItem.WeightItem -> WeightHistoryCard(
                                            entry = historyItem.entry,
                                            onDelete = { onRequestDeleteConfirmation(historyItem) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.targetValue) {
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
        else -> Color.Transparent
    }
    val alignment = Alignment.CenterEnd
    Box(
        Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_action),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun MeasurementHistoryCard(
    measurement: BodyFatMeasurement,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(measurement.timeStamp))
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Percent,
                contentDescription = "Body Fat Measurement",
                modifier = Modifier.padding(end = 12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Body Fat: ${
                        String.format(
                            Locale.US,
                            "%.1f",
                            measurement.percentage
                        )
                    }%", style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Method: ${measurement.method.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Date: $formattedDate", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete measurement",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun WeightHistoryCard(
    entry: WeightEntry,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(entry.timeStamp))
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = "Weight Entry",
                modifier = Modifier.padding(end = 12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Weight: ${
                        String.format(
                            Locale.US,
                            "%.1f",
                            entry.weight
                        )
                    } ${entry.unit.name}", style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (entry.notes?.isNotBlank() == true) {
                    Text(
                        text = "Notes: ${entry.notes}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(text = "Date: $formattedDate", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete weight entry",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "History - Populated (Charts & Dialog Hidden)")
@Composable
fun HistoryViewPreview_Populated_Charts_DialogHidden() {
    HistoryViewPreview_Populated_WithChartsAndDialogState(
        showDialog = false,
        itemIsMeasurement = true
    )
}

@Preview(showBackground = true, name = "History - Populated (Charts & Dialog Shown - Measurement)")
@Composable
fun HistoryViewPreview_Populated_Charts_DialogShown_Measurement() {
    HistoryViewPreview_Populated_WithChartsAndDialogState(
        showDialog = true,
        itemIsMeasurement = true
    )
}

@Composable
private fun HistoryViewPreview_Populated_WithChartsAndDialogState(
    showDialog: Boolean,
    itemIsMeasurement: Boolean
) {
    BodyFatTrackerTheme {
        val sampleTime = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L

        val measurement1 =
            BodyFatMeasurement(1L,sampleTime - 5 * dayMillis, 10.5, MeasurementMethod.SEVEN_POINTS, )
        val measurement2 = BodyFatMeasurement(
            2L,
            sampleTime - 3 * dayMillis,
            10.2,
            MeasurementMethod.THREE_POINTS,
        )
        val measurement3 = BodyFatMeasurement(
            3L,
            sampleTime - 1 * dayMillis,
            10.0,
            MeasurementMethod.SEVEN_POINTS,
        )
        val sampleBfItems = listOf(measurement1, measurement2, measurement3).map {
            HistoryListItem.MeasurementItem(it)
        }
        val sampleBfChartData = sampleBfItems.map {
            Pair(
                it.measurement.timeStamp,
                it.measurement.percentage.toFloat()
            )
        }.sortedBy { it.first }

        val weight1 = WeightEntry(1L, 75.5, WeightUnit.KG, sampleTime - 6 * dayMillis, "Start")
        val weight2 = WeightEntry(2L, 75.0, WeightUnit.KG, sampleTime - 4 * dayMillis, null)
        val weight3 = WeightEntry(3L, 74.8, WeightUnit.KG, sampleTime - 2 * dayMillis, "Good")
        val sampleWtItems = listOf(weight1, weight2, weight3).map { HistoryListItem.WeightItem(it) }
        val sampleWtChartData =
            sampleWtItems.map { Pair(it.entry.timeStamp, it.entry.weight.toFloat()) }
                .sortedBy { it.first }

        val allListItems = (sampleBfItems + sampleWtItems).sortedByDescending { it.timestamp }
        val groupedItems =
            allListItems.groupBy { item -> DateUtils.formatHeaderDate(item.timestamp) }

        val itemToDeleteForPreview = if (showDialog) {
            if (itemIsMeasurement) sampleBfItems.first() else sampleWtItems.first()
        } else null

        val sampleUiState = HistoryUiState(
            isLoading = false,
            historyItems = allListItems,
            groupedHistoryItems = groupedItems,
            error = null,
            selectedFilter = HistoryFilterOption.ALL,
            selectedSortOption = HistorySortOption.NEWEST_FIRST,
            itemPendingDelete = itemToDeleteForPreview,
            showConfirmDeleteDialog = showDialog,
            bodyFatChartEntries = sampleBfChartData,
            weightChartEntries = sampleWtChartData
        )
        HistoryViewContent(
            uiState = sampleUiState,
            onSetFilter = {},
            onSetSortOption = {},
            onRequestDeleteConfirmation = {},
            onConfirmPendingDelete = {},
            onCancelDeleteConfirmation = {}
        )
    }
}

object DateUtils {
    fun isToday(timestamp: Long): Boolean = android.text.format.DateUtils.isToday(timestamp)
    fun isYesterday(timestamp: Long): Boolean {
        val yesterday = Calendar.getInstance(); yesterday.add(Calendar.DAY_OF_YEAR, -1)
        val cal = Calendar.getInstance(); cal.timeInMillis = timestamp
        return yesterday.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
    }

    fun formatHeaderDate(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        return when {
            isToday(timestamp) -> "Today"
            isYesterday(timestamp) -> "Yesterday"
            else -> SimpleDateFormat("MMM dd, yyyy", locale).format(Date(timestamp))
        }
    }
}

@Preview(showBackground = true, name = "History View - Empty (No Charts)")
@Composable
fun HistoryViewPreview_Empty() {
    BodyFatTrackerTheme {
        HistoryViewContent(
            uiState = HistoryUiState(
                isLoading = false,
                groupedHistoryItems = emptyMap(),
                bodyFatChartEntries = emptyList(), // Ensure chart data is empty
                weightChartEntries = emptyList()   // Ensure chart data is empty
            ),
            onSetFilter = {},
            onSetSortOption = {},
            onRequestDeleteConfirmation = {},
            onConfirmPendingDelete = {},
            onCancelDeleteConfirmation = {}
        )
    }
}

@Preview(showBackground = true, name = "History View - Loading")
@Composable
fun HistoryViewPreview_Loading() {
    BodyFatTrackerTheme {
        HistoryViewContent(
            uiState = HistoryUiState(
                isLoading = true,
                bodyFatChartEntries = emptyList(),
                weightChartEntries = emptyList()
            ),
            onSetFilter = {},
            onSetSortOption = {},
            onRequestDeleteConfirmation = {},
            onConfirmPendingDelete = {},
            onCancelDeleteConfirmation = {}
        )
    }
}

