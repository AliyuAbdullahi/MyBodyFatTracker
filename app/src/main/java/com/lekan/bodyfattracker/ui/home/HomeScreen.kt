package com.lekan.bodyfattracker.ui.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.model.BodyFatInfo
import com.lekan.bodyfattracker.ui.home.components.HomeView
import com.lekan.bodyfattracker.ui.theme.White

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStartThreeSites: () -> Unit = {},
    onStartSevenSites: () -> Unit = {},
    onStartThreeSitesGuest: () -> Unit = {},
    onStartSevenSitesGuest: () -> Unit = {},
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        HomeView(
            lastInfo = state.bodyFatInfo,
            name = state.userName,
            onStartThreeSites = onStartThreeSites,
            onStartSevenSites = onStartSevenSites,
            onGuestClicked = {
                viewModel.takeGuestMeasurement()
            },
            onThreeSitesMoreInfo = {
                viewModel.onMoreInfoClicked(
                    getInfo(
                        context,
                        BodyFatInfo.Type.THREE_POINTS
                    )
                )
            },
            onFivSitesMoreInfo = {
                viewModel.onMoreInfoClicked(
                    getInfo(
                        context,
                        BodyFatInfo.Type.SEVEN_POINTS
                    )
                )
            },
            onGuestInfoClicked = { viewModel.onMoreInfoClicked(getInfo(context, null)) }
        )
        AnimatedVisibility(visible = state.info != null) {
            state.info?.let {
                InfoCard(
                    imageResId = R.drawable.caliper,
                    imageContentDescription = "Caliper",
                    infoText = it,
                    onCloseClicked = { viewModel.onClearInfoClicked() }
                )
            }
        }

        AnimatedVisibility(visible = state.isTakingGuestMeasurement) {
            GuestMeasurementCard(
                onClose = {
                    viewModel.clearGuestMeasurement()
                },
                onThreeSitesClicked = {
                    onStartThreeSitesGuest()
                    viewModel.clearGuestMeasurement()
                },
                onSevenSitesClicked = {
                    onStartSevenSitesGuest()
                    viewModel.clearGuestMeasurement()
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.update()
    }
}

// New Composable Function for the Info Card
@Composable
fun InfoCard(
    modifier: Modifier = Modifier, // Allow passing modifiers from the caller
    imageResId: Int,              // Drawable resource ID for the image
    imageContentDescription: String,
    infoText: String,
    imageSize: Int = 120, // Default image size in dp
    imageBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant, // Background for image container
    onCloseClicked: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                onCloseClicked()
            }
            .background(color = Color.Black.copy(alpha = 0.6F)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier // Apply caller-provided modifiers first
                .fillMaxWidth()
                .clickable(
                    interactionSource = null,
                    indication = null
                ) {}
                .padding(horizontal = 32.dp) // Outer padding for the card itself
                .clip(RoundedCornerShape(16.dp)) // Corner radius for the card
                .background(MaterialTheme.colorScheme.surface) // Background color for the card
                .padding(16.dp), // Inner padding for the content *within* the card
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Image Container (optional, if you want a specific background or shape for the image)

            Row(
                modifier = Modifier.padding(16.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f)) // Spacer to push the image to the left
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black,
                    modifier = Modifier.clickable { onCloseClicked() })
            }

            Box(
                modifier = Modifier
                    .size(imageSize.dp)
                    .clip(CircleShape) // Makes the image container circular
                    .background(imageBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = imageContentDescription,
                    contentScale = ContentScale.Crop, // Or Fit, depending on your image
                    modifier = Modifier.size((imageSize * 0.9).dp) // Image slightly smaller than its container
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                modifier = Modifier.padding(16.dp),
                text = infoText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface // Text color appropriate for the surface
            )
        }
    }
}

@Composable
fun GuestMeasurementCard(
    onThreeSitesClicked: () -> Unit,
    onSevenSitesClicked: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                onClose()
            }
            .background(Color.Black.copy(alpha = 0.6F)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(White)
                    .clickable(interactionSource = null, indication = null) {}
            ) {
                Button(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    onClick = {
                        onThreeSitesClicked()
                    },
                    shape = RoundedCornerShape(12.dp), // Optional: Rounded corners for the button
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.three_sites_label))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    onClick = {
                        onSevenSitesClicked()
                    },
                    shape = RoundedCornerShape(12.dp), // Optional: Rounded corners for the button
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.seven_sites_label))
                }
            }
        }
    }
}

fun getInfo(context: Context, bodyFatInfo: BodyFatInfo.Type?) =
    when (bodyFatInfo) {
        BodyFatInfo.Type.THREE_POINTS -> context.getString(R.string.three_sites_info)
        BodyFatInfo.Type.SEVEN_POINTS -> context.getString(R.string.seven_sites_info)
        null -> context.getString(R.string.guest_info)
    }