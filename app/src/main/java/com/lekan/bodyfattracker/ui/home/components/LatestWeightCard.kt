package com.lekan.bodyfattracker.ui.home.components

// import androidx.compose.ui.graphics.toArgb // No longer needed with new Fill approach

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lekan.bodyfattracker.model.WeightEntry
import com.lekan.bodyfattracker.ui.core.ui.ProgressChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LatestWeightEntryCard(
    modifier: Modifier = Modifier,
    latestWeightEntry: WeightEntry?,
    recentWeightEntries: List<WeightEntry>,
    onAddWeightEntryClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Latest Weight",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (latestWeightEntry != null) {
                val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                val formattedDate = sdf.format(Date(latestWeightEntry.timeStamp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", latestWeightEntry.weight)} ${latestWeightEntry.unit}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (latestWeightEntry.notes?.isNotBlank() == true) {
                        Text(
                            text = "Notes: ${latestWeightEntry.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Recorded on: $formattedDate",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (recentWeightEntries.size >= 3) {
                    val modelProducer = remember { CartesianChartModelProducer() }

                    // User's working data loading mechanism
                    LaunchedEffect(recentWeightEntries) {
                        modelProducer.runTransaction {
                            lineSeries {
                                series(recentWeightEntries.map { it.weight })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Weight Progression", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    val color = MaterialTheme.colorScheme.primary
                    ProgressChart(
                        modifier = Modifier.fillMaxWidth(),
                        entries = recentWeightEntries.map { it.timeStamp to it.weight.toFloat() },
                        lineColor = color,
                        yAxisTitle = "Weight (kg)"
                    )
                }

            } else {
                Text(
                    text = "No weight entries recorded yet.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onAddWeightEntryClick) {
                    Text("ADD WEIGHT ENTRY")
                }
            }
        }
    }
}

