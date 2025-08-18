package com.lekan.bodyfattracker.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun App() {
    BodyFatTrackerTheme {
        AppContent()
    }
}