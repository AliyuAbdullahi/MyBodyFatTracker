package com.lekan.bodyfattracker.ui.education

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.lekan.bodyfattracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationScreen(
    viewModel: EducationViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.addVideoMessage) {
        uiState.addVideoMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearAddVideoMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.education_tab_title)) },
                windowInsets = WindowInsets(0),
            )
        },
        floatingActionButton = {
            if (uiState.isSuperUser) {
                FloatingActionButton(onClick = { viewModel.onShowAddVideoDialog() }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_education_video_fab_cd)
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.error_loading_videos, uiState.error.orEmpty()))
                }
            } else if (uiState.videos.isEmpty() && !uiState.isLoading) { // Ensure not to show "no videos" while loading
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.no_videos_available))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(uiState.videos, key = { it.id }) { video ->
                        VideoListItem(videoInfo = video) {
                            val openYouTubeIntent = Intent(
                                Intent.ACTION_VIEW,
                                "https://www.youtube.com/watch?v=${video.youtubeVideoId}".toUri()
                            )
                            context.startActivity(openYouTubeIntent)
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }

    if (uiState.showAddVideoDialog) {
        AddVideoDialog(
            uiState = uiState,
            onTitleChange = viewModel::onNewVideoTitleChanged,
            onUrlChange = viewModel::onNewVideoUrlChanged,
            onDismiss = viewModel::onDismissAddVideoDialog,
            onSubmit = viewModel::submitNewVideo
        )
    }
}

@Composable
fun VideoListItem(
    videoInfo: VideoInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = videoInfo.thumbnailUrl,
                contentDescription = videoInfo.title, // Keep title for CD
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_notification_large), // Replace with a generic placeholder
                error = painterResource(R.drawable.ic_notification_large), // Replace with a generic error placeholder
                modifier = Modifier
                    .size(width = 120.dp, height = 90.dp) // Fixed size for consistency
                    .clip(RoundedCornerShape(8.dp)) // More rounded corners
            )
            Spacer(modifier = Modifier.width(12.dp)) // Increased spacer
            Text(
                text = videoInfo.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 3, // Allow a bit more text
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f) // Allow text to take remaining space
            )
        }
    }
}

@Composable
fun AddVideoDialog(
    uiState: EducationUiState,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_video_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = uiState.newVideoTitle,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(R.string.video_title_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.newVideoTitle.isBlank() && uiState.addVideoMessage != null // Basic error indication
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.newVideoUrl,
                    onValueChange = onUrlChange,
                    label = { Text(stringResource(R.string.video_url_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.newVideoUrl.isBlank() && uiState.addVideoMessage != null // Basic error indication
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !uiState.addVideoInProgress && uiState.newVideoTitle.isNotBlank() && uiState.newVideoUrl.isNotBlank()
            ) {
                if (uiState.addVideoInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(R.string.add_button))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

// Preview for EducationScreen (Optional, but good practice)
// @Preview(showBackground = true)
// @Composable
// fun EducationScreenPreview() {
//    // You would typically mock the ViewModel and UI state here
//    // For simplicity, this is just a placeholder
//    BodyFatTrackerTheme {
//        EducationScreen()
//    }
// }
//
// @Preview(showBackground = true)
// @Composable
// fun AddVideoDialogPreview() {
//    BodyFatTrackerTheme {
//        AddVideoDialog(
//            uiState = EducationUiState(showAddVideoDialog = true, isSuperUser = true),
//            onTitleChange = {},
//            onUrlChange = {},
//            onDismiss = {},
//            onSubmit = {}
//        )
//    }
// }
