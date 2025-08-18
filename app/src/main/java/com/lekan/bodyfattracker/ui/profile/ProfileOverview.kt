package com.lekan.bodyfattracker.ui.profile

// ... other necessary imports from ProfileView.kt ...
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.placeholder
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.ui.home.Gender
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme
import java.io.File

@Composable
fun ProfileOverview(
    name: String,
    photoPath: String?,
    bodyFatGoal: String?, // Display value, e.g., "20.5 %" or "Not set"
    gender: Gender?,
    onEditProfileClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Changed to Top for better control
    ) {
        Text(
            text = stringResource(R.string.profile_title), // New string resource
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .size(150.dp) // Slightly larger for overview
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface) // Solid background for image
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val context = LocalContext.current
            if (photoPath != null) {
                val imageRequest = remember(photoPath) {
                    ImageRequest.Builder(context)
                        .data(File(photoPath))
                        .crossfade(true)
                        .placeholder(R.drawable.camera_image)
                        .build()
                }
                Image(
                    painter = rememberAsyncImagePainter(model = imageRequest),
                    contentDescription = stringResource(R.string.profile_picture_desc),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.camera_image),
                    contentDescription = stringResource(R.string.add_profile_photo_desc),
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onEditProfileClicked,
            modifier = Modifier.fillMaxWidth(0.7f) // Button takes 70% of width
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.edit_profile_desc), // New string
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.edit_profile_desc)) // New string
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Goal Section
        Text(
            text = stringResource(R.string.body_fat_goal_optional_label), // New string
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = bodyFatGoal ?: stringResource(R.string.not_set_placeholder), // New string
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Gender Section
        Text(
            text = stringResource(R.string.gender_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = gender?.name?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                ?: stringResource(R.string.not_set_placeholder),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Preview(showBackground = true, name = "Profile Overview - Full Data")
@Composable
fun ProfileOverviewFullPreview() {
    BodyFatTrackerTheme { // Apply your app's theme for consistent preview
        Surface {
            ProfileOverview(
                name = "Jane Doe",
                // For previewing with an image, you'd typically use a drawable resource
                // if you don't want to rely on an actual file path existing during preview.
                // Since photoPath expects a file path string, this is tricky for previews
                // without either a sample image in resources + logic to get its URI/path for preview,
                // or by enhancing ProfileOverview to accept a Painter directly for previews.
                // For simplicity, we'll show it with null photoPath here, or you can
                // use a placeholder drawable in your debug resources and reference its "path".
                // For now, let's assume photoPath = null will show the camera icon.
                photoPath = null, // Or provide a valid path to a sample image if accessible in preview
                bodyFatGoal = "18.5 %",
                gender = Gender.FEMALE,
                onEditProfileClicked = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Profile Overview - No Photo, No Goal")
@Composable
fun ProfileOverviewNoPhotoNoGoalPreview() {
    BodyFatTrackerTheme {
        Surface {
            ProfileOverview(
                name = "John Smith",
                photoPath = null,
                bodyFatGoal = null, // Handled by ?: stringResource(R.string.not_set_placeholder)
                gender = Gender.MALE,
                onEditProfileClicked = {}
            )
        }
    }
}


