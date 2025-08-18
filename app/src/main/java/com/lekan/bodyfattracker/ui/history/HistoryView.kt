package com.lekan.bodyfattracker.ui.history

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lekan.bodyfattracker.model.BodyFatMeasurement
import com.lekan.bodyfattracker.model.WeightEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryView(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // TODO: Implement confirmation dialog logic here if uiState.showConfirmDeleteDialog is used

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.historyItems.isEmpty()) {
            Text(
                text = "No history yet. Start tracking!",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.historyItems,
                    key = { item ->
                        when (item) {
                            is HistoryListItem.MeasurementItem -> "bf-${item.measurement.id}"
                            is HistoryListItem.WeightItem -> "wt-${item.entry.id}"
                        }
                    }
                ) { historyItem ->
                    when (historyItem) {
                        is HistoryListItem.MeasurementItem -> MeasurementHistoryCard(
                            measurement = historyItem.measurement,
                            onDelete = { viewModel.deleteMeasurement(historyItem.measurement.id) } // Pass delete action
                        )
                        is HistoryListItem.WeightItem -> WeightHistoryCard(
                            entry = historyItem.entry,
                            onDelete = { viewModel.deleteWeightEntry(historyItem.entry.id) } // Pass delete action
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeasurementHistoryCard(
    measurement: BodyFatMeasurement,
    onDelete: () -> Unit // Added callback
) {
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(measurement.timeStamp))

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row( // Use Row to place delete button at the end
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp), // Adjust padding for icon
            verticalAlignment = Alignment.Top // Align content to top
        ) {
            Column(modifier = Modifier.weight(1f)) { // Column takes available space
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
//                if (measurement.siteMeasurements.isNotEmpty()) {
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(
//                        text = "Site Measurements: ${measurement.siteMeasurements.entries.joinToString { "${it.key}: ${it.value}mm" }}",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
            }
            IconButton(
                onClick = onDelete, // Call the passed lambda
                modifier = Modifier.size(40.dp) // Consistent size for the touch target
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
    onDelete: () -> Unit // Added callback
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
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Weight: ${String.format(Locale.US, "%.1f", entry.weight)} ${entry.unit.name}",
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

