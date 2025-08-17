import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme
import com.lekan.bodyfattracker.ui.theme.Grey500
import com.lekan.bodyfattracker.ui.theme.PadGrey
import com.lekan.bodyfattracker.ui.uttils.CaptureController
import com.lekan.bodyfattracker.ui.uttils.ShareUtils
import kotlinx.coroutines.launch

@Composable
fun UserInfoOverview(
    modifier: Modifier = Modifier,
    bodyFatPercentageString: String = "15%",
    bodyFatLabelColor: Color = Color.Green,
    circleColor: Color = Grey500,
    date: String = "2023-09-10 12:00:00",
    currentBodyFatValue: Int?,
    bodyFatGoalValue: Int?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val captureController = remember { CaptureController() }

    AndroidView(
        factory = { ctx ->
            androidx.compose.ui.platform.ComposeView(ctx).apply {
                setViewCompositionStrategy(
                    androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    BodyFatTrackerTheme { // Apply your app's theme
                        ActualCapturableContentInternal(
                            bodyFatPercentageString = bodyFatPercentageString,
                            bodyFatLabelColor = bodyFatLabelColor,
                            circleColor = circleColor,
                            date = date,
                            currentBodyFatValue = currentBodyFatValue,
                            bodyFatGoalValue = bodyFatGoalValue,
                            onRequestCapture = {
                                coroutineScope.launch {
                                    // Ensure view is assigned and ready
                                    if (captureController.viewToCapture == null ||
                                        (captureController.viewToCapture?.width ?: 0) == 0 ||
                                        (captureController.viewToCapture?.height ?: 0) == 0) {
                                        // View not ready, try to wait a bit (not ideal, but pragmatic for some cases)
                                        // A more robust solution involves explicit layout readiness signals
                                        kotlinx.coroutines.delay(200) // Increased delay, adjust as needed
                                    }

                                    if (captureController.viewToCapture != null &&
                                        (captureController.viewToCapture?.width ?: 0) > 0 &&
                                        (captureController.viewToCapture?.height ?: 0) > 0
                                    ) {
                                        val bitmap = captureController.capture()
                                        val imageUri = ShareUtils.saveBitmapToCache(context, bitmap)
                                        val shareText =
                                            context.getString(R.string.i_am_sharing_my_body_fat_percentage_with_you)
                                        ShareUtils.shareImageWithText(context, shareText, imageUri)
                                    } else {
                                        // Handle error: View not ready for capture even after delay
                                        println("Error: View not ready for capture.")
                                        // Optionally show a toast or log
                                    }
                                }
                            }
                        )
                    }
                }
                captureController.assignView(this)
            }
        },
        modifier = modifier, // This modifier sizes the AndroidView which hosts the ComposeView
        update = { composeView ->
            // Re-assign view to controller and re-set content if parameters change
            captureController.assignView(composeView)
            composeView.setContent {
                BodyFatTrackerTheme {
                    ActualCapturableContentInternal(
                        bodyFatPercentageString = bodyFatPercentageString,
                        bodyFatLabelColor = bodyFatLabelColor,
                        circleColor = circleColor,
                        date = date,
                        currentBodyFatValue = currentBodyFatValue,
                        bodyFatGoalValue = bodyFatGoalValue,
                        onRequestCapture = {
                            coroutineScope.launch {
                                // Same capture logic as in factory's onRequestCapture
                                if (captureController.viewToCapture == null ||
                                    (captureController.viewToCapture?.width ?: 0) == 0 ||
                                    (captureController.viewToCapture?.height ?: 0) == 0) {
                                    kotlinx.coroutines.delay(200)
                                }
                                if (captureController.viewToCapture != null &&
                                    (captureController.viewToCapture?.width ?: 0) > 0 &&
                                    (captureController.viewToCapture?.height ?: 0) > 0
                                ) {
                                    val bitmap = captureController.capture()
                                    val imageUri = ShareUtils.saveBitmapToCache(context, bitmap)
                                    val shareText = "I am sharing my body fat percentage with you!"
                                    ShareUtils.shareImageWithText(context, shareText, imageUri)
                                } else {
                                    println("Error: View not ready for capture in update block.")
                                }
                            }
                        }
                    )
                }
            }
        }
    )

    LaunchedEffect(captureController) {
        captureController.onCaptureRequested.collect {
            val view = captureController.viewToCapture
            view?.let {
                if (it.width > 0 && it.height > 0) {
                    val bitmap = ShareUtils.captureView(it)
                    captureController.onBitmapCaptured(bitmap)
                } else {
                    // Fallback for when view dimensions are not ready
                    // This might happen if capture is triggered too quickly
                    kotlinx.coroutines.delay(100) // Short delay to allow layout
                    if (it.width > 0 && it.height > 0) {
                        val bitmap = ShareUtils.captureView(it)
                        captureController.onBitmapCaptured(bitmap)
                    } else {
                        println("Warning: Capture attempted on view with zero dimensions.")
                        // Create a minimal placeholder bitmap to avoid crash, though this indicates a problem
                        val errorBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                        captureController.onBitmapCaptured(errorBitmap)
                    }
                }
            } ?: run {
                println("Error: viewToCapture was null during capture request.")
                val errorBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                captureController.onBitmapCaptured(errorBitmap) // Avoid deadlock
            }
        }
    }
}

