package com.lekan.bodyfattracker.ui.history

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
import androidx.compose.foundation.lazy.items // Ensure this is the correct items import
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api // Added for SwipeToDismissBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox // Added
import androidx.compose.material3.SwipeToDismissBoxState // Added
import androidx.compose.material3.SwipeToDismissBoxValue // Added
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState // Added
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
import com.lekan.bodyfattracker.model.WeightEntry
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
) // Added ExperimentalMaterial3Api
@Composable
fun HistoryView(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HistoryViewContent(
        uiState = uiState,
        onDeleteMeasurement = { viewModel.deleteMeasurement(it) },
        onDeleteWeightEntry = { viewModel.deleteWeightEntry(it) }
    )
}

@Composable
fun HistoryViewContent(
    uiState: HistoryUiState,
    onDeleteMeasurement: (Long) -> Unit,
    onDeleteWeightEntry: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.your_measurement_history),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) { // Added weight(1f) to Box
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.groupedHistoryItems.isEmpty()) {
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
                    contentPadding = PaddingValues(vertical = 8.dp) // Adjusted padding, horizontal on sticky header/items
                ) {
                    uiState.groupedHistoryItems.entries.toList().forEach { dateGroup ->
                        stickyHeader {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.97f))
                                    .padding(horizontal = 16.dp) // Horizontal padding for sticky header
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
                            // contentType = { item -> item.javaClass } // Optional: for performance
                        ) { historyItem ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        when (historyItem) {
                                            is HistoryListItem.MeasurementItem -> {
                                                onDeleteMeasurement(historyItem.measurement.id)
                                                true
                                            }

                                            is HistoryListItem.WeightItem -> {
                                                onDeleteWeightEntry(historyItem.entry.id)
                                                true
                                            }
                                        }
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false, // Only swipe from right to left
                                backgroundContent = { SwipeBackground(dismissState = dismissState) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp) // Horizontal padding for items
                                    .animateItem()
                            ) {
                                // Column wrapper for consistent padding, as cards might have their own.
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    when (historyItem) {
                                        is HistoryListItem.MeasurementItem -> MeasurementHistoryCard(
                                            measurement = historyItem.measurement,
                                            onDelete = { onDeleteMeasurement(historyItem.measurement.id) }
                                        )

                                        is HistoryListItem.WeightItem -> WeightHistoryCard(
                                            entry = historyItem.entry,
                                            onDelete = { onDeleteWeightEntry(historyItem.entry.id) }
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
    val color =
        when (dismissState.targetValue) { // Use targetValue for a more responsive background
            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
            else -> Color.Transparent
        }
    val alignment = Alignment.CenterEnd

    Box(
        Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp), // Padding for the icon inside background
        contentAlignment = alignment
    ) {
        if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) { // Show icon only when swiping to delete
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
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
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
                    text = "Body Fat: ${String.format(Locale.US, "%.1f", measurement.percentage)}%",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Method: ${measurement.method.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Date: $formattedDate",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
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
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
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
                    } ${entry.unit.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (entry.notes?.isNotBlank() == true) {
                    Text(
                        text = "Notes: ${entry.notes}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = "Date: $formattedDate",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete weight entry",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "History View - Empty")
@Composable
fun HistoryViewPreview_Empty() {
    BodyFatTrackerTheme {
        val sampleUiState = HistoryUiState(
            isLoading = false,
            historyItems = emptyList(),
            groupedHistoryItems = emptyMap(),
            error = null
        )
        HistoryViewContent(
            uiState = sampleUiState,
            onDeleteMeasurement = { },
            onDeleteWeightEntry = { }
        )
    }
}

@Preview(showBackground = true, name = "History View - Loading")
@Composable
fun HistoryViewPreview_Loading() {
    BodyFatTrackerTheme {
        val sampleUiState = HistoryUiState(isLoading = true)
        HistoryViewContent(
            uiState = sampleUiState,
            onDeleteMeasurement = { },
            onDeleteWeightEntry = { }
        )
    }
}

