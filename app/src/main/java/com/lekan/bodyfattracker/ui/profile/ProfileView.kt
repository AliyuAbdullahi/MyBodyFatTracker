package com.lekan.bodyfattracker.ui.profile

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.lekan.bodyfattracker.BuildConfig
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.model.UserProfile
import com.lekan.bodyfattracker.model.WeightUnit
import com.lekan.bodyfattracker.ui.ads.AdmobBanner
import com.lekan.bodyfattracker.ui.home.Gender
import com.lekan.bodyfattracker.ui.profile.components.FeedbackDialog
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme

enum class ProfileContent {
    OVERVIEW, FORM
}

const val AboutPageUri = "https://aliyuabdullahi.github.io/MyBodyFatTracker/privacy_policy"


private fun showToast(context: android.content.Context, message: String) {
    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    openFeedbacksScreen: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()
    var currentTargetState by remember { mutableStateOf(ProfileContent.OVERVIEW) }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(uiState.profileSavedSuccessfully) {
        if (uiState.profileSavedSuccessfully) {
            showToast(context, context.getString(R.string.profile_saved_successfully))
            viewModel.dismissSuccessMessage()
            currentTargetState = ProfileContent.OVERVIEW
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            showToast(context, it)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(uiState.feedbackSentMessage) {
        uiState.feedbackSentMessage?.let { message ->
            showToast(context, message)
            viewModel.clearFeedbackMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.profile_destination)) },
                actions = {
                    if (currentTargetState == ProfileContent.OVERVIEW && uiState.userProfile != null) {
                        IconButton(onClick = {
                            viewModel.onEditProfile()
                            currentTargetState = ProfileContent.FORM
                        }) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.edit_profile_content_description)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        AnimatedContent(
            targetState = currentTargetState,
            modifier = Modifier.padding(paddingValues),
            transitionSpec = {
                if (targetState == ProfileContent.FORM && initialState == ProfileContent.OVERVIEW) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            }, label = "ProfileScreenAnimation"
        ) { targetState ->
            when (targetState) {
                ProfileContent.OVERVIEW -> {
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.userProfile == null) {
                        // Show a prompt to create a profile if it doesn't exist
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_profile_found),
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                viewModel.onCreateProfileClicked()
                                currentTargetState = ProfileContent.FORM
                            }) {
                                Text(stringResource(R.string.create_profile_button))
                            }
                        }
                    } else {
                        ProfileOverview(
                            userProfile = uiState.userProfile!!, // Safe as we checked for null
                            onShowAboutSheet =  {
                                viewModel.onAboutAppClicked()
                            },
                            onShowPrivacySheet = {
                                uriHandler.openUri(AboutPageUri)
                            },
                            onLeaveFeedbackClicked = viewModel::onLeaveFeedbackClicked,
                            selectedDisplayWeightUnit = uiState.selectedDisplayWeightUnit,
                            onWeightUnitSelected = viewModel::updateDisplayWeightUnit,
                            isSuperUser = uiState.isSuperUser,
                            onSeeFeedbacksClicked = {
                                openFeedbacksScreen()
                            }
                        )
                    }
                }

                ProfileContent.FORM -> {
                    val context = LocalContext.current
                    ProfileForm(
                        uiState = uiState,
                        onNameChange = viewModel::onNameChanged,
                        onAgeChange = viewModel::onAgeChanged,
                        onGenderSelected = viewModel::onGenderSelected,
                        onBodyFatGoalChange = viewModel::onBodyFatGoalChanged,
                        onPhotoSelected = { uri ->
                            viewModel.onPhotoSelected(
                                uri, context, failedToSaveProfileMessage = context.getString(
                                    R.string.photo_saving_failed
                                )
                            )
                        },
                        onSaveClicked = {
                            viewModel.saveProfile(
                                invalidAgeMessage = context.getString(R.string.invalid_age),
                                selectAGenderMessage = "Select Gender",
                                invalidBodyFatGoalMessage = "Invalid Body Fat Goal",
                            )
                        },
                        onCancelEditClicked = {
                            viewModel.cancelEditProfile()
                            currentTargetState = ProfileContent.OVERVIEW
                        },
                        onShowAboutSheet = {
                            viewModel.onAboutAppClicked()
                        },
                        onShowPrivacySheet = {
                            uriHandler.openUri(AboutPageUri)
                        },
                        onLeaveFeedbackClicked = viewModel::onLeaveFeedbackClicked,
                        isSuperUser = uiState.isSuperUser,
                        onSeeFeedbacksClicked = {
                            openFeedbacksScreen()
                        }
                    )
                }
            }
            AnimatedVisibility(
                visible = uiState.showAboutSheet,
                enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }) + fadeOut()
            ) {
                AboutAppBottomSheet(
                    onDismiss = {
                        viewModel.onDismissAboutApp()
                    }
                )
            }

            if (uiState.showFeedbackDialog) {
                FeedbackDialog(
                    showDialog = uiState.showFeedbackDialog,
                    feedbackText = uiState.feedbackDialogText,
                    onFeedbackTextChanged = {
                        viewModel.onFeedbackDialogTextChanged(it)
                    },
                    onDismissRequest = {
                        viewModel.onDismissFeedbackDialog()
                    },
                    onSendFeedback = {
                        viewModel.sendFeedback(
                            feedbackEmptyMessage = context.getString(R.string.feedback_is_empty),
                            feedbackSentMessage = context.getString(R.string.feedback_sent_successfully),
                            failedToSendMessage = context.getString(R.string.failed_to_send_feedback)
                        )
                    },
                    isSending = uiState.isSendingFeedback
                )
            }
        }
    }
}

