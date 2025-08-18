package com.lekan.bodyfattracker.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter // Assuming this resolves, or we'll change it
import androidx.compose.material.icons.filled.SquareFoot   // Assuming this resolves, or we'll change it
import androidx.compose.material.icons.filled.Straighten   // Assuming this resolves, or we'll change it
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lekan.bodyfattracker.model.WeightEntry // Required for the new parameter
import com.lekan.bodyfattracker.ui.home.components.LatestMeasurementCard
import com.lekan.bodyfattracker.ui.home.components.LatestWeightEntryCard

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAddMeasurement: () -> Unit,
    onNavigateToAddWeightEntry: () -> Unit,
    onStartSevenSites: () -> Unit,
    onStartThreeSites: () -> Unit,
    onStartThreeSitesGuest: () -> Unit,
    onStartSevenSitesGuest: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()
    val isLoading = uiState.isLoading
    val userProfile = uiState.userProfile
    val latestMeasurement = uiState.latestMeasurement
    val latestWeightEntry = uiState.latestWeightEntry
    val recentWeightEntries = uiState.recentWeightEntries // Extract recent entries

    var isFabExpanded by remember { mutableStateOf(false) }
    val fabRotation by animateFloatAsState(targetValue = if (isFabExpanded) 45f else 0f, label = "fab_rotation")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Body Fat Tracker") },
                windowInsets = WindowInsets(0),)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                onStartThreeSites()
                                isFabExpanded = false
                            },
                            icon = { Icon(Icons.Filled.SquareFoot, "Add 3-Site Measurement") },
                            text = { Text("3-Site") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                onStartSevenSites()
                                isFabExpanded = false
                            },
                            icon = { Icon(Icons.Filled.Straighten, "Add 7-Site Measurement") },
                            text = { Text("7-Site") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                onNavigateToAddWeightEntry()
                                isFabExpanded = false
                            },
                            icon = { Icon(Icons.Filled.FitnessCenter, "Add Weight Entry") },
                            text = { Text("Weight") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                onStartThreeSitesGuest() // New action
                                isFabExpanded = false
                            },
                            icon = { Icon(Icons.Filled.SquareFoot, "Add 3-Site Guest Measurement") },
                            text = { Text("3-Site Guest") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                onStartSevenSitesGuest() // New action
                                isFabExpanded = false
                            },
                            icon = { Icon(Icons.Filled.Straighten, "Add 7-Site Guest Measurement") },
                            text = { Text("7-Site Guest") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isFabExpanded) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = if (isFabExpanded) "Close FAB" else "Open FAB",
                        modifier = Modifier.rotate(fabRotation)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) { // Outer Box for Scrim (if any) and Content
            // Scrim: Drawn first, so it's behind the main content if visible

            // Main content area: Drawn on top of the Scrim (if visible)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply scaffold padding first
                    .padding(horizontal = 16.dp, vertical = 8.dp), // Then specific content padding
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                } else {
                    if (userProfile != null) {
                        Text("Welcome, ${userProfile.name}!")
                    } else {
                        Text("No profile set up yet.")
                    }

                    LatestMeasurementCard(
                        latestMeasurement = latestMeasurement
                    )

                    LatestWeightEntryCard(
                        latestWeightEntry = latestWeightEntry,
                        recentWeightEntries = recentWeightEntries, // Pass the list here
                        onAddWeightEntryClick = onNavigateToAddWeightEntry
                    )
                }
            }
            if (isFabExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.6f)) // Changed to White
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // No ripple effect for scrim click
                            onClick = { isFabExpanded = false }
                        )
                )
            }
        }
    }
}
