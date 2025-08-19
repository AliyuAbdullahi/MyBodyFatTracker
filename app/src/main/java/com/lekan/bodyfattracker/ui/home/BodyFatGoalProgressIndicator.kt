package com.lekan.bodyfattracker.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lekan.bodyfattracker.R
import kotlin.math.roundToInt

@Composable
fun BodyFatGoalProgressIndicator(
    currentBfp: Double,
    goalBfp: Double,
    modifier: Modifier = Modifier.Companion
) {
    // Ensure inputs are positive for sensible calculations
    val saneCurrentBfp = currentBfp.coerceAtLeast(0.1)
    val saneGoalBfp = goalBfp.coerceAtLeast(0.1)

    val fillRatioTarget = if (saneCurrentBfp <= saneGoalBfp) {
        1.0f
    } else {
        (saneGoalBfp / saneCurrentBfp).toFloat().coerceIn(0f, 1f)
    }

    val animatedFillRatio by animateFloatAsState(
        targetValue = fillRatioTarget,
        animationSpec = tween(durationMillis = 1000), // Smooth animation
        label = "GoalProgressBarAnimation"
    )

    val progressPercentage = (animatedFillRatio * 100).roundToInt()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.goal_tracking_title),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.Companion.padding(bottom = 8.dp)
        )

        Box( // Track
            modifier = Modifier.Companion
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box( // Progress
                modifier = Modifier.Companion
                    .fillMaxWidth(fraction = animatedFillRatio)
                    .fillMaxHeight()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.Companion.height(8.dp))

        val progressText = if (saneCurrentBfp <= saneGoalBfp) {
            stringResource(R.string.goal_achieved_message_simple, saneGoalBfp)
        } else {
            stringResource(
                R.string.progress_towards_goal_message,
                progressPercentage,
                saneCurrentBfp,
                saneGoalBfp
            )
        }

        Text(
            text = progressText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Companion.Center,
            modifier = Modifier.Companion.fillMaxWidth()
        )
    }
}