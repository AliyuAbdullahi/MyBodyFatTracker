package com.lekan.bodyfattracker.ui.home.components

import InfoRow
import NoMeasurementComponent
import UserInfoOverview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.model.BodyFatInfo
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme
import com.lekan.bodyfattracker.ui.theme.CircleBlue
import com.lekan.bodyfattracker.ui.theme.Error
import com.lekan.bodyfattracker.ui.theme.Error3
import com.lekan.bodyfattracker.ui.theme.Green900
import com.lekan.bodyfattracker.ui.theme.Grey300
import com.lekan.bodyfattracker.ui.theme.Grey700
import com.lekan.bodyfattracker.ui.theme.Grey800
import com.lekan.bodyfattracker.ui.theme.OnSecondary

@Composable
fun HomeView(
    lastInfo: BodyFatInfo?,
    name: String? = null,
    goal: Int? = null,
    onStartThreeSites: () -> Unit = {},
    onStartSevenSites: () -> Unit = {},
    onThreeSitesMoreInfo: () -> Unit = {},
    onFivSitesMoreInfo: () -> Unit = {},
    onGuestClicked: () -> Unit = {},
    onGuestInfoClicked: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        Text(stringResource(R.string.welcome_message, name.orEmpty()), modifier = Modifier.padding(16.dp), fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        if (lastInfo == null) { // Changed condition to correctly show NoMeasurementComponent
            NoMeasurementComponent()
        } else {
            // lastInfo is not null here, so no need for lastInfo?.let
            val color = when {
                lastInfo.percentage > 20 -> Error3
                lastInfo.percentage > 15 -> CircleBlue
                lastInfo.percentage > 10 -> Color.Green
                else -> Error
            }

            val circleColor = when {
                lastInfo.percentage > 20 -> Grey700
                lastInfo.percentage > 15 -> Grey800
                lastInfo.percentage > 10 -> Grey700
                else -> Error
            }
            UserInfoOverview(
                bodyFatPercentageString = stringResource(R.string.percentage_integer_format, lastInfo.percentage),
                bodyFatLabelColor = color,
                date = lastInfo.date,
                circleColor = Grey700,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                currentBodyFatValue = lastInfo.percentage,
                bodyFatGoalValue = goal
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HomeBody(
            onStartThreeSites = onStartThreeSites,
            onStartFiveSites = onStartSevenSites,
            onThreeSitesMoreInfo = onThreeSitesMoreInfo,
            onFivSitesMoreInfo = onFivSitesMoreInfo,
            onGuestClicked = onGuestClicked,
            onGuestInfoClicked = onGuestInfoClicked
        )
    }
}

@Composable
private fun HomeBody(
    onStartThreeSites: () -> Unit,
    onStartFiveSites: () -> Unit,
    onThreeSitesMoreInfo: () -> Unit,
    onFivSitesMoreInfo: () -> Unit,
    onGuestClicked: () -> Unit = {},
    onGuestInfoClicked: () -> Unit = {}
) {
    InfoRow(
        text = stringResource(R.string.three_points_measurement),
        onRowClick = onStartThreeSites,
        onMoreInfoClick = onThreeSitesMoreInfo,
        circleColor = OnSecondary,
        subtitle = stringResource(R.string.three_sites_accuracy),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    InfoRow(
        text = stringResource(R.string.seven_points_measurement),
        onRowClick = onStartFiveSites,
        onMoreInfoClick = onFivSitesMoreInfo,
        circleColor = Green900,
        subtitle = stringResource(R.string.seven_sites_accuracy),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )

    Spacer(modifier = Modifier.height(32.dp))

    InfoRow(
        text = stringResource(R.string.guest),
        onRowClick = onGuestClicked,
        onMoreInfoClick = onGuestInfoClicked,
        circleColor = Grey300,
        subtitle = "",
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}


@Preview(showBackground = true, name = "HomeScreen - No Last Info")
@Composable
fun HomeScreenPreview_NoLastInfo() {
    BodyFatTrackerTheme { // Replace with your actual app theme if different
        Surface {
            HomeView(lastInfo = null)
        }
    }
}

@Preview(showBackground = true, name = "HomeScreen - With Last Info (Healthy)")
@Composable
fun HomeScreenPreview_WithLastInfoHealthy() {
    BodyFatTrackerTheme {
        Surface {
            HomeView(
                name = "Jake",
                lastInfo = BodyFatInfo(
                    percentage = 12, // Example: Healthy range
                    date = "Oct 28, 2023",
                    timeStamp = System.currentTimeMillis(),
                    type = BodyFatInfo.Type.THREE_POINTS
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "HomeScreen - With Last Info (High)")
@Composable
fun HomeScreenPreview_WithLastInfoHigh() {
    BodyFatTrackerTheme {
        Surface {
            HomeView(
                lastInfo = BodyFatInfo(
                    percentage = 25, // Example: High range
                    date = "Oct 29, 2023",
                    timeStamp = System.currentTimeMillis(),
                    type = BodyFatInfo.Type.SEVEN_POINTS
                )
            )
        }
    }
}

// Make sure these dummy composables are defined if they are not imported
// or part of the same file. Remove them if they are already accessible.
/*
@Composable
fun NoMeasurementComponent() {
    androidx.compose.material3.Text("No measurements yet. Add one!", modifier = Modifier.padding(16.dp))
}

@Composable
fun InfoRow(
    text: String,
    onRowClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    circleColor: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Text("$text (More Info)", color = circleColor, modifier = modifier.padding(vertical = 8.dp, horizontal = 16.dp))
}
*/
