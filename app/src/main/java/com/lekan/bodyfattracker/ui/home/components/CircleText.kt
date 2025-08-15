package com.lekan.bodyfattracker.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

/**
 * A Composable that displays a circular Box with a custom content in the center.
 *
 * @param modifier The modifier to be applied to the Box.
 * @param circleBackgroundColor The background color of the circle.
 * @param content The composable content to be displayed in the center of the circle.
 *                This is typically a Text composable, but can be any composable.
 */
@Composable
fun CircleWithContent(
    modifier: Modifier = Modifier,
    circleBackgroundColor: Color = Color.LightGray,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f) // This makes the Box a square
            .clip(CircleShape) // This clips the Box to a circle shape
            .background(circleBackgroundColor)
    ) {
        content() // Invoke the content composable lambda
    }
}

@Preview(showBackground = true, name = "Circle with Simple Text")
@Composable
fun CircleWithSimpleTextPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircleWithContent(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.3f), // Make the circle 30% of the preview size
            content = { Text(text = "123") }
        )
    }
}

@Preview(showBackground = true, name = "Circle with Styled Text")
@Composable
fun CircleWithStyledTextPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircleWithContent(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.4f), // Make the circle 40% of the preview size
            circleBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            content = {
                Text(
                    text = "75%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        )
    }
}

@Preview(showBackground = true, name = "Circle with Different Content")
@Composable
fun CircleWithDifferentContentPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircleWithContent(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.2f),
            circleBackgroundColor = Color.Cyan,
            content = {
                // Example with a different composable (e.g., an Icon, though you'd need the dependency)
                // For simplicity, we'll use another Box here
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.5f) // Smaller box inside
                        .background(Color.Magenta)
                )
            }
        )
    }
}