@Composable
fun ProfileOverview(
    userProfile: UserProfile,
    onShowAboutSheet: () -> Unit,
    onShowPrivacySheet: () -> Unit,
    onLeaveFeedbackClicked: () -> Unit,
    selectedDisplayWeightUnit: WeightUnit,
    onWeightUnitSelected: (WeightUnit) -> Unit,
    onSeeFeedbacksClicked: () -> Unit = {},
    isSuperUser: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = userProfile.photoPath,
            contentDescription = stringResource(R.string.profile_picture_content_description),
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(userProfile.name, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.age_display_format, "${userProfile.age}"),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.gender_display_format, userProfile.gender.name),
            style = MaterialTheme.typography.bodyLarge
        )
        userProfile.bodyFatPercentGoal?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.body_fat_goal_display_format, "$it"),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
//        Divider()
//
//        DisplayWeightUnitSelection(
//            selectedUnit = selectedDisplayWeightUnit,
//            onUnitSelected = onWeightUnitSelected
//        )
        Divider()

        ListItem(
            headlineContent = { Text(stringResource(R.string.profile_about_app_link)) },
            leadingContent = { Icon(Icons.AutoMirrored.Outlined.HelpOutline, null) },
            modifier = Modifier.clickable(onClick = onShowAboutSheet)
        )
        Divider()
        ListItem(
            headlineContent = { Text(stringResource(R.string.privacy_policy_title)) },
            leadingContent = { Icon(Icons.Outlined.GppGood, null) },
            modifier = Modifier.clickable(onClick = onShowPrivacySheet)
        )
        Divider()
        ListItem(
            headlineContent = { Text(stringResource(R.string.leave_feedback_label)) },
            leadingContent = { Icon(Icons.Outlined.Feedback, null) },
            modifier = Modifier.clickable(onClick = onLeaveFeedbackClicked)
        )
        Divider()
        if (isSuperUser) {
            Divider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.see_feedbacks_label)) },
                leadingContent = { Icon(Icons.Outlined.Feedback, null) },
                modifier = Modifier.clickable(onClick = onSeeFeedbacksClicked)
            )
            Divider()
        }

        // Add more ListItems for other settings if needed
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileForm(
    uiState: ProfileScreenUiState,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onGenderSelected: (Gender) -> Unit,
    onBodyFatGoalChange: (String) -> Unit,
    onPhotoSelected: (Uri?) -> Unit,
    onSaveClicked: () -> Unit,
    onCancelEditClicked: () -> Unit,
    onShowAboutSheet: () -> Unit,
    onShowPrivacySheet: () -> Unit,
    onLeaveFeedbackClicked: () -> Unit,
    isSuperUser: Boolean = false,
    onSeeFeedbacksClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onPhotoSelected(uri) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable {
                    singlePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
            AsyncImage(
                model = uiState.photoPath,
                contentDescription = stringResource(R.string.profile_picture_content_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Icon(
                imageVector = Icons.Rounded.PhotoCamera,
                contentDescription = stringResource(R.string.select_photo_content_description),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.nameInput,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.name_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.ageInput,
            onValueChange = onAgeChange,
            label = { Text(stringResource(R.string.age_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.selectedGender?.name
                    ?: stringResource(R.string.select_gender_placeholder),
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(R.string.gender_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Gender.entries.forEach { gender ->
                    DropdownMenuItem(
                        text = { Text(gender.name) },
                        onClick = {
                            onGenderSelected(gender)
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.bodyFatGoalInput,
            onValueChange = onBodyFatGoalChange,
            label = { Text(stringResource(R.string.body_fat_goal_optional_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onCancelEditClicked,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.cancel_button)) }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onSaveClicked,
                enabled = uiState.canSave,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.save_button)) }
        }

        Spacer(modifier = Modifier.height(24.dp))
//        Divider()

//        DisplayWeightUnitSelection(
//            selectedUnit = selectedDisplayWeightUnit,
//            onUnitSelected = onWeightUnitSelected
//        )
//        Divider()
//
//        ListItem(
//            headlineContent = { Text(stringResource(R.string.notifications_label)) },
//            leadingContent = { Icon(Icons.Outlined.Notifications, null) },
//            modifier = Modifier.clickable { /* TODO: Navigate to notification settings */ }
//        )
        Divider()
        ListItem(
            headlineContent = { Text(stringResource(R.string.profile_about_app_link)) },
            leadingContent = { Icon(Icons.AutoMirrored.Outlined.HelpOutline, null) },
            modifier = Modifier.clickable(onClick = onShowAboutSheet)
        )
        Divider()
        ListItem(
            headlineContent = { Text(stringResource(R.string.privacy_policy_title)) },
            leadingContent = { Icon(Icons.Outlined.GppGood, null) },
            modifier = Modifier.clickable(onClick = onShowPrivacySheet)
        )
        Divider()
        ListItem(
            headlineContent = { Text(stringResource(R.string.leave_feedback_label)) },
            leadingContent = { Icon(Icons.Outlined.Feedback, null) },
            modifier = Modifier.clickable(onClick = onLeaveFeedbackClicked)
        )
        Divider()

        if (isSuperUser) {
            Divider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.see_feedbacks_label)) },
                leadingContent = { Icon(Icons.Outlined.Feedback, null) },
                modifier = Modifier.clickable(onClick = onSeeFeedbacksClicked)
            )
            Divider()
        }

        // AdMob Banner Placeholder - Assuming you have AdView composable
        // com.google.android.gms.ads.AdView
        // For now, a simple placeholder
        Spacer(modifier = Modifier.height(16.dp))
        AdmobBanner(modifier = Modifier.fillMaxWidth(), adUnitId = BuildConfig.PROFILE_BANNER_AD_UNIT_ID)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayWeightUnitSelection(
    selectedUnit: WeightUnit,
    onUnitSelected: (WeightUnit) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.display_weight_in_label),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            WeightUnit.entries.forEach { unit ->
                SegmentedButton(
                    selected = unit == selectedUnit,
                    onClick = { onUnitSelected(unit) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = unit.ordinal,
                        count = WeightUnit.entries.size
                    ),
                    icon = {
                        SegmentedButtonDefaults.Icon(
                            active = unit == selectedUnit,
                            activeContent = {
                                Icon(
                                    Icons.Rounded.MonitorWeight,
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                )
                            },
                            inactiveContent = {
                                Icon(
                                    Icons.Rounded.MonitorWeight,
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                )
                            }
                        )
                    }
                ) {
                    Text(unit.name)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Profile Screen Overview Light")
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Profile Screen Overview Dark"
)
@Composable
fun ProfileScreenPreviewOverview() {
    BodyFatTrackerTheme {
        val previewUserProfile = UserProfile(
            name = "Jane Doe",
            age = 30,
            gender = Gender.FEMALE,
            bodyFatPercentGoal = 20,
            photoPath = null // Or a drawable resource
        )
        val uiState = ProfileScreenUiState(
            isLoading = false,
            userProfile = previewUserProfile,
            isSuperUser = true
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) { Icon(Icons.Outlined.Edit, "Edit") }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                ProfileOverview(
                    userProfile = uiState.userProfile!!,
                    onShowAboutSheet = { },
                    onShowPrivacySheet = { },
                    onLeaveFeedbackClicked = {},
                    selectedDisplayWeightUnit = WeightUnit.KG,
                    onWeightUnitSelected = {},
                    isSuperUser = uiState.isSuperUser
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Profile Screen Form Light")
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Profile Screen Form Dark"
)
@Composable
fun ProfileScreenPreviewForm() {
    BodyFatTrackerTheme {
        val uiState = ProfileScreenUiState(
            isLoading = false,
            userProfile = null,
            isEditing = true,
            nameInput = "John Doe",
            ageInput = "25",
            selectedGender = Gender.MALE,
            canSave = true,
            isSuperUser = true
        )
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Profile") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                ProfileForm(
                    uiState = uiState,
                    onNameChange = {},
                    onAgeChange = {},
                    onGenderSelected = {},
                    onBodyFatGoalChange = {},
                    onPhotoSelected = {},
                    onSaveClicked = {},
                    onCancelEditClicked = {},
                    onShowAboutSheet = {},
                    onShowPrivacySheet = {},
                    onLeaveFeedbackClicked = {},
                )
            }
        }
    }
}
