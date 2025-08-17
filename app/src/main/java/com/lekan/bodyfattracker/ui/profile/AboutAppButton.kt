package com.lekan.bodyfattracker.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AboutAppButton(
    modifier: Modifier = Modifier.Companion,
    onAboutClickedAppClicked: () -> Unit
) {
    Text(
        text = "About App",
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.clickable {
            onAboutClickedAppClicked()
        },
        fontSize = 16.sp,
        fontWeight = FontWeight.Companion.SemiBold
    )
}