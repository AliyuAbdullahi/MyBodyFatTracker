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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationScreen(
    viewModel: EducationViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val tabTitles = listOf(
        stringResource(R.string.cloud_videos_tab),
        stringResource(R.string.saved_videos_tab)
    )

    LaunchedEffect(uiState.addVideoMessage) {
        uiState.addVideoMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearAddVideoMessage()
        }
    }
     LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            // Consider adding a viewModel.clearErrorMessage() if you want errors to be non-sticky
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
        ) {
            TabRow(selectedTabIndex = uiState.currentTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.currentTab == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }

            // Content based on selected tab
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                when (uiState.currentTab) {
                    0 -> CloudVideosTabContent(uiState = uiState, viewModel = viewModel)
                    1 -> SavedVideosTabContent(uiState = uiState, viewModel = viewModel)
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
            onSubmit = {
                scope.launch { // Launching submit in a coroutine as it might involve suspend functions
                    viewModel.submitNewVideo()
                }
            }
        )
    }
}

@Composable
fun CloudVideosTabContent(uiState: EducationUiState, viewModel: EducationViewModel) {
    val context = LocalContext.current
    if (uiState.isLoadingCloud) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (uiState.cloudVideos.isEmpty()) {
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
            items(uiState.cloudVideos, key = { "cloud-${it.id}" }) { video ->
                val isBookmarked = video.id in uiState.savedVideoIds
                VideoListItem(
                    videoInfo = video,
                    isBookmarked = isBookmarked,
                    onItemClick = {
                        val openYouTubeIntent = Intent(
                            Intent.ACTION_VIEW,
                            "https://www.youtube.com/watch?v=${video.youtubeVideoId}".toUri()
                        )
                        context.startActivity(openYouTubeIntent)
                    },
                    onBookmarkToggle = {
                        if (isBookmarked) {
                            viewModel.unbookmarkVideo(video)
                        } else {
                            viewModel.bookmarkVideo(video)
                        }
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun SavedVideosTabContent(uiState: EducationUiState, viewModel: EducationViewModel) {
    val context = LocalContext.current
    if (uiState.savedVideosList.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.no_saved_videos))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(uiState.savedVideosList, key = { "saved-${it.id}" }) { video ->
                VideoListItem(
                    videoInfo = video,
                    onItemClick = {
                        val openYouTubeIntent = Intent(
                            Intent.ACTION_VIEW,
                            "https://www.youtube.com/watch?v=${video.youtubeVideoId}".toUri()
                        )
                        context.startActivity(openYouTubeIntent)
                    },
                    onDelete = {
                        viewModel.deleteSavedVideo(video)
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}


@Composable
fun VideoListItem(
    videoInfo: VideoInfo,
    onItemClick: () -> Unit,
    isBookmarked: Boolean? = null, // Nullable for saved videos tab where it's not shown
    onBookmarkToggle: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = videoInfo.thumbnailUrl,
                contentDescription = videoInfo.title,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_notification_large),
                error = painterResource(R.drawable.ic_notification_large),
                modifier = Modifier
                    .size(width = 120.dp, height = 90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                 Text(
                    text = videoInfo.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2, // Max 2 lines for title
                    overflow = TextOverflow.Ellipsis
                )
                // Spacer for actions, or if you want to add more info like video duration later
                Spacer(modifier = Modifier.height(4.dp))
            }


            // Action buttons
            if (onBookmarkToggle != null && isBookmarked != null) {
                IconButton(onClick = onBookmarkToggle) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = stringResource(if (isBookmarked) R.string.unbookmark_video_cd else R.string.bookmark_video_cd),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete_saved_video_cd),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
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
                    isError = uiState.newVideoTitleError != null,
                    supportingText = { uiState.newVideoTitleError?.let { Text(it) } }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.newVideoUrl,
                    onValueChange = onUrlChange,
                    label = { Text(stringResource(R.string.video_url_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.newVideoUrlError != null,
                    supportingText = { uiState.newVideoUrlError?.let { Text(it) } }
                )
                // Display general add video message/error if any (e.g. from submitNewVideo)
                 uiState.addVideoMessage?.let {
                    val isError = uiState.error != null // A bit of a guess, tailor this to how addVideoMessage is set
                    Text(
                        text = it,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
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
