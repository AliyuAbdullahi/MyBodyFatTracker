package com.lekan.bodyfattracker.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.lekan.bodyfattracker.BuildConfig
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.ui.ads.AdmobBanner
import com.lekan.bodyfattracker.ui.home.components.LatestMeasurementCard
import com.lekan.bodyfattracker.ui.home.components.LatestWeightEntryCard
import com.lekan.bodyfattracker.ui.home.components.TimePickerDialog
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAddWeightEntry: () -> Unit,
    onStartSevenSites: () -> Unit,
    onStartThreeSites: () -> Unit,
    onStartThreeSitesGuest: () -> Unit,
    onStartSevenSitesGuest: () -> Unit
) {
    val titleList = listOf(R.string.let_get_started, R.string.let_work_on_your_goals, R.string.let_work_on_your_goals)
    val uiState by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isLoading = uiState.isLoading
    val userProfile = uiState.userProfile
    val latestMeasurement = uiState.latestMeasurement
    val latestWeightEntry = uiState.latestWeightEntry
    val recentWeightEntries = uiState.recentWeightEntries

    var isFabExpanded by remember { mutableStateOf(false) }
    val fabRotation by animateFloatAsState(targetValue = if (isFabExpanded) 45f else 0f, label = "fab_rotation")

    var showTimePickerDialog by remember { mutableStateOf(false) }

    if (showTimePickerDialog) {
        val currentTime = Calendar.getInstance()
        val initialHour = uiState.reminderHour ?: currentTime.get(Calendar.HOUR_OF_DAY)
        val initialMinute = uiState.reminderMinute ?: currentTime.get(Calendar.MINUTE)
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            // Consider making is24Hour configurable or based on system settings
            is24Hour = false
        )

        TimePickerDialog(
            onCancel = { showTimePickerDialog = false },
            onConfirm = { calendar ->
                viewModel.updateReminderTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
                showTimePickerDialog = false
            },
            timeState = timePickerState
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.setReminderEnabled(true)
            // Optionally, if no time is set, prompt to set time:
            if (uiState.reminderHour == null || uiState.reminderMinute == null) {
                showTimePickerDialog = true // Assuming showTimePickerDialog state exists
            }
        } else {
            // Permission denied. Show a rationale to the user.
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.notification_permission_denied_rationale),
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleList[uiState.messageIndex])) },
                windowInsets = WindowInsets(0),
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                onStartThreeSites()
                                isFabExpanded = false
                            },
                            icon = { Icon(Icons.Filled.SquareFoot, "Add 3-Site Measurement") },
                            text = { Text("3-Site") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                onStartSevenSites()
                                isFabExpanded = false
                            },
                            icon = { Icon(Icons.Filled.Straighten, "Add 7-Site Measurement") },
                            text = { Text("7-Site") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                onNavigateToAddWeightEntry()
                                isFabExpanded = false
                            },
                            icon = { Icon(Icons.Filled.FitnessCenter, "Add Weight Entry") },
                            text = { Text("Weight") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                onStartThreeSitesGuest()
                                isFabExpanded = false
                            },
                            icon = { Icon(Icons.Filled.SquareFoot, "Add 3-Site Guest Measurement") },
                            text = { Text("3-Site Guest") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                onStartSevenSitesGuest()
                                isFabExpanded = false
                            },
                            icon = { Icon(Icons.Filled.Straighten, "Add 7-Site Guest Measurement") },
                            text = { Text("7-Site Guest") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isFabExpanded) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = if (isFabExpanded) "Close FAB" else "Open FAB",
                        modifier = Modifier.rotate(fabRotation)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                } else {
                    if (userProfile != null) {
                        // REMOVED: HorizontalDivider()
                        Spacer(modifier = Modifier.height(4.dp)) // Reduced from 8.dp

                        // Reminder Section
                       Row (
                           modifier = Modifier.fillMaxWidth(),
                           verticalAlignment = Alignment.CenterVertically,
                           horizontalArrangement = Arrangement.SpaceBetween
                       ){
                           Text(
                               text = stringResource(R.string.measurement_reminder_title),
                               style = MaterialTheme.typography.titleMedium,
                               modifier = Modifier.weight(1F)
                           )
                           // Inside the Row for the Reminder Switch:
                           Switch(
                               checked = uiState.isReminderEnabled,
                               onCheckedChange = { desiredState ->
                                   if (desiredState) { // Trying to enable the reminder
                                       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                           when (ContextCompat.checkSelfPermission(
                                               context,
                                               Manifest.permission.POST_NOTIFICATIONS
                                           )) {
                                               PackageManager.PERMISSION_GRANTED -> {
                                                   viewModel.setReminderEnabled(true)
                                               }
                                               else -> {
                                                   // Launch the permission request
                                                   permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                               }
                                           }
                                       } else {
                                           viewModel.setReminderEnabled(true)
                                       }
                                   } else {
                                       // Trying to disable the reminder
                                       viewModel.setReminderEnabled(false)
                                   }
                               }
                           )

                       }

                        AnimatedVisibility(visible = uiState.isReminderEnabled) {
                            Column {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = { showTimePickerDialog = true })
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val timeText = if (uiState.reminderHour != null && uiState.reminderMinute != null) {
                                        String.format(Locale.getDefault(), "%02d:%02d", uiState.reminderHour, uiState.reminderMinute)
                                    } else {
                                        stringResource(R.string.set_reminder_time_action)
                                    }
                                    Text(
                                        text = stringResource(R.string.reminder_set_for_prefix, timeText),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = stringResource(R.string.set_reminder_time_action),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp)) // Reduced from 8.dp
                        // REMOVED: HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp)) // This spacer might still be needed before the next card
                    } else {
                        Text(stringResource(R.string.no_profile_setup))
                    }

                    LatestMeasurementCard(
                        latestMeasurement = latestMeasurement,
                        userProfile = uiState.userProfile
                    )

                    AdmobBanner(modifier = Modifier.fillMaxWidth(), adUnitId = BuildConfig.HOME_BANNER_AD_UNIT_ID)

                    LatestWeightEntryCard(
                        latestWeightEntry = latestWeightEntry,
                        recentWeightEntries = recentWeightEntries,
                        onAddWeightEntryClick = onNavigateToAddWeightEntry
                    )
                }
            }
            if (isFabExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.6f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isFabExpanded = false }
                        )
                )
            }
        }
    }
}
