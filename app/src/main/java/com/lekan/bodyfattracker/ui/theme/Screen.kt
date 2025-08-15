package com.lekan.bodyfattracker.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class Screen(val route: String, val label: String, val icon: ImageVector? = null): NavKey {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object History : Screen("history", "History", Icons.Filled.List)
    data object Profile : Screen("profile", "Profile", Icons.Filled.Person)

    data class ThreeSitesMeasurement(val canSave: Boolean = false) : Screen("three_sites_measurement", "Three Sites Measurement")

    data class SevenSitesMeasurement(val canSave: Boolean = false) : Screen("five_sites_measurement", "Five Sites Measurement")
    // Add other screens if any
}