package com.lekan.bodyfattracker.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School // Added for Education
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

sealed class Screen(val route: String, val label: String, val icon: ImageVector? = null): NavKey {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object History : Screen("history", "History", Icons.Filled.List)
    data object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    data object Education : Screen("education", "Education", Icons.Filled.School) // Added Education screen

    data class ThreeSitesMeasurement(val canSave: Boolean = false) : Screen("three_sites_measurement", "Three Sites Measurement")
    data class SevenSitesMeasurement(val canSave: Boolean = false) : Screen("seven_sites_measurement", "Seven Sites Measurement") // Corrected route

    // Added new screen definitions
    data object AddMeasurementScreen : Screen("add_measurement", "Add Measurement", Icons.Filled.AddCircle)
    data object AddWeightEntryScreen : Screen("add_weight_entry", "Add Weight", Icons.Filled.List)

    // Screen for playing YouTube videos
    data class YoutubePlayer(val videoId: String) : Screen("youtube_player", "Video Player")

    data object FeedbacksScreen : Screen("feedbacks", "Feedbacks", Icons.AutoMirrored.Filled.List)
}
