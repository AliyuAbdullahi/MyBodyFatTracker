package com.lekan.bodyfattracker.ui.home.measurement.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
// Verify this import if Gender is used in SevenSiteSkinfoldInfoSheet;
// import com.lekan.bodyfattracker.model.Gender 
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
    var showSkinfoldInfoSheet by remember { mutableStateOf(false) }

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
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.seven_site_skinfold_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSkinfoldInfoSheet = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.info_icon_description)
                        )
                    }
                },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
            } else {
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
                    modifier = Modifier.fillMaxSize()
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

            AnimatedVisibility(
                visible = showSkinfoldInfoSheet,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { showSkinfoldInfoSheet = false }, // Click on scrim to close
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Stop click propagation to the sheet itself
                    Box(modifier = Modifier.clickable(enabled = false, onClickLabel = null, role = null, onClick = {} )) {
                        SevenSiteSkinfoldInfoSheet(
                            onClose = { showSkinfoldInfoSheet = false }
                        )
                    }
                }
            }
        }
    }
}

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
            .background(Color.Black.copy(alpha = 0.8F))
            .clickable { onClose() } // Click on scrim to close
    ) {
        val density = LocalDensity.current
        val sheetHeight: Dp = with(density) { (constraints.maxHeight * 0.8).toInt().toDp() }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(sheetHeight)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.surface) // Use surface color from theme
                .clickable(enabled = false, onClickLabel = null, role = null, onClick = {}) // Stop click propagation
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Make sheet content scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                // The original Row for accuracy was removed due to a text conflict.
                // If you want to re-add it, ensure the Text composable is used correctly.
                // For now, only the accuracy value is shown to avoid the conflict.
                 Text(
                     text = stringResource(R.string.accuracy_label, it),
                     style = MaterialTheme.typography.bodyMedium,
                     fontSize = 10.sp
                 )
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
fun SevenSiteSkinfoldInfoSheet(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant) // Or your preferred sheet background
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.seven_sites_info_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(stringResource(R.string.info_general_tips_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.info_tip_1), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.info_tip_2), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.info_tip_3), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.info_tip_4), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.info_tip_5), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.info_seven_sites_list_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.info_site_chest), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.info_site_midaxillary), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.info_site_triceps), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.info_site_subscapular), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.info_site_abdomen), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.info_site_suprailiac), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.info_site_thigh), style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onClose) {
            Text(stringResource(R.string.close))
        }
    }
}

@Composable
fun getType(type: MeasurementMethod): String = when (type) {
    MeasurementMethod.SEVEN_POINTS -> stringResource(R.string.measuring_type_seven_points)
    MeasurementMethod.THREE_POINTS -> stringResource(R.string.measuring_type_three_points)
    else -> "Other"
}

@Preview(showBackground = true, name = "7-Site Info Sheet")
@Composable
fun SevenSitesInfoSheetPreview() {
    BodyFatTrackerTheme {
        SevenSiteSkinfoldInfoSheet(onClose = {})
    }
}

// Keep existing previews for CalculationResultSheet if they are still relevant
// or update them as needed. The original ones are commented out below
// as they might need adjustment based on any changes to CalculationResultSheet.

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
//                    percentage = 22.0f, // Ensure float for percentage
//                    timeStamp = System.currentTimeMillis(),
//                    method = MeasurementMethod.SEVEN_POINTS 
//                ),
//                onRecalculate = {},
//                onClose = {},
//                accuracy = "±3.5%" 
//            )
//        }
//    }
//}