package com.lekan.bodyfattracker.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import coil3.compose.AsyncImage
import com.lekan.bodyfattracker.ui.history.HistoryView
import com.lekan.bodyfattracker.ui.home.HomeScreen
import com.lekan.bodyfattracker.ui.home.measurement.screens.SevenSitesMeasurementScreen
import com.lekan.bodyfattracker.ui.home.measurement.screens.ThreeSitesMeasurementScreen
import com.lekan.bodyfattracker.ui.profile.ProfileScreen

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun App() {
    BodyFatTrackerTheme {
        AppContent()
    }
}

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
            AnimatedVisibility(
                current in navItems,
                enter = slideInVertically(
                    initialOffsetY = { it * 2 },
                ) + fadeIn(tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it * 2 }
                ) + fadeOut(tween(300))
            ) {
                BFCNavigationBar(navigationViewModel, navItems)
            }
        }
    ) { innerPadding -> // Content area of the Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply padding from Scaffold
        ) {
            NavDisplay(
                backStack = navigationViewModel.backStack, // Your custom-managed back stack
                modifier = modifier,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                entryDecorators = listOf(
                    // Add the default decorators for managing scenes and saving state
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                ),
                entryProvider = entryProvider { // Define your screen entries here
                    entry<Screen.Home> { // Entry for the Home screen
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
                            }
                        )
                    }
                    entry<Screen.History> { // Entry for the Topics screen
                        HistoryView()
                    }

                    entry<Screen.Profile> {
                        ProfileScreen()
                    }

                    entry<Screen.ThreeSitesMeasurement> {
                        ThreeSitesMeasurementScreen(
                            onBackPressed = {
                                navigationViewModel.popLast()
                            },
                            canSave = it.canSave
                        )
                    }

                    entry<Screen.SevenSitesMeasurement> {
                        SevenSitesMeasurementScreen(
                            onBackPressed = {
                                navigationViewModel.popLast()
                            },
                            canSave = it.canSave
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
    NavigationBar { // Using NavigationBar for Material 3
        val currentScreen = navigationViewModel.backStack.last() // Observe current screen

        navItems.forEach { screen ->
            NavigationBarItem(
                icon = {
                    if (screen is Screen.Profile) {
                        val photo = navigationViewModel.user?.photoPath
                        if (photo != null) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = photo,
                                    contentScale = ContentScale.Crop,
                                    contentDescription = "Photo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            screen.icon?.let { // Ensure icon is not null
                                Icon(it, contentDescription = screen.label)
                            }
                        }
                    } else {
                        screen.icon?.let { // Ensure icon is not null
                            Icon(it, contentDescription = screen.label)
                        }
                    }
                },
                label = { Text(screen.label) },
                selected = currentScreen.route == screen.route,
                onClick = {
                    // Avoid pushing the same screen multiple times on top of itself
                    if (currentScreen.route != screen.route) {
                        navigationViewModel.push(screen) // Or use a popUpTo for better UX
                    }
                }
            )
        }
    }
}