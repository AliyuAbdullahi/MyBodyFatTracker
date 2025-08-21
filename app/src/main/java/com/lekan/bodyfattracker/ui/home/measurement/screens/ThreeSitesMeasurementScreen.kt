package com.lekan.bodyfattracker.ui.home.measurement.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.ui.home.Gender
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
    var showSkinfoldInfoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.start(canSave)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.three_site_skinfold_title)) }, // Added title
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
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(R.string.info_icon_description) // Changed description
                        )
                    }
                },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.onPrimary, // Adjusted for themed AppBar
                    containerColor = MaterialTheme.colorScheme.primary, // Themed AppBar
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
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            } else {
                ThreeSitesMeasureInput(
                    age = uiState.age,
                    onAgeChanged = viewModel::onAgeChanged,
                    selectedGender = uiState.selectedGender,
                    onGenderSelected = viewModel::onGenderSelected,
                    chestSkinfold = uiState.skinfold1,
                    onChestSkinfoldChanged = viewModel::onSkinfold1Changed,
                    abdomenSkinfold = uiState.skinfold2,
                    onAbdomenSkinfoldChanged = viewModel::onSkinfold2Changed,
                    thighSkinfold = uiState.skinfold3,
                    onThighSkinfoldChanged = viewModel::onSkinfold3Changed,
                    onCalculateClicked = viewModel::calculateBodyFat,
                    onResetClicked = {
                        viewModel.resetFormAndResult()
                    },
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
                        .clickable { viewModel.closeResultForm() }, // Click on scrim to close
                    contentAlignment = Alignment.BottomCenter
                ) {
                     // Stop click propagation to the sheet itself
                    Box(modifier = Modifier.clickable(enabled = false, onClick = {})) {
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

            // Info Bottom Sheet Overlay
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
                    Box(modifier = Modifier.clickable(enabled = false, onClick = {})) {
                        SkinfoldInfoSheet(
                            selectedGender = uiState.selectedGender,
                            onClose = { showSkinfoldInfoSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkinfoldInfoSheet(
    selectedGender: Gender,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) // Use surface color for sheet
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Make content scrollable
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.info_sheet_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(stringResource(R.string.info_general_tips_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            Text(stringResource(R.string.info_tip_1), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
            Text(stringResource(R.string.info_tip_2), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
            Text(stringResource(R.string.info_tip_3), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
            Text(stringResource(R.string.info_tip_4), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
            Text(stringResource(R.string.info_tip_5), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))

            if (selectedGender == Gender.MALE) {
                Text(stringResource(R.string.info_male_sites_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                Text(stringResource(R.string.info_male_site_1_chest), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                Text(stringResource(R.string.info_male_site_2_abdomen), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                Text(stringResource(R.string.info_male_site_3_thigh), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
            } else {
                Text(stringResource(R.string.info_female_sites_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                Text(stringResource(R.string.info_female_site_1_triceps), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                Text(stringResource(R.string.info_female_site_2_suprailiac), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                Text(stringResource(R.string.info_female_site_3_thigh), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.close))
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.BottomCenter
            ) {
//                CalculationResultSheet(
//                    result = BodyFatMeasurement(
//                        percentage = 15.0,
//                        timeStamp = System.currentTimeMillis(),
//                        type = BodyFatMeasurement.Type.THREE_POINTS
//                    ),
//                    onRecalculate = {},
//                    onClose = {}
//                )
            }
        }
    }
}

@Preview(showBackground = true, name = "3-Site Info Sheet - Male")
@Composable
fun ThreeSitesInfoSheetMalePreview() {
    BodyFatTrackerTheme {
        SkinfoldInfoSheet(selectedGender = Gender.MALE, onClose = {})
    }
}

@Preview(showBackground = true, name = "3-Site Info Sheet - Female")
@Composable
fun ThreeSitesInfoSheetFemalePreview() {
    BodyFatTrackerTheme {
        SkinfoldInfoSheet(selectedGender = Gender.FEMALE, onClose = {})
    }
}
