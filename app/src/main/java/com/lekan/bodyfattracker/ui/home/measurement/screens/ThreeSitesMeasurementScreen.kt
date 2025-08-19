package com.lekan.bodyfattracker.ui.home.measurement.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.model.BodyFatMeasurement
import com.lekan.bodyfattracker.ui.home.measurement.components.ThreeSitesMeasureInput
import com.lekan.bodyfattracker.ui.home.measurement.viewmodels.ThreeSiteMeasurementViewModel
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme


@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar if used
@Composable
fun ThreeSitesMeasurementScreen(
    onBackPressed: () -> Unit, // Kept if you navigate to this screen
    canSave: Boolean = false,
    viewModel: ThreeSiteMeasurementViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.start(canSave)
    }
    // Removed LaunchedEffect for viewModel.start(canSave) as it seems specific
    // to your original structure. If you need it, add it back.

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
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            } else {
                // Your existing ThreeSitesMeasureInput component
                ThreeSitesMeasureInput(
                    age = uiState.age,
                    onAgeChanged = viewModel::onAgeChanged,
                    selectedGender = uiState.selectedGender,
                    onGenderSelected = viewModel::onGenderSelected,
                    chestSkinfold = uiState.skinfold1, // Maps to Triceps for female
                    onChestSkinfoldChanged = viewModel::onSkinfold1Changed,
                    abdomenSkinfold = uiState.skinfold2, // Maps to Suprailiac for female
                    onAbdomenSkinfoldChanged = viewModel::onSkinfold2Changed,
                    thighSkinfold = uiState.skinfold3,
                    onThighSkinfoldChanged = viewModel::onSkinfold3Changed,
                    onCalculateClicked = viewModel::calculateBodyFat,
                    onResetClicked = {
                        viewModel.resetFormAndResult() // This will also hide the sheet if open
                    },
                    // modifier = Modifier.fillMaxSize() // Already handled by parent Box
                )
            }

            // Result Bottom Sheet Overlay
            AnimatedVisibility(
                visible = uiState.isShowingResult && uiState.calculationResult != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(enabled = false, onClick = {}),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    uiState.calculationResult?.let { result ->
                        CalculationResultSheet(
                            result = result,
                            onRecalculate = {
                                viewModel.resetFormAndResult()
                            },
                            onClose = {
                                viewModel.closeResultForm()
                            },
                            accuracy = stringResource(R.string.three_sites_accuracy)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "3-Site Screen - Input View")
@Composable
fun ThreeSitesMeasurementScreenInputPreview() {
    BodyFatTrackerTheme {
        ThreeSitesMeasurementScreen(onBackPressed = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "3-Site Screen with Result Sheet")
@Composable
fun ThreeSitesMeasurementScreenWithSheetPreview() {
    BodyFatTrackerTheme {
        // To truly preview this, you'd need a mock ViewModel that sets
        // isShowingResult = true and a calculationResult.
        // For simplicity, we can show the scaffold and an idea of the sheet.
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("3-Site Measurement") },
                        navigationIcon = {
                            IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "")}
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Input form (ThreeSitesMeasureInput) would be here")
                }
            }


            // Simulate the translucent background & sheet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.BottomCenter
            ) {
//                CalculationResultSheet(
//                    result = BodyFatMeasurement(
//                        percentage = 15.0,
////                        date = "15/07/2023 - 11:30",
//                        timeStamp = System.currentTimeMillis(),
//                        type = BodyFatMeasurement.Type.THREE_POINTS // Use correct type
//                    ),
//                    onRecalculate = {},
//                    onClose = {}
//                )
            }
        }
    }
}