@Composable
private fun ActualCapturableContentInternal(
    // Parameters as before
    bodyFatPercentageString: String,
    bodyFatLabelColor: Color,
    circleColor: Color,
    date: String,
    currentBodyFatValue: Int?,
    bodyFatGoalValue: Int?,
    onRequestCapture: () -> Unit
) {
    // This Column defines the content that will be drawn into the ComposeView
    Column(
        modifier = Modifier
            .fillMaxWidth() // Fill the ComposeView provided by AndroidView
            // The clip and background should be here to be part of the captured image
            .clip(RoundedCornerShape(16.dp))
            .background(PadGrey)
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onRequestCapture) {
                Icon(
                    imageVector = Icons.Default.Share,
                    tint = Color.Black, // Or use MaterialTheme.colorScheme.onSurface
                    contentDescription = stringResource(R.string.share)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.body_fat_label),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = Color.Black.copy(alpha = 0.9f) // Consider MaterialTheme colors
                )
            }
            CircleWithContent(
                modifier = Modifier.size(100.dp),
                circleBackgroundColor = circleColor,
            ) {
                Text(
                    text = bodyFatPercentageString,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = bodyFatLabelColor,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        if (bodyFatGoalValue != null && currentBodyFatValue != null) {
            Spacer(modifier = Modifier.height(24.dp))
            GoalOverview(
                currentBodyFat = currentBodyFatValue,
                bodyFatGoal = bodyFatGoalValue
            )
        }
    }
}

@Composable
fun GoalOverview(
    currentBodyFat: Int,
    bodyFatGoal: Int,
    modifier: Modifier = Modifier,
    progressBarHeight: Int = 20
) {
    val progressValue: Float
    if (currentBodyFat <= bodyFatGoal) {
        progressValue = 1.0f
    } else {
        if (bodyFatGoal == 0) {
            progressValue = if (currentBodyFat == 0) 1.0f else 0.0f
        } else {
            val effectiveDifference = currentBodyFat - bodyFatGoal
            progressValue = (bodyFatGoal.toFloat() - effectiveDifference.toFloat()) / bodyFatGoal.toFloat()
        }
    }
    val displayedProgress = progressValue.coerceIn(0f, 1f)
    val percentageReached = (displayedProgress * 100).toInt()

    Column( // Added parent Column for GoalOverview as per original structure
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.your_bf_goal_label, bodyFatGoal),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f) // Progress bar takes 80% of width
                .height(progressBarHeight.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(progressBarHeight.dp / 2))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(displayedProgress)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(progressBarHeight.dp / 2))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.goal_progress_percentage, percentageReached),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CircleWithContent(
    modifier: Modifier = Modifier,
    circleBackgroundColor: Color = Color.LightGray,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(circleBackgroundColor)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}


// --- Previews ---

@Preview(showBackground = true, widthDp = 360)
@Composable
fun UserInfoOverviewPreview_Default() {
    BodyFatTrackerTheme {
        Surface(modifier = Modifier.fillMaxWidth()) { // Surface helps with theme application
            UserInfoOverview(
                modifier = Modifier.padding(16.dp), // Modifier for the AndroidView
                bodyFatPercentageString = "22%",
                bodyFatLabelColor = MaterialTheme.colorScheme.error, // Use theme colors
                circleColor = MaterialTheme.colorScheme.secondaryContainer,
                date = "2024-01-15 10:30:00",
                currentBodyFatValue = 22,
                bodyFatGoalValue = 20
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun UserInfoOverviewPreview_GoalMet() {
    BodyFatTrackerTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            UserInfoOverview(
                modifier = Modifier.padding(16.dp),
                bodyFatPercentageString = "18%",
                bodyFatLabelColor = Color.Green, // Or a theme color for success
                date = "2024-03-20 11:00:00",
                currentBodyFatValue = 18,
                bodyFatGoalValue = 20
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun UserInfoOverviewPreview_NoGoal() {
    BodyFatTrackerTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            UserInfoOverview(
                modifier = Modifier.padding(16.dp),
                bodyFatPercentageString = "25%",
                bodyFatLabelColor = MaterialTheme.colorScheme.primary,
                date = "2024-02-10 12:00:00",
                currentBodyFatValue = 25,
                bodyFatGoalValue = null // No goal
            )
        }
    }
}