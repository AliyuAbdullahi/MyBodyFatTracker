package com.lekan.bodyfattracker.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.ui.home.Gender
import com.lekan.bodyfattracker.ui.home.measurement.components.GenderSelector
import com.lekan.bodyfattracker.ui.theme.BodyFatTrackerTheme
import java.io.File

// ... ProfileScreenUiState (if in a separate file, or defined in ViewModel)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    // ActivityResultLauncher for picking an image
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onPhotoSelected(uri, context)
    }

    // Launcher for requesting permissions
    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        // Check if the necessary permission was granted (either full or partial)
        val readMediaImagesGranted = permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
        val readVisualUserSelectedGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+ for selected photos
            permissions[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true
        } else {
            false // Not applicable before API 34, or assume covered by READ_MEDIA_IMAGES if older logic is used
        }
        // For older devices, if READ_EXTERNAL_STORAGE was requested
        val readExternalStorageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true


        if (readMediaImagesGranted || readVisualUserSelectedGranted || readExternalStorageGranted) {
            // Permission granted (either full or partial access which GetContent handles)
            imagePickerLauncher.launch("image/*")
        } else {
            // Permission denied: Inform the user
            viewModel.onPermissionDenied("Storage permission is required to select a photo.")
        }
    }

    // Function to check and request permissions, then launch picker
    val launchImagePickerWithPermissionCheck = {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            // On API 34+, request both. If user selects "Selected Photos",
            // READ_MEDIA_VISUAL_USER_SELECTED is granted.
            // If they select "Allow", READ_MEDIA_IMAGES is granted.
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else { // Below API 33 (Android 12 and lower)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isEmpty()) {
            // Permissions already granted or not needed for this flow on this API level by GetContent()
            imagePickerLauncher.launch("image/*")
        } else {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }
    LaunchedEffect(uiState.profileSavedSuccessfully) {
        if (uiState.profileSavedSuccessfully) {
            snackbarHostState.showSnackbar(message = "Profile saved successfully!")
            viewModel.dismissSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { // Optional: if you want a consistent TopAppBar

        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.showCreateProfileButton -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyProfileView(onCreateProfileClicked = viewModel::onCreateProfileClicked)
                    }
                }
                uiState.isEditing -> { // Show Form if editing or creating (after create button clicked)
                    ProfileForm(
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
                            launchImagePickerWithPermissionCheck()
                        },
                        onSaveClicked = viewModel::saveProfile,
                        canSave = uiState.canSave,
                        isEditingOrCreating = true // Pass true to show save button logic
                    )
                }
                uiState.userProfile != null && !uiState.isEditing -> { // Show Overview if profile exists and not editing
                    ProfileOverview(
                        name = uiState.userProfile!!.name, // Safe due to userProfile != null check
                        photoPath = uiState.userProfile!!.photoPath,
                        bodyFatGoal = uiState.userProfile!!.bodyFatPercentGoal?.let { "$it %" }
                            ?: stringResource(R.string.not_set_placeholder),
                        gender = uiState.userProfile!!.gender,
                        onEditProfileClicked = viewModel::onEditProfile
                    )
                }
                else -> {
                    // Fallback, should ideally be covered by isLoading or showCreateProfileButton
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Something went wrong or no profile state.")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileForm(
    name: String,
    age: String,
    bodyFatGoalInput: String,
    selectedGender: Gender?,
    photoPath: String?, // <<< ADD photoPath
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onBodyFatGoalChange: (String) -> Unit,
    onGenderSelect: (Gender) -> Unit,
    onPhotoPickerClick: () -> Unit, // <<< ADD callback for photo picker
    onSaveClicked: () -> Unit,
    canSave: Boolean,
    isEditingOrCreating: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        Text(
            text = stringResource(R.string.user_profile_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp) // Reduced bottom padding a bit
        )

        // --- Profile Photo Picker ---
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

                val imageRequest = remember(photoPath) { // Rebuild request when photoPath changes
                    ImageRequest.Builder(context)
                        .data(File(photoPath)) // Coil loads from File path
                        .crossfade(true)
                        .placeholder(R.drawable.camera_image) // Optional: from previous response
                        .error(R.drawable.camera_image)       // Optional: from previous response
                        // To bust caches more aggressively if needed, though usually not required for local files:
                        // .memoryCacheKey(photoPath) // Use the path as memory cache key
                        // .diskCacheKey(photoPath)   // Use the path as disk cache key
                        // .networkCachePolicy(CachePolicy.DISABLED) // Not for local files
                        // .diskCachePolicy(CachePolicy.DISABLED)    // Could try if still caching too aggressively
                        // .memoryCachePolicy(CachePolicy.DISABLED)  // Could try if still caching too aggressively
                        .build()
                }

                Image(
                    painter = rememberAsyncImagePainter(
                        model = imageRequest
                    ),
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
        // --- End Profile Photo Picker ---

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
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = bodyFatGoalInput,
            onValueChange = onBodyFatGoalChange,
            label = { Text(stringResource(R.string.body_fat_goal_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            placeholder = { Text(stringResource(R.string.optional_placeholder)) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.gender_label),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        GenderSelector(
            modifier = Modifier.fillMaxWidth(),
            selectedGender = selectedGender ?: Gender.FEMALE,
            onGenderSelected = onGenderSelect
        )
        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(visible = isEditingOrCreating && canSave) {
            Button(
                onClick = onSaveClicked,
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_button))
            }
        }
    }
}

// ... EmptyProfileView remains the same ...

// --- Previews (Update as needed) ---
@Preview(showBackground = true, name = "Profile Screen - Form View (New Profile)")
@Composable
fun ProfileScreenFormNewPreview() {
    BodyFatTrackerTheme {
        ProfileForm(
            name = "", age = "", bodyFatGoalInput = "", selectedGender = null, photoPath = null,
            onNameChange = {}, onAgeChange = {}, onBodyFatGoalChange = {}, onGenderSelect = {},
            onPhotoPickerClick = {}, onSaveClicked = {}, canSave = false, isEditingOrCreating = true
        )
    }
}

@Preview(showBackground = true, name = "Profile Screen - Form View (With Photo)")
@Composable
fun ProfileScreenFormWithPhotoPreview() {
    BodyFatTrackerTheme {
        // For preview, you won't have a real file path.
        // You might use a placeholder drawable or skip showing an actual image in preview for file paths.
        // Or, for a more involved preview, you could save a sample image to assets and load it.
        // For simplicity, passing null here.
        ProfileForm(
            name = "Jane Doe", age = "30", bodyFatGoalInput = "20.5", selectedGender = Gender.FEMALE,
            photoPath = null, // In a real scenario, this would be a file path.
            onNameChange = {}, onAgeChange = {}, onBodyFatGoalChange = {}, onGenderSelect = {},
            onPhotoPickerClick = {}, onSaveClicked = {}, canSave = true, isEditingOrCreating = true
        )
    }
}