package com.lekan.bodyfattracker.ui.history

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.model.BodyFatInfo
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryView(
     viewModel: HistoryViewModel = hiltViewModel() // Inject your ViewModel
) {
    val uiState by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.recordedMeasurements.isEmpty() -> {
                EmptyHistoryView()
            }
            else -> {
                HistoryContentView(measurements = uiState.recordedMeasurements)
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.start()
    }
}

@Composable
fun EmptyHistoryView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Warning, // Or your custom empty state icon
                contentDescription = stringResource(R.string.empty_history_icon_description),
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_recordings_saved),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun HistoryContentView(measurements: List<BodyFatInfo>) {
    LazyColumn( // Use LazyColumn for efficient list display
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        if (measurements.size > 3) {
            item {
                Text(
                    text = stringResource(R.string.progress_over_time_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                )
                BodyFatProgressGraph(measurements = measurements)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Text(
                text = stringResource(R.string.recorded_measurements_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
            )
        }

        items(measurements.sortedByDescending { it.timeStamp }) { measurement -> // Display newest first
            HistoryListItem(measurement = measurement)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
@Composable
fun BodyFatProgressGraph(measurements: List<BodyFatInfo>, modifier: Modifier = Modifier) {
    if (measurements.isEmpty()) return

    val sortedMeasurements = measurements.sortedBy { it.timeStamp }
    val points = sortedMeasurements.map { it.percentage.toFloat() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Define colors from MaterialTheme for better theming
        val primaryColor = MaterialTheme.colorScheme.primary
        val mapStartColor = primaryColor.copy(alpha = 0.4f)
        val mapEndColor = primaryColor.copy(alpha = 0.05f)
        val textColor =
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) // Color for axis labels

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 28.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) { // Added more start padding for labels
            if (points.size < 2) {
                // Optionally draw a single point or a message like "Not enough data for graph"
                val textPaint = Paint().apply {
                    color = textColor.toArgb() // Use Android Graphics Color for native drawText
                    textSize = 12.sp.toPx()
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "Not enough data for graph",
                    size.width / 2,
                    size.height / 2,
                    textPaint
                )
                return@Canvas
            }

            val path = Path()
            val fillPath = Path()

            val yMax = points.maxOrNull() ?: 0f
            val yMin = points.minOrNull() ?: 0f
            val yRange = if ((yMax - yMin) == 0f) 1f else (yMax - yMin)

            val xStep = size.width / (points.size - 1)

            // Start path for line and fill
            var currentX = 0f
            var currentY = size.height - ((points.first() - yMin) / yRange * size.height)
            if (currentY.isNaN() || currentY.isInfinite()) currentY = size.height / 2f
            path.moveTo(currentX, currentY)
            fillPath.moveTo(currentX, size.height)
            fillPath.lineTo(currentX, currentY)

            points.forEachIndexed { index, point ->
                if (index > 0) {
                    currentX = index * xStep
                    currentY = size.height - ((point - yMin) / yRange * size.height)
                    if (currentY.isNaN() || currentY.isInfinite()) currentY = size.height / 2f
                    path.lineTo(currentX, currentY)
                    fillPath.lineTo(currentX, currentY)
                }
            }

            fillPath.lineTo(currentX, size.height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(colors = listOf(mapStartColor, mapEndColor))
            )
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx())
            )

            // --- Draw Min/Max Y-axis labels ---
            val textPaint = Paint().apply {
                color = textColor.toArgb() // Convert Compose Color to Android Color int
                textSize = 12.sp.toPx() // Convert Dp to Px for text size
                textAlign =
                    Paint.Align.RIGHT // Align text to the right of the specified x-coordinate
            }

            // Draw Max Value (top of the graph content area)
            // Y position for max text is at the top of the graph (y=0 in canvas terms for graph content)
            // Add a small offset so it's not cut off.
            val maxTextY = 10.sp.toPx() // Offset from the very top
            drawContext.canvas.nativeCanvas.drawText(
                "${yMax.toInt()}%", // Display as integer percentage
                -4.dp.toPx(), // X position (slightly to the left of the graph line, hence negative)
                maxTextY, // Y position
                textPaint
            )

            // Draw Min Value (bottom of the graph content area)
            // Y position for min text is at the bottom of the graph (y=size.height for graph content)
            // Subtract a small offset so it's not cut off.
            val minTextY = size.height - 2.sp.toPx()
            drawContext.canvas.nativeCanvas.drawText(
                "${yMin.toInt()}%", // Display as integer percentage
                -4.dp.toPx(), // X position
                minTextY, // Y position
                textPaint
            )
        }
    }
}


@Composable
fun HistoryListItem(measurement: BodyFatInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image (Placeholder - use appropriate icon based on measurement type or a generic one)
            Image(
                //painter = painterResource(id = R.drawable.ic_measurement_type_generic), // Replace with actual logic/drawable
                painter = painterResource(R.drawable.caliper), // Example
                contentDescription = stringResource(R.string.measurement_type_icon_description, measurement.type.name),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.body_fat_percentage_label_prefix, measurement.percentage),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.measurement_method_label_prefix, measurement.type.name.replace("_", " ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.date_label_prefix, measurement.date),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// --- Dummy Data for Preview ---
fun generateDummyData(count: Int): List<BodyFatInfo> {
    if (count == 0) return emptyList()
    val list = mutableListOf<BodyFatInfo>()
    val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
    val types = BodyFatInfo.Type.values()
    for (i in 0 until count) {
        val timeStamp = System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000L) // Decreasing timestamps
        list.add(
            BodyFatInfo(
                percentage = (10..35).random(),
                date = sdf.format(Date(timeStamp)),
                timeStamp = timeStamp,
                type = types[i % types.size]
            )
        )
    }
    return list
}


// --- Previews ---
@Preview(showBackground = true, name = "History View - Empty State")
@Composable
fun HistoryViewEmptyPreview() {
    BodyFatTrackerTheme {
        EmptyHistoryView()
    }
}

@Preview(showBackground = true, name = "History View - With Data (Less than 3)")
@Composable
fun HistoryViewWithDataFewPreview() {
    BodyFatTrackerTheme {
        HistoryContentView(measurements = generateDummyData(2))
    }
}

@Preview(showBackground = true, name = "History View - With Data (More than 3)")
@Composable
fun HistoryViewWithDataManyPreview() {
    BodyFatTrackerTheme {
        HistoryContentView(measurements = generateDummyData(5))
    }
}

@Preview(showBackground = true, name = "History List Item Preview")
@Composable
fun HistoryListItemPreview() {
    BodyFatTrackerTheme {
        HistoryListItem(
            measurement = BodyFatInfo(
                percentage = 22,
                date = "15/07/2023 - 10:30",
                timeStamp = System.currentTimeMillis(),
                type = BodyFatInfo.Type.SEVEN_POINTS
            )
        )
    }
}

@Preview(showBackground = true, name = "History Graph Preview")
@Composable
fun HistoryGraphPreview() {
    BodyFatTrackerTheme {
        BodyFatProgressGraph(measurements = generateDummyData(10))
    }
}
