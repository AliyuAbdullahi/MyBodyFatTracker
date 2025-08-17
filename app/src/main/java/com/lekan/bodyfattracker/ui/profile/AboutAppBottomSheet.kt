package com.lekan.bodyfattracker.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme

@Composable
fun AboutAppBottomSheet(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(Color.Companion.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss), // Click on scrim to dismiss
        contentAlignment = Alignment.Companion.BottomCenter // Align sheet to bottom
    ) {
        Surface( // The bottom sheet itself
            modifier = Modifier.Companion
                .fillMaxWidth()
                .fillMaxHeight(0.7f) // Takes 70% of screen height
                .clickable(enabled = false) {}, // Prevent clicks on the sheet from propagating to the scrim
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Box(modifier = Modifier.Companion.fillMaxSize()) { // Use Box for easier alignment of close button
                Column(
                    modifier = Modifier.Companion
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.about_app),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.Companion.padding(bottom = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.about_app_content),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.Companion.padding(bottom = 16.dp)
                    )
                    // Add more content about your app here
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.Companion
                        .align(Alignment.Companion.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.close_button_desc)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "About App Bottom Sheet Preview")
@Composable
fun AboutAppBottomSheetPreview() {
    BodyFatTrackerTheme {
        Box(Modifier.fillMaxSize()) { // Simulate it being overlaid
            AboutAppBottomSheet(onDismiss = {})
        }
    }
}