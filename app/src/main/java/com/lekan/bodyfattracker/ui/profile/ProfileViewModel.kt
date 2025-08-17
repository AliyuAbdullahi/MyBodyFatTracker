package com.lekan.bodyfattracker.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.domain.IProfileRepository
import com.lekan.bodyfattracker.model.UserProfile
import com.lekan.bodyfattracker.ui.core.CoreViewModel
import com.lekan.bodyfattracker.ui.home.Gender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

data class ProfileScreenUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null,
    val nameInput: String = "",
    val ageInput: String = "",
    val selectedGender: Gender? = null,
    val bodyFatGoalInput: String = "",
    val photoPath: String? = null, // <<< ADDED for profile photo path
    val isEditing: Boolean = false,
    val showCreateProfileButton: Boolean = false,
    val canSave: Boolean = false,
    val errorMessage: String? = null,
    val profileSavedSuccessfully: Boolean = false,
    val showAboutSheet: Boolean = false, // Added for About App bottom sheet
    val showPrivacyPolicySheet: Boolean = false // Added for Privacy Policy bottom sheet
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: IProfileRepository
) : CoreViewModel<ProfileScreenUiState>() {

    override fun initialize(): ProfileScreenUiState = ProfileScreenUiState()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            val existingProfile = profileRepository.getProfile()
            if (existingProfile != null) {
                updateState {
                    copy(
                        isLoading = false,
                        userProfile = existingProfile,
                        nameInput = existingProfile.name,
                        ageInput = existingProfile.age.toString(),
                        selectedGender = existingProfile.gender,
                        bodyFatGoalInput = existingProfile.bodyFatPercentGoal?.toString() ?: "",
                        photoPath = existingProfile.photoPath, // <<< SET photoPath
                        isEditing = false,
                        showCreateProfileButton = false,
                        canSave = false
                    )
                }
            } else {
                updateState {
                    copy(
                        isLoading = false,
                        userProfile = null,
                        nameInput = "",
                        ageInput = "",
                        selectedGender = null,
                        bodyFatGoalInput = "",
                        photoPath = null, // <<< Ensure null for new profile
                        showCreateProfileButton = true,
                        isEditing = false
                    )
                }
            }
        }
    }

    fun onEditProfile() {
        viewModelScope.launch {
            // Populate form fields from current profile if available, otherwise use current inputs
            val currentProfile = state.value.userProfile
            updateState {
                copy(
                    isEditing = true,
                    // If a profile exists, prefill from it, otherwise keep current form inputs
                    // This handles the case where user starts creating, then (if we add cancel) goes back, then edits again
                    nameInput = currentProfile?.name ?: state.value.nameInput,
                    ageInput = currentProfile?.age?.toString() ?: state.value.ageInput,
                    selectedGender = currentProfile?.gender ?: state.value.selectedGender,
                    bodyFatGoalInput = currentProfile?.bodyFatPercentGoal?.toString() ?: state.value.bodyFatGoalInput,
                    photoPath = currentProfile?.photoPath ?: state.value.photoPath,
                    showCreateProfileButton = false // Editing an existing or newly initiated profile
                )
            }
            // Re-validate based on prefilled/current data
            updateState { validateInputsInternal(this) }
        }
    }

    fun onNameChanged(name: String) {
        viewModelScope.launch {
            updateState {
                val updatedStateWithName = copy(
                    nameInput = name,
                    isEditing = true,
                    profileSavedSuccessfully = false
                )
                // The result of validateInputsInternal is now the result of the updateState lambda
                validateInputsInternal(updatedStateWithName)
            }
        }
    }

    fun onAgeChanged(age: String) {
        viewModelScope.launch {
            updateState {
                val updatedStateWithAge = copy(
                    ageInput = age.filter { char -> char.isDigit() },
                    isEditing = true,
                    profileSavedSuccessfully = false
                )
                // The result of validateInputsInternal is now the result of the updateState lambda
                validateInputsInternal(updatedStateWithAge)
            }
        }
    }

    fun onGenderSelected(gender: Gender) {
        viewModelScope.launch {
            updateState {
                val updatedState = copy(
                    selectedGender = gender,
                    isEditing = true,
                    profileSavedSuccessfully = false
                )
                validateInputsInternal(updatedState)
            }
        }
    }

    fun onBodyFatGoalChanged(goal: String) {
        viewModelScope.launch {
            updateState {
                // Allow empty string, digits, and a single decimal point
                val filteredGoal = goal.filter { it.isDigit() || it == '.' }
                // Prevent multiple decimal points
                val newGoal = if (filteredGoal.count { it == '.' } > 1) {
                    // if more than one '.', take up to the second one and remove others
                    val firstDot = filteredGoal.indexOf('.')
                    val secondDot = filteredGoal.indexOf('.', startIndex = firstDot + 1)
                    if (secondDot != -1) filteredGoal.substring(0, secondDot) else filteredGoal
                } else {
                    filteredGoal
                }

                val updatedState = copy(
                    bodyFatGoalInput = newGoal,
                    isEditing = true, // Any change implies editing
                    profileSavedSuccessfully = false
                )
                validateInputsInternal(updatedState)
            }
        }
    }

    // --- Photo Selection Handler ---
    fun onPhotoSelected(uri: Uri?, applicationContext: Context) {
        uri ?: return // If URI is null, do nothing

        viewModelScope.launch {
            // Copy file in a background thread
            val internalPath = copyImageToInternalStorage(applicationContext, uri)
            internalPath?.let { path ->
                // If there was an old photo, try to delete it
                val oldPath = state.value.photoPath
                if (oldPath != null && oldPath != path) {
                    deleteImageFromInternalStorage(oldPath)
                }

                updateState {
                    val updatedState = copy(
                        photoPath = path,
                        isEditing = true, // Changing photo is an edit
                        profileSavedSuccessfully = false
                    )
                    validateInputsInternal(updatedState)
                }
            } ?: run {
                // Handle error copying file (e.g., show a snackbar)
                updateState { copy(errorMessage = "Failed to save profile image.") }
            }
        }
    }

    private suspend fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) { // Perform file operations on IO dispatcher
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                // Use a consistent filename to overwrite if user selects new photo
                val nameModifier = UUID.randomUUID().toString()
                val fileName = "profile_image_${nameModifier}.jpg"
                val file = File(context.filesDir, fileName)
                val outputStream = FileOutputStream(file)
                inputStream?.use { input -> // Use 'use' to ensure stream is closed
                    outputStream.use { output -> // Use 'use' to ensure stream is closed
                        input.copyTo(output)
                    }
                }
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace() // Log error
                null
            }
        }
    }

    private suspend fun deleteImageFromInternalStorage(filePath: String) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace() // Log error
            }
        }
    }

    // validateInputsInternal correctly takes a state and returns a new one
    private fun validateInputsInternal(currentState: ProfileScreenUiState): ProfileScreenUiState {
        val nameIsValid = currentState.nameInput.isNotBlank()
        val ageIsValid = currentState.ageInput.isNotBlank()
        val genderIsValid = currentState.selectedGender != null
        val bodyFatGoalIsValidOrEmpty = currentState.bodyFatGoalInput.isEmpty() ||
                currentState.bodyFatGoalInput.toDoubleOrNull() != null

        // Photo path change is also considered a "change made"
        val changesMade = if (currentState.userProfile == null) {
            // For new profile, any valid interaction implies changes for saving,
            // or if a photo was added.
            currentState.nameInput.isNotEmpty() || currentState.ageInput.isNotEmpty() ||
                    currentState.selectedGender != null || currentState.bodyFatGoalInput.isNotEmpty() ||
                    currentState.photoPath != null
        } else {
            currentState.nameInput != currentState.userProfile.name ||
                    currentState.ageInput != currentState.userProfile.age.toString() ||
                    currentState.selectedGender != currentState.userProfile.gender ||
                    (currentState.bodyFatGoalInput.toDoubleOrNull()
                        ?: -1.0) != (currentState.userProfile.bodyFatPercentGoal ?: -1.0) ||
                    currentState.photoPath != currentState.userProfile.photoPath // <<< CHECK photoPath change
        }

        val effectiveIsEditing = currentState.isEditing

        val canActuallySave = nameIsValid && ageIsValid && genderIsValid &&
                bodyFatGoalIsValidOrEmpty && changesMade && effectiveIsEditing

        return currentState.copy(
            canSave = canActuallySave,
            isEditing = effectiveIsEditing
        )
    }

    fun onCreateProfileClicked() {
        viewModelScope.launch {
            updateState {
                val newState = copy(
                    isEditing = true,
                    nameInput = "",
                    ageInput = "",
                    selectedGender = null,
                    bodyFatGoalInput = "",
                    photoPath = null, // <<< RESET photoPath
                    canSave = false, // An empty form cannot be saved
                    showCreateProfileButton = false
                )
                // Even with photo being optional, an empty form isn't saveable yet
                validateInputsInternal(newState)
            }
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            if (!state.value.canSave) return@launch

            val currentValidatedState = state.value
            val ageToSave = currentValidatedState.ageInput.trim().toIntOrNull()
            val genderToSave = currentValidatedState.selectedGender
            val bodyFatGoalToSave: Double? = currentValidatedState.bodyFatGoalInput.trim().let {
                if (it.isEmpty()) null else it.toDoubleOrNull()
            }
            val photoPathToSave = currentValidatedState.photoPath // Path is already what we need

            if (currentValidatedState.bodyFatGoalInput.isNotEmpty() && bodyFatGoalToSave == null) {
                updateState { copy(errorMessage = "Invalid Body Fat Goal entered.") }
                return@launch
            }
            if (ageToSave == null) {
                updateState { copy(errorMessage = "Invalid age entered.") }
                return@launch
            }
            if (genderToSave == null) {
                updateState { copy(errorMessage = "Please select a gender.") }
                return@launch
            }

            val newProfile = UserProfile(
                name = currentValidatedState.nameInput.trim(),
                age = ageToSave,
                gender = genderToSave,
                bodyFatPercentGoal = bodyFatGoalToSave?.toInt(),
                photoPath = photoPathToSave // <<< SAVE photo path
            )
            viewModelScope.launch {
                profileRepository.saveProfile(newProfile)
                updateState {
                    copy(
                        userProfile = newProfile,
                        isEditing = false,
                        canSave = false,
                        profileSavedSuccessfully = true
                        // photoPath remains as is, already updated
                    )
                }
            }
        }
    }

    fun dismissSuccessMessage() {
        viewModelScope.launch {
            updateState { copy(profileSavedSuccessfully = false) }
        }
    }

    fun clearErrorMessage() {
        viewModelScope.launch {
            updateState { copy(errorMessage = null) }
        }
    }

    fun onPermissionDenied(message: String) {
        viewModelScope.launch {
            updateState { copy(errorMessage = message) }
        }
    }

    // --- About App Bottom Sheet ---
    fun onAboutAppClicked() {
        viewModelScope.launch {
            updateState { copy(showAboutSheet = true) }
        }
    }

    fun onDismissAboutApp() {
        viewModelScope.launch {
            updateState { copy(showAboutSheet = false) }
        }
    }

    // --- Privacy Policy Bottom Sheet ---
    fun onPrivacyPolicyClicked() {
        viewModelScope.launch {
            updateState { copy(showPrivacyPolicySheet = true) }
        }
    }

    fun onDismissPrivacyPolicy() {
        viewModelScope.launch {
            updateState { copy(showPrivacyPolicySheet = false) }
        }
    }
}
