package com.lekan.bodyfattracker.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lekan.bodyfattracker.model.BodyFatMeasurement
import com.lekan.bodyfattracker.model.UserProfile
import com.lekan.bodyfattracker.model.getLocalisedMethodName
import com.lekan.bodyfattracker.ui.home.BodyFatGoalProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LatestMeasurementCard(
    modifier: Modifier = Modifier,
    userProfile: UserProfile?,
    latestMeasurement: BodyFatMeasurement?
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Latest Body Fat",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (latestMeasurement != null) {
                // Helper to format the timestamp
                val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                val formattedDate = sdf.format(Date(latestMeasurement.timeStamp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", latestMeasurement.percentage)}%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Method: ${latestMeasurement.method.getLocalisedMethodName(
                            LocalContext.current
                        )}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Measured on: $formattedDate",
                    style = MaterialTheme.typography.bodySmall
                )
                // You can add more details here if needed
                // e.g., site measurements if applicable and available in the model
            } else {
                Text(
                    text = "No body fat measurements recorded yet.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (latestMeasurement != null && userProfile?.bodyFatPercentGoal != null && userProfile.bodyFatPercentGoal > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                BodyFatGoalProgressIndicator(
                    currentBfp = latestMeasurement.percentage,
                    goalBfp = userProfile.bodyFatPercentGoal.toDouble()
                )
            }
        }
    }
}
