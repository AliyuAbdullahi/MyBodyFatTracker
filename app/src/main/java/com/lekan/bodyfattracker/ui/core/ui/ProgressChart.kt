package com.lekan.bodyfattracker.ui.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider.Companion.verticalGradient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressChart(
    modifier: Modifier = Modifier,
    entries: List<Pair<Long, Float>>, // List of (timestamp, value)
    lineColor: Color = MaterialTheme.colorScheme.primary,
    yAxisTitle: String,
    minPointsToShowChart: Int = 3,
    emptyChartContent: @Composable () -> Unit = {
        Text(
            text = "Not enough data to display chart.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
) {
    if (entries.size < minPointsToShowChart) {
        emptyChartContent()
    } else {
        val modelProducer = remember { CartesianChartModelProducer() }

        LaunchedEffect(entries) {
            modelProducer.runTransaction {
                // Data for Vico: needs list of x values and list of y values
                // We'll use the index for x-values internally for the series,
                // but the timestamp for formatting the bottom axis.
                lineSeries {
                    series(entries.map { it.second }) // it.second is the y-value (weight or bodyfat)
                }
            }
        }

        val lineLayer = remember(lineColor) { // Re-remember if lineColor changes
            val lineItselfVicoFill = fill(lineColor)
            val lineItselfFill = LineCartesianLayer.LineFill.single(fill = lineItselfVicoFill)
            val lineStroke = LineCartesianLayer.LineStroke.Continuous(thicknessDp = 2f)
            val gradientComposeBrush = verticalGradient(
                colors = intArrayOf(
                     lineColor.toArgb(),
                     Color.Transparent.toArgb()
                )
            )
            val gradientVicoFill = fill(
                shaderProvider = gradientComposeBrush
            )
            val lineAreaFill = LineCartesianLayer.AreaFill.single(fill = gradientVicoFill)
            val actualLine = LineCartesianLayer.Line(
                fill = lineItselfFill,
                stroke = lineStroke,
                areaFill = lineAreaFill
            )
            val lineProvider = LineCartesianLayer.LineProvider.series(listOf(actualLine))
            LineCartesianLayer(lineProvider = lineProvider)
        }

        CartesianChartHost(
            chart = rememberCartesianChart(
                lineLayer,
                startAxis = VerticalAxis.rememberStart(
                    title = yAxisTitle,
                    tickLength = 0.dp
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = CartesianValueFormatter { context, x, _ ->
                        val index = x.toInt()
                        if (index >= 0 && index < entries.size) {
                            val entryTimestamp = entries[index].first // it.first is the timestamp
                            val entryDate = Date(entryTimestamp)
                            SimpleDateFormat("MMM dd", Locale.getDefault()).format(entryDate)
                        } else {
                            ""
                        }
                    }
                ),
            ),
            modelProducer = modelProducer,
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp) // Default height, can be overridden by modifier
        )
    }
}
    