package com.lekan.bodyfattracker.ui.education

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.lekan.bodyfattracker.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationScreen(
    viewModel: EducationViewModel = hiltViewModel(),
    onVideoClick: (videoId: String) -> Unit
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
            viewModel.clearErrorMessage()
        }
    }

    // Superuser delete feedback
    LaunchedEffect(uiState.deleteOperationMessage) {
        uiState.deleteOperationMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearDeleteOperationMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isSuperUser && uiState.isSelectionModeActive && uiState.currentTab == 0) {
                        Text(stringResource(R.string.selected_items_title, uiState.selectedVideoIds.size))
                    } else {
                        Text(stringResource(R.string.education_tab_title))
                    }
                },
                windowInsets = WindowInsets(0),
                actions = {
                    if (uiState.isSuperUser && uiState.currentTab == 0) { // Actions for Superuser on Cloud Videos tab
                        if (uiState.isSelectionModeActive) {
                            IconButton(
                                onClick = {
                                    viewModel.prepareDeleteSelectedVideos(
                                        title = context.getString(R.string.confirm_delete_title),
                                        messageFormat = context.getString(R.string.confirm_delete_selected_message)
                                    )
                                },
                                enabled = uiState.selectedVideoIds.isNotEmpty()
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.delete_selected_videos_cd)
                                )
                            }
                            IconButton(onClick = { viewModel.clearSelectionMode() }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.clear_selection_cd)
                                )
                            }
                        } else {
                            IconButton(onClick = {
                                viewModel.prepareDeleteAllCloudVideos(
                                    title = context.getString(R.string.confirm_delete_title),
                                    message = context.getString(R.string.confirm_delete_all_cloud_message)
                                )
                            }) {
                                Icon(
                                    Icons.Filled.DeleteSweep,
                                    contentDescription = stringResource(R.string.delete_all_cloud_videos_cd)
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // FAB is visible for superuser and when not in selection mode for cloud videos
            if (uiState.isSuperUser && !(uiState.isSelectionModeActive && uiState.currentTab == 0)) {
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

            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                when (uiState.currentTab) {
                    0 -> CloudVideosTabContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onItemClick = { videoInfo -> onVideoClick(videoInfo.youtubeVideoId) }
                    )

                    1 -> SavedVideosTabContent(
                        uiState = uiState,
                        viewModel = viewModel, // Pass viewModel for consistency, though not all features used
                        onItemClick = { videoInfo -> onVideoClick(videoInfo.youtubeVideoId) }
                    )
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
                scope.launch {
                    viewModel.submitNewVideo()
                }
            }
        )
    }

    // Confirmation Dialog for Deletions
    if (uiState.showConfirmDeleteDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { viewModel.cancelVideoDeletion() },
            title = { Text(uiState.confirmDeleteDialogTitle) },
            text = { Text(uiState.confirmDeleteDialogMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.executeConfirmedDeletion(
                            successMessageSelected = context.getString(R.string.videos_deleted_success),
                            successMessageAll = context.getString(R.string.all_videos_deleted_success),
                            errorMessageFormat = context.getString(R.string.delete_video_error) // Assuming general error message format
                        )
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelVideoDeletion() }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    // Global loading indicator for deletions
    if (uiState.isDeletingVideos) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Use the same padding as main content
                .clickable(enabled = false, onClick = {}), // Block interaction
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.deleting_videos))
        }
    }
}

@Composable
fun CloudVideosTabContent(
    uiState: EducationUiState,
    viewModel: EducationViewModel,
    onItemClick: (VideoInfo) -> Unit
) {
    Log.d("EducationScreen", "CloudVideosTabContent recomposing. isLoading: ${uiState.isLoadingCloud}, videos count: ${uiState.filteredCloudVideos.size}, error: ${uiState.error}, query: ${uiState.searchQuery}")

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            label = { Text(stringResource(R.string.search_videos_placeholder)) },
            placeholder = { Text(stringResource(R.string.search_videos_placeholder)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true
        )

        if (uiState.isLoadingCloud) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null && uiState.filteredCloudVideos.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(stringResource(R.string.error_loading_videos) + " " + uiState.error)
            }
        } else if (uiState.filteredCloudVideos.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(if (uiState.searchQuery.isNotEmpty()) stringResource(R.string.no_videos_found_search) else stringResource(R.string.no_videos_available))
            }
        } else {
            val confirmDeleteTitle = stringResource(R.string.confirm_delete_title)
            val confirmDeleteMessage =  stringResource(R.string.confirm_delete_single_message)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredCloudVideos, key = { "cloud-${it.id}" }) { video ->
                    VideoListItem(
                        videoInfo = video,
                        onItemClick = { onItemClick(video) }, // For playback
                        isBookmarked = video.id in uiState.savedVideoIds,
                        onBookmarkToggle = {
                            if (video.id in uiState.savedVideoIds) {
                                viewModel.unbookmarkVideo(video)
                            } else {
                                viewModel.bookmarkVideo(video)
                            }
                        },
                        // Superuser and selection related parameters
                        isSuperUser = uiState.isSuperUser,
                        isCloudVideoItem = true,
                        isSelectionModeActive = uiState.isSelectionModeActive,
                        isSelected = video.id in uiState.selectedVideoIds,
                        onVideoLongClick = { viewModel.onVideoLongPress(video) },
                        onVideoShortClickInSelection = { viewModel.onVideoShortPressSelection(video) },
                        onVideoShortClickForSingleDelete = {
                            viewModel.prepareDeleteSingleVideoFromTap(
                                videoInfo = video,
                                title = confirmDeleteTitle,
                                messageFormat = confirmDeleteMessage
                            )
                        },
                        // Saved video specific parameters (not used here)
                        showDeleteActionForSavedVideo = false,
                        onDeleteSavedVideoClick = null
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) } // For padding at the bottom
            }
        }
    }
}

