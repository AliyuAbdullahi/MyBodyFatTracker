package com.lekan.bodyfattracker.ui.home.measurement.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.model.BodyFatMeasurement
import com.lekan.bodyfattracker.model.MeasurementMethod
import com.lekan.bodyfattracker.ui.home.measurement.components.SevenSitesMeasureInput
import com.lekan.bodyfattracker.ui.home.measurement.viewmodels.SevenSitesMeasurementViewModel
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SevenSitesMeasurementScreen(
    onBackPressed: () -> Unit,
    viewModel: SevenSitesMeasurementViewModel = hiltViewModel(),
    canSave: Boolean = false
) {
    val uiState by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage() // Clear message after showing
        }
    }

    LaunchedEffect(Unit) {
        viewModel.start(canSave)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { // Optional: if you want a consistent TopAppBar
            TopAppBar(
                title = {  }, // Define this string
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back) // Define this string
                        )
                    }
                },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.onSurface, // Color for title on white background
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface, // Color for nav icon
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface // Color for action icons
                ),
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }  else {
                SevenSitesMeasureInput(
                    age = uiState.age,
                    onAgeChanged = viewModel::onAgeChanged,
                    selectedGender = uiState.selectedGender,
                    onGenderSelected = viewModel::onGenderSelected,
                    chestSkinfold = uiState.chest,
                    onChestSkinfoldChanged = viewModel::onChestChanged,
                    midaxillarySkinfold = uiState.midaxillary,
                    onMidaxillarySkinfoldChanged = viewModel::onMidaxillaryChanged,
                    tricepsSkinfold = uiState.triceps,
                    onTricepsSkinfoldChanged = viewModel::onTricepsChanged,
                    subscapularSkinfold = uiState.subscapular,
                    onSubscapularSkinfoldChanged = viewModel::onSubscapularChanged,
                    abdomenSkinfold = uiState.abdomen,
                    onAbdomenSkinfoldChanged = viewModel::onAbdomenChanged,
                    suprailiacSkinfold = uiState.suprailiac,
                    onSuprailiacSkinfoldChanged = viewModel::onSuprailiacChanged,
                    thighSkinfold = uiState.thigh,
                    onThighSkinfoldChanged = viewModel::onThighChanged,
                    onCalculateClicked = viewModel::calculateBodyFat,
                    onResetClicked = viewModel::resetFormAndResult,
                    modifier = Modifier.fillMaxSize() // The input itself handles scrolling
                )
            }
            uiState.calculationResult?.let {
                CalculationResultSheet(
                    result = it,
                    onRecalculate = { viewModel.resetFormAndResult() },
                    onClose = {
                        viewModel.closeResultForm()
                    },
                    accuracy = stringResource(R.string.seven_sites_accuracy)
                )
            }
        }
    }
}

// You might want to extract this to a common component if it's identical
// to the one used in ThreeSitesMeasurementScreen
@Composable
fun CalculationResultSheet(
    result: BodyFatMeasurement,
    accuracy: String? = null,
    modifier: Modifier = Modifier,
    onRecalculate: () -> Unit,
    onClose: () -> Unit,
) {

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6F))
    ) {
        // Removed BoxWithConstraints, Column is now the direct root of the sheet's content
        val density = LocalDensity.current

        val height: Dp = with(density) {
            // Extension function on Density to convert pixels (Float) to Dp
            (constraints.maxHeight * 0.7).toInt().toDp()
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(height)
                // .height(constraints) // If you want it to wrap content height,
                // but fillMaxWidth might make this tricky without more structure.
                // Often, you'd let the content define the height naturally.
                // .fillMaxHeight(0.6f) // If you wanted a fixed percentage of the *outer* Box (the one with
                // translucent background), this Column needs to be inside a Box
                // that itself is constrained or sized.
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close Button
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.close_button_description)
                    )
                }
            }

            // Caliper Image
            Image(
                painter = painterResource(id = R.drawable.caliper),
                contentDescription = stringResource(R.string.caliper_image_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.body_fat_percentage_result_label),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
            val formattedDate = sdf.format(Date(result.timeStamp))
            Text(
                text = stringResource(R.string.calculation_date_label, formattedDate),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${String.format(locale = Locale.getDefault(), "%.1f", result.percentage)}%",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(
                    R.string.measurement_type_label,
                    result.method.name.replace("_", " ")
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getType(result.method),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            accuracy?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.accuracy_label, it),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 10.sp
                    )
                    Text(text = it)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRecalculate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.recalculate_button_label))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun getType(type: MeasurementMethod): String = when (type) {
    MeasurementMethod.SEVEN_POINTS -> stringResource(R.string.measuring_type_seven_points)
    MeasurementMethod.THREE_POINTS -> stringResource(R.string.measuring_type_three_points)
    else -> "Other"
}
//
//@Preview(showBackground = true, name = "Calculation Result Sheet - 3 Points")
//@Composable
//fun CalculationResultSheetPreviewThreePoints() {
//    BodyFatTrackerTheme { // Apply your app's theme
//        Surface( // Surface provides a background color from the theme
//            modifier = Modifier
//                .fillMaxWidth()  // Add some padding around the sheet in the preview
//        ) {
//            CalculationResultSheet(
//                result = BodyFatMeasurement(
//                    percentage = 18,
//                    date = "15/07/2023 - 10:30",
//                    timeStamp = System.currentTimeMillis(),
//                    type = BodyFatMeasurement.Type.THREE_POINTS // Example with 3-points
//                ),
//                onRecalculate = { /* Preview: No action needed */ },
//                onClose = { /* Preview: No action needed */ },
//                accuracy = stringResource(R.string.seven_sites_accuracy)
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true, name = "Calculation Result Sheet - 7 Points")
//@Composable
//fun CalculationResultSheetPreviewSevenPoints() {
//    BodyFatTrackerTheme {
//        Surface(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            CalculationResultSheet(
//                result = BodyFatMeasurement(
//                    percentage = 22,
//                    date = "16/07/2023 - 14:45",
//                    timeStamp = System.currentTimeMillis(),
//                    type = BodyFatMeasurement.Type.SEVEN_POINTS // Example with 7-points
//                ),
//                onRecalculate = {},
//                onClose = {}
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true, name = "Calculation Result Sheet - Higher Percentage")
//@Composable
//fun CalculationResultSheetPreviewHighPercentage() {
//    BodyFatTrackerTheme {
//        Surface(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            CalculationResultSheet(
//                result = BodyFatMeasurement(
//                    percentage = 35,
//                    date = "17/07/2023 - 09:00",
//                    timeStamp = System.currentTimeMillis(),
//                    type = BodyFatMeasurement.Type.SEVEN_POINTS
//                ),
//                onRecalculate = {},
//                onClose = {}
//            )
//        }
//    }
//}



