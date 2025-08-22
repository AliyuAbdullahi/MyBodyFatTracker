package com.lekan.bodyfattracker.ui.feedback

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.model.UserFeedback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("SimpleDateFormat")
fun formatTimestamp(timestamp: Date?, pattern: String = "dd MMM yyyy, HH:mm"): String {
    return timestamp?.let { SimpleDateFormat(pattern, Locale.getDefault()).format(it) } ?: "No date"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbacksTopAppBar(
    uiState: FeedbacksState,
    onNavigateBack: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onClearAll: () -> Unit,
    isAnyLoading: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
    val selectionCount = uiState.selectedFeedbackIds.size
    val inSelectionMode = selectionCount > 0

    TopAppBar(
        title = {
            Text(
                if (inSelectionMode) stringResource(R.string.items_selected_title, selectionCount)
                else stringResource(R.string.user_feedbacks_title)
            )
        },
        navigationIcon = {
            if (!inSelectionMode) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back_content_description)
                    )
                }
            } else {
                IconButton(onClick = onClearSelection, enabled = !isAnyLoading) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.clear_selection_action)
                    )
                }
            }
        },
        windowInsets = WindowInsets(0, 0, 0, 0),
        actions = {
            if (inSelectionMode) {
                IconButton(onClick = onDeleteSelected, enabled = !isAnyLoading) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete_selected_feedbacks_action)
                    )
                }
            } else {
                IconButton(onClick = { showMenu = !showMenu }, enabled = !isAnyLoading) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "More options"
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.clear_all_feedbacks_menu)) },
                        onClick = {
                            showMenu = false
                            onClearAll()
                        },
                        enabled = !isAnyLoading && !uiState.feedbackList.isEmpty()
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedbackItemCard(
    feedback: UserFeedback,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onDeleteSingle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .combinedClickable(
                onClick = { if (isSelected) onToggleSelection() /* Allow deselection by tap if already selected */ },
                onLongClick = onToggleSelection // Enter/toggle selection mode on long press
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(feedback.message, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    formatTimestamp(feedback.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    stringResource(
                        R.string.feedback_from_template,
                        feedback.deviceModel,
                        feedback.appVersion
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    stringResource(
                        R.string.device_info_template,
                        feedback.deviceModel,
                        feedback.androidVersion,
                        feedback.locale
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp
                )
            }
            IconButton(onClick = onDeleteSingle, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.delete_button_text),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String = stringResource(R.string.delete_button_text)
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    onDismiss()
                }) {
                    Text(confirmButtonText, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel_button_text))
                }
            }
        )
    }
}


@Composable
fun FeedbacksScreen(
    viewModel: FeedbacksViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteSingleDialog by remember { mutableStateOf<UserFeedback?>(null) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.operationMessage) {
        uiState.operationMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearOperationMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            FeedbacksTopAppBar(
                uiState = uiState,
                onNavigateBack = onNavigateBack,
                onClearSelection = { viewModel.clearSelection() },
                onDeleteSelected = { showDeleteSelectedDialog = true },
                onClearAll = { showClearAllDialog = true },
                isAnyLoading = uiState.isLoading || uiState.isDeleting
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading || uiState.isDeleting) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                if (uiState.isLoading && uiState.feedbackList.isEmpty()) {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator() // Show centered progress if loading and list is empty
                    }
                } else if (!uiState.isLoading && uiState.feedbackList.isEmpty() && uiState.operationMessage == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_feedback_submitted_yet))
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.feedbackList, key = { it.id }) { feedback ->
                            FeedbackItemCard(
                                feedback = feedback,
                                isSelected = uiState.selectedFeedbackIds.contains(feedback.id),
                                onToggleSelection = { viewModel.toggleFeedbackSelection(feedback.id) },
                                onDeleteSingle = { showDeleteSingleDialog = feedback }
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialogs
    ConfirmationDialog(
        showDialog = showDeleteSingleDialog != null,
        onDismiss = { showDeleteSingleDialog = null },
        onConfirm = {
            showDeleteSingleDialog?.let { viewModel.deleteSingleFeedback(it.id) }
        },
        title = stringResource(R.string.confirm_deletion_title),
        message = stringResource(
            R.string.confirm_delete_single_feedback_message,
            showDeleteSingleDialog?.message?.take(50) ?: "this item"
        )
    )

    ConfirmationDialog(
        showDialog = showDeleteSelectedDialog,
        onDismiss = { showDeleteSelectedDialog = false },
        onConfirm = { viewModel.deleteSelectedFeedbacks() },
        title = stringResource(R.string.confirm_deletion_title),
        message = stringResource(
            R.string.confirm_delete_selected_message,
            uiState.selectedFeedbackIds.size
        )
    )

    ConfirmationDialog(
        showDialog = showClearAllDialog,
        onDismiss = { showClearAllDialog = false },
        onConfirm = { viewModel.clearAllFeedbacks() },
        title = stringResource(R.string.confirm_deletion_title),
        message = stringResource(R.string.confirm_clear_all_feedbacks_message),
        confirmButtonText = stringResource(R.string.clear_all_button_text)
    )
}