@Composable
fun SavedVideosTabContent(
    uiState: EducationUiState,
    viewModel: EducationViewModel, // Passed for consistency if needed, but primary actions are via existing uiState fields
    onItemClick: (VideoInfo) -> Unit
) {
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
                    onItemClick = { onItemClick(video) }, // For playback
                    // Cloud video specific parameters (not used here)
                    isBookmarked = null, // Saved videos aren't bookmarked in this context
                    onBookmarkToggle = null,
                    isSuperUser = uiState.isSuperUser, // Pass for consistency, though actions are gated by isCloudVideoItem
                    isCloudVideoItem = false,
                    isSelectionModeActive = false, // Selection mode is for cloud videos
                    isSelected = false,
                    onVideoLongClick = null,
                    onVideoShortClickInSelection = null,
                    onVideoShortClickForSingleDelete = null,
                    // Saved video specific parameters
                    showDeleteActionForSavedVideo = true,
                    onDeleteSavedVideoClick = { viewModel.deleteSavedVideo(video) }
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class) // Needed for combinedClickable
@Composable
fun VideoListItem(
    videoInfo: VideoInfo,
    onItemClick: () -> Unit, // For playback
    isBookmarked: Boolean? = null,
    onBookmarkToggle: (() -> Unit)? = null,
    // Superuser and selection related parameters for cloud videos
    isSuperUser: Boolean = false,
    isCloudVideoItem: Boolean = false,
    isSelectionModeActive: Boolean = false,
    isSelected: Boolean = false,
    onVideoLongClick: (() -> Unit)? = null,
    onVideoShortClickInSelection: (() -> Unit)? = null,
    onVideoShortClickForSingleDelete: (() -> Unit)? = null,
    // Parameters for saved video deletion
    onDeleteSavedVideoClick: (() -> Unit)? = null,
    showDeleteActionForSavedVideo: Boolean = false // True only for saved videos tab item
) {
    val cardBackgroundColor = if (isSelected && isSelectionModeActive && isCloudVideoItem) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        CardDefaults.cardColors().containerColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionModeActive) {
                        onVideoShortClickInSelection?.invoke()
                    } else {
                        onItemClick()
                    }
                },
                onLongClick = {
                    if (isSuperUser && isCloudVideoItem) {
                        onVideoLongClick?.invoke()
                    }
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator for cloud videos in selection mode for superuser
            if (isSuperUser && isCloudVideoItem && isSelectionModeActive) {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (isSelected) stringResource(R.string.selected_cd) else stringResource(R.string.not_selected_cd),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // delete single video super user
            if (isCloudVideoItem && isSuperUser) {
                IconButton(onClick = {
                    onVideoShortClickForSingleDelete?.invoke()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete_video_cd),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // Bookmark toggle for cloud videos (not in selection mode, or for non-superusers)
            if (isCloudVideoItem && (!isSelectionModeActive || !isSuperUser) ) { // Only show bookmark if not in selection mode OR not a superuser in selection mode
                isBookmarked?.let { bookmarked ->
                    onBookmarkToggle?.let { toggle ->
                        IconButton(onClick = toggle) {
                            Icon(
                                imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = stringResource(if (bookmarked) R.string.unbookmark_video_cd else R.string.bookmark_video_cd),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Delete icon for saved videos tab
            if (showDeleteActionForSavedVideo) {
                onDeleteSavedVideoClick?.let {
                    IconButton(onClick = it) {
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
                uiState.addVideoMessage?.let {
                    val isError = uiState.error != null
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
                enabled = !uiState.addVideoInProgress && uiState.newVideoTitle.isNotBlank() && uiState.newVideoUrl.isNotBlank() && uiState.newVideoTitleError == null && uiState.newVideoUrlError == null
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
