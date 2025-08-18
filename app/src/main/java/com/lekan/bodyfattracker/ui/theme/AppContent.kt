package com.lekan.bodyfattracker.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lekan.bodyfattracker.ui.addweight.AddWeightEntryScreen
import com.lekan.bodyfattracker.ui.history.HistoryView
import com.lekan.bodyfattracker.ui.home.HomeScreen
import com.lekan.bodyfattracker.ui.home.measurement.screens.SevenSitesMeasurementScreen
import com.lekan.bodyfattracker.ui.home.measurement.screens.ThreeSitesMeasurementScreen
import com.lekan.bodyfattracker.ui.profile.ProfileScreen

// Assume you will create these screens and their Screen objects are now defined:
// import com.lekan.bodyfattracker.ui.addmeasurement.AddMeasurementScreen
// import com.lekan.bodyfattracker.ui.addweight.AddWeightEntryScreen

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppContent(
    navigationViewModel: NavViewModel = hiltViewModel(),
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val navItems = listOf(Screen.Home, Screen.History, Screen.Profile)
    val current = navigationViewModel.backStack.last()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BFCNavigationBar(navigationViewModel = navigationViewModel, navItems = navItems)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavDisplay(
                backStack = navigationViewModel.backStack,
                modifier = modifier,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                entryDecorators = listOf(
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<Screen.Home> {  // navKey can be used if HomeScreen needed its own Screen object
                        HomeScreen(
                            onStartSevenSites = {
                                navigationViewModel.push(Screen.SevenSitesMeasurement(canSave = true))
                            },
                            onStartThreeSites = {
                                navigationViewModel.push(Screen.ThreeSitesMeasurement(canSave = true))
                            },
                            onStartThreeSitesGuest = {
                                navigationViewModel.push(Screen.ThreeSitesMeasurement(canSave = false))
                            },
                            onStartSevenSitesGuest = {
                                navigationViewModel.push(Screen.SevenSitesMeasurement(canSave = false))
                            },
                            onNavigateToAddMeasurement = {
                                navigationViewModel.push(Screen.AddMeasurementScreen)
                            },
                            onNavigateToAddWeightEntry = {
                                navigationViewModel.push(Screen.AddWeightEntryScreen)
                            }
                        )
                    }
                    entry<Screen.History> { HistoryView() }
                    entry<Screen.Profile> { ProfileScreen() }
                    entry<Screen.ThreeSitesMeasurement> { navKey ->
                        ThreeSitesMeasurementScreen(
                            onBackPressed = { navigationViewModel.popLast() },
                            canSave = navKey.canSave
                        )
                    }
                    entry<Screen.SevenSitesMeasurement> { navKey ->
                        SevenSitesMeasurementScreen(
                            onBackPressed = { navigationViewModel.popLast() },
                            canSave = navKey.canSave
                        )
                    }

                    // Entries for the new screens using the defined Screen objects
                    entry<Screen.AddMeasurementScreen> { navKey ->
                        // TODO: Replace with your actual AddMeasurementScreen Composable
                        // Example: AddMeasurementScreen(navKey = navKey, onNavigateUp = { navigationViewModel.popLast() })
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Placeholder for Add Measurement Screen (${navKey.label})")
                        }
                    }

                    entry<Screen.AddWeightEntryScreen> { navKey ->
                        // TODO: Replace with your actual AddWeightEntryScreen Composable
                        // Example: AddWeightEntryScreen(navKey = navKey, onNavigateUp = { navigationViewModel.popLast() })
                        AddWeightEntryScreen(
                            onNavigateUp = { navigationViewModel.popLast() }
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun BFCNavigationBar(
    navigationViewModel: NavViewModel,
    navItems: List<Screen>
) {
    NavigationBar {
        navItems.forEach {
            val currentScreenKey = navigationViewModel.backStack.last().route
            NavigationBarItem(
                icon = { it.icon?.let { icon -> androidx.compose.material3.Icon(painter = rememberVectorPainter(image = icon), contentDescription = null) } },
                label = { Text(it.label) },
                selected = currentScreenKey == it.route,
                onClick = {
                    if (currentScreenKey != it.route) {
                        navigationViewModel.push(it)
                    }
                }
            )
        }
    }
}
