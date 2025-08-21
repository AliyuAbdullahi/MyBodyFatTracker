package com.lekan.bodyfattracker.ui.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.ui.ads.AdmobBanner
import com.lekan.bodyfattracker.ui.home.Gender
import java.io.File

// Define this enum at the top level of the file or inside a relevant scope
private enum class ProfileContent {
    OVERVIEW, FORM, EMPTY_FALLBACK
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateUp: (() -> Unit)? = null
) {
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current // General context for Toasts etc.

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { viewModel.onPhotoSelected(it, context.applicationContext) }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                photoPickerLauncher.launch("image/*")
            } else {
                viewModel.onPermissionDenied("Storage permission is required to select a photo.")
            }
        }
    )

    LaunchedEffect(uiState.profileSavedSuccessfully) {
        if (uiState.profileSavedSuccessfully) {
            Toast.makeText(context, "Profile Saved!", Toast.LENGTH_SHORT).show()
            viewModel.dismissSuccessMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.profile_title)) },
                navigationIcon = {
                    if (onNavigateUp != null) {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isEditing && !uiState.showCreateProfileButton && uiState.userProfile != null) {
                FloatingActionButton(onClick = viewModel::onEditProfile) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.edit_profile_button)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        if (uiState.isLoading && uiState.userProfile?.name?.isNotBlank()
                ?.let { !it } == true && !uiState.isEditing && !uiState.showCreateProfileButton
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val targetContentState = when {
                uiState.isEditing || uiState.showCreateProfileButton -> ProfileContent.FORM
                uiState.userProfile != null -> ProfileContent.OVERVIEW
                else -> ProfileContent.EMPTY_FALLBACK
            }

            AnimatedContent(
                targetState = targetContentState,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(), // Apply padding here
                transitionSpec = {
                    if (targetState == ProfileContent.FORM && initialState == ProfileContent.OVERVIEW) {
                        slideInHorizontally { fullWidth -> fullWidth } + fadeIn(
                            animationSpec = tween(
                                300
                            )
                        ) togetherWith
                                slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut(
                            animationSpec = tween(300)
                        )
                    } else if (targetState == ProfileContent.OVERVIEW && initialState == ProfileContent.FORM) {
                        slideInHorizontally { fullWidth -> -fullWidth } + fadeIn(
                            animationSpec = tween(
                                300
                            )
                        ) togetherWith
                                slideOutHorizontally { fullWidth -> fullWidth } + fadeOut(
                            animationSpec = tween(300)
                        )
                    } else {
                        fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                                fadeOut(animationSpec = tween(90))
                    }
                },
                label = "ProfileContentAnimation"
            ) { currentTargetState ->
                // Obtain LocalContext here as it's needed by the branches for the Intent
                val intentContext = LocalContext.current
                when (currentTargetState) {
                    ProfileContent.FORM -> {
                        ProfileForm(
                            modifier = Modifier.fillMaxSize(), // Let ProfileForm fill AnimatedContent
                            name = uiState.nameInput,
                            age = uiState.ageInput,
                            bodyFatGoalInput = uiState.bodyFatGoalInput,
                            selectedGender = uiState.selectedGender,
                            photoPath = uiState.photoPath,
                            onNameChange = viewModel::onNameChanged,
                            onAgeChange = viewModel::onAgeChanged,
                            onBodyFatGoalChange = viewModel::onBodyFatGoalChanged,
                            onGenderSelect = viewModel::onGenderSelected,
                            onPhotoPickerClick = {
                                val permission =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        Manifest.permission.READ_MEDIA_IMAGES
                                    } else {
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    }
                                permissionLauncher.launch(permission)
                            },
                            onSaveClicked = viewModel::saveProfile,
                            onCancelEditClicked = viewModel::cancelEditProfile,
                            canSave = uiState.canSave,
                            isEditingOrCreating = uiState.isEditing || uiState.showCreateProfileButton,
                            onAboutAppClicked = viewModel::onAboutAppClicked,
                            onPrivacyPolicyClicked = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aliyuabdullahi.github.io/MyBodyFatTracker/privacy_policy"))
                                intentContext.startActivity(intent)
                            }
                        )
                    }

                    ProfileContent.OVERVIEW -> {
                        uiState.userProfile?.let { // Ensure userProfile is not null
                            ProfileOverview(
                                modifier = Modifier.fillMaxSize(), // Let ProfileOverview fill AnimatedContent
                                name = it.name,
                                age = it.age.toString(),
                                photoPath = it.photoPath,
                                bodyFatGoal = it.bodyFatPercentGoal?.toString() ?: "N/A",
                                gender = it.gender,
                                onAboutAppClicked = viewModel::onAboutAppClicked,
                                onPrivacyPolicyClicked = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aliyuabdullahi.github.io/MyBodyFatTracker/privacy_policy"))
                                    intentContext.startActivity(intent)
                                }
                            )
                        } ?: Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { // Fallback if userProfile somehow null
                            Text("Profile data is unavailable.")
                            Button(onClick = viewModel::onCreateProfileClicked) {
                                Text("Create Profile")
                            }
                        }
                    }

                    ProfileContent.EMPTY_FALLBACK -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoading) { // Show loading specifically for this case
                                CircularProgressIndicator()
                            } else {
                                Text(
                                    "No profile found. Create one to get started!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = viewModel::onCreateProfileClicked) {
                                    Text("Create Profile")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showAboutSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = viewModel::onDismissAboutApp,
                sheetState = sheetState
            ) {
                AboutAppBottomSheet(onDismiss = viewModel::onDismissAboutApp)
            }
        }

        if (uiState.showPrivacyPolicySheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = viewModel::onDismissPrivacyPolicy,
                sheetState = sheetState
            ) {
                PrivacyPolicyBottomSheet(onDismiss = viewModel::onDismissPrivacyPolicy)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileForm(
    modifier: Modifier = Modifier,
    name: String,
    age: String,
    bodyFatGoalInput: String,
    selectedGender: Gender?,
    photoPath: String?,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onBodyFatGoalChange: (String) -> Unit,
    onGenderSelect: (Gender) -> Unit,
    onPhotoPickerClick: () -> Unit,
    onSaveClicked: () -> Unit,
    onCancelEditClicked: () -> Unit,
    canSave: Boolean,
    isEditingOrCreating: Boolean,
    onAboutAppClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit
) {
    Column(
        modifier = modifier // Use the modifier passed from AnimatedContent
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        Text(
            text = if (isEditingOrCreating && name.isBlank() && age.isBlank()) stringResource(R.string.create_profile_label)
            else stringResource(R.string.edit_profile_label),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { onPhotoPickerClick() },
            contentAlignment = Alignment.Center
        ) {
            if (photoPath != null) {
                val imageRequest = remember(photoPath) {
                    ImageRequest.Builder(context)
                        .data(File(photoPath))
                        .crossfade(true)
                        .placeholder(R.drawable.camera_image)
                        .error(R.drawable.camera_image)
                        .build()
                }
                Image(
                    painter = rememberAsyncImagePainter(model = imageRequest),
                    contentDescription = stringResource(R.string.add_profile_photo_desc),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.camera_image),
                    contentDescription = stringResource(R.string.add_profile_photo_desc),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = age,
            onValueChange = onAgeChange,
            label = { Text(stringResource(R.string.age_label)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        var genderDropdownExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = genderDropdownExpanded,
            onExpandedChange = { genderDropdownExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedGender?.name
                    ?: "Select Gender", // Ensure Gender enum has displayName
                onValueChange = {},
                label = { Text("Gender") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { genderDropdownExpanded = !genderDropdownExpanded }) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderDropdownExpanded)
                    }
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = genderDropdownExpanded,
                onDismissRequest = { genderDropdownExpanded = false }
            ) {
                Gender.entries.forEach { gender -> // Ensure Gender.entries exists
                    DropdownMenuItem(
                        text = { Text(gender.name) }, // Ensure Gender enum has displayName
                        onClick = {
                            onGenderSelect(gender)
                            genderDropdownExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = bodyFatGoalInput,
            onValueChange = onBodyFatGoalChange,
            label = { Text(stringResource(R.string.body_fat_goal_optional_label)) }, // Ensure this string exists
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (isEditingOrCreating) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End, // Or Arrangement.SpaceBetween, or use Spacers
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancelEditClicked) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.weight(1f)) // Pushes save to the end if Arrangement.End is not enough
                Button(
                    onClick = onSaveClicked,
                    enabled = canSave
                ) {
                    Text(stringResource(R.string.save_button)) // Ensure this string exists
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Added space before About/Privacy for clarity

        TextButton(
            onClick = onAboutAppClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("About App")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onPrivacyPolicyClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Privacy Policy")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileOverview(
    modifier: Modifier = Modifier,
    name: String,
    age: String,
    photoPath: String?,
    bodyFatGoal: String,
    gender: Gender,
    onAboutAppClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit
) {
    // val context = LocalContext.current // Not needed here if onPrivacyPolicyClicked handles context
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp), // Overall padding for the content
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Centered Profile Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant) // Placeholder background
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) // Optional border
        ) {
            AsyncImage(
                model = photoPath,
                contentDescription = stringResource(R.string.profile_picture_description), // Accessibility
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.camera_image), // Placeholder image
                error = painterResource(R.drawable.camera_image), // Error image
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Centered Title (User's Name)
        Text(
            text = name, // User's name as the main title below the image
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // --- Left-aligned informational items ---
        Column(modifier = Modifier.fillMaxWidth()) {

            // Age
            Text(
                text = stringResource(R.string.profile_age_display, age), // Example: "Age: 30"
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
            AdmobBanner(modifier = Modifier.fillMaxWidth())
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // Gender
            Text(
                text = stringResource(
                    R.string.profile_gender_display,
                    gender.toString()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }), // Example: "Gender: Male"
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // Body Fat Goal
            Text(
                text = stringResource(
                    R.string.profile_body_fat_goal_display,
                    bodyFatGoal
                ), // Example: "Body Fat Goal: 15%"
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // About App
            Text(
                text = stringResource(R.string.profile_about_app_link), // Example: "About App"
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAboutAppClicked)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ) // Slightly more padding for clickable items
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // Privacy Policy
            Text(
                text = stringResource(R.string.profile_privacy_policy_link), // Example: "Privacy Policy"
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPrivacyPolicyClicked)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
            // No divider after the last item for a cleaner look
        }
        Spacer(modifier = Modifier.height(16.dp)) // Spacer at the bottom for scroll padding
    }
}
