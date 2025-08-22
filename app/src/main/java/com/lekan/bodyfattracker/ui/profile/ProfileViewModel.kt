package com.lekan.bodyfattracker.ui.profile

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.lekan.bodyfattracker.BuildConfig
import com.lekan.bodyfattracker.data.UserPreferencesRepository
import com.lekan.bodyfattracker.domain.IProfileRepository
import com.lekan.bodyfattracker.model.UserFeedback
import com.lekan.bodyfattracker.model.UserProfile
import com.lekan.bodyfattracker.model.WeightUnit
import com.lekan.bodyfattracker.ui.core.CoreViewModel
import com.lekan.bodyfattracker.ui.home.Gender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class ProfileScreenUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null, // Store the loaded profile for comparison
    val nameInput: String = "",
    val ageInput: String = "",
    val selectedGender: Gender? = null,
    val bodyFatGoalInput: String = "",
    val photoPath: String? = null,
    val isEditing: Boolean = false,
    val showCreateProfileButton: Boolean = false,
    val canSave: Boolean = false,
    val errorMessage: String? = null,
    val profileSavedSuccessfully: Boolean = false,
    val showAboutSheet: Boolean = false,
    val showPrivacyPolicySheet: Boolean = false,
    val selectedDisplayWeightUnit: WeightUnit = WeightUnit.KG,
    val isSuperUser: Boolean = true, // Added for testing feedback viewing
    val showFeedbackDialog: Boolean = false,
    val feedbackDialogText: String = "",
    val isSendingFeedback: Boolean = false,
    val feedbackSentMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: IProfileRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val firestore: FirebaseFirestore // Injected Firestore
) : CoreViewModel<ProfileScreenUiState>() {

    override fun initialize() = ProfileScreenUiState()

    init {
        loadProfile()
        viewModelScope.launch {
            userPreferencesRepository.userSettingsFlow.collect { settings ->
                updateState { copy(selectedDisplayWeightUnit = settings.displayWeightUnit) }
            }
        }
    }

    fun updateDisplayWeightUnit(weightUnit: WeightUnit) {
        viewModelScope.launch {
            userPreferencesRepository.updateDisplayWeightUnit(weightUnit)
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            profileRepository.getProfile().collect { existingProfile ->
                if (existingProfile != null) {
                    updateState {
                        copy(
                            isLoading = false,
                            userProfile = existingProfile,
                            nameInput = existingProfile.name,
                            ageInput = existingProfile.age.toString(),
                            selectedGender = existingProfile.gender,
                            bodyFatGoalInput = existingProfile.bodyFatPercentGoal?.toString() ?: "",
                            photoPath = existingProfile.photoPath,
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
                            photoPath = null,
                            showCreateProfileButton = true,
                            isEditing = false,
                            canSave = false
                        )
                    }
                }
            }
        }
    }

    fun onEditProfile() {
        val currentLoadedProfile = state.value.userProfile
        launch {
            updateState {
                copy(
                    isEditing = true,
                    nameInput = currentLoadedProfile?.name ?: state.value.nameInput,
                    ageInput = currentLoadedProfile?.age?.toString() ?: state.value.ageInput,
                    selectedGender = currentLoadedProfile?.gender ?: state.value.selectedGender,
                    bodyFatGoalInput = currentLoadedProfile?.bodyFatPercentGoal?.toString()
                        ?: state.value.bodyFatGoalInput,
                    photoPath = currentLoadedProfile?.photoPath ?: state.value.photoPath,
                    showCreateProfileButton = false
                ).let { validateInputsInternal(it) }
            }
        }
    }

    fun cancelEditProfile() {
        val lastLoadedProfile = state.value.userProfile
        if (lastLoadedProfile != null) {
            launch {
                updateState {
                    copy(
                        isEditing = false,
                        nameInput = lastLoadedProfile.name,
                        ageInput = lastLoadedProfile.age.toString(),
                        selectedGender = lastLoadedProfile.gender,
                        bodyFatGoalInput = lastLoadedProfile.bodyFatPercentGoal?.toString() ?: "",
                        photoPath = lastLoadedProfile.photoPath,
                        canSave = false,
                        errorMessage = null,
                        showCreateProfileButton = false
                    )
                }
            }
        } else {
            launch {
                updateState {
                    copy(
                        isEditing = false,
                        nameInput = "",
                        ageInput = "",
                        selectedGender = null,
                        bodyFatGoalInput = "",
                        photoPath = null,
                        canSave = false,
                        errorMessage = null,
                        showCreateProfileButton = true
                    )
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        launch {
            updateState {
                copy(
                    nameInput = name,
                    isEditing = true,
                    profileSavedSuccessfully = false
                ).let { validateInputsInternal(it) }
            }
        }
    }

    fun onAgeChanged(age: String) {
        launch {
            updateState {
                copy(
                    ageInput = age.filter { char -> char.isDigit() },
                    isEditing = true,
                    profileSavedSuccessfully = false
                ).let { validateInputsInternal(it) }
            }
        }
    }

    fun onGenderSelected(gender: Gender) {
        launch {
            updateState {
                copy(
                    selectedGender = gender,
                    isEditing = true,
                    profileSavedSuccessfully = false
                ).let { validateInputsInternal(it) }
            }
        }
    }

    fun onBodyFatGoalChanged(goal: String) {
        val filteredGoal = goal.filter { it.isDigit() || it == '.' }
        val newGoal = if (filteredGoal.count { it == '.' } > 1) {
            val firstDot = filteredGoal.indexOf('.')
            val secondDot = filteredGoal.indexOf('.', startIndex = firstDot + 1)
            if (secondDot != -1) filteredGoal.substring(0, secondDot) else filteredGoal
        } else {
            filteredGoal
        }
        launch {
            updateState {
                copy(
                    bodyFatGoalInput = newGoal,
                    isEditing = true,
                    profileSavedSuccessfully = false
                ).let { validateInputsInternal(it) }
            }
        }
    }

    fun onPhotoSelected(
        uri: Uri?,
        applicationContext: Context,
        failedToSaveProfileMessage: String
    ) {
        uri ?: return
        viewModelScope.launch {
            val internalPath = copyImageToInternalStorage(applicationContext, uri)
            internalPath?.let { path ->
                val oldPath = state.value.photoPath
                if (oldPath != null && oldPath != path) {
                    deleteImageFromInternalStorage(oldPath)
                }
                updateState {
                    copy(
                        photoPath = path,
                        isEditing = true,
                        profileSavedSuccessfully = false
                    ).let { validateInputsInternal(it) }
                }
            } ?: run {
                updateState { copy(errorMessage = failedToSaveProfileMessage) }
            }
        }
    }

    private suspend fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val nameModifier = UUID.randomUUID().toString()
                val fileName = "profile_image_${nameModifier}.jpg"
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    inputStream?.use { input -> input.copyTo(outputStream) }
                }
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
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
                e.printStackTrace()
            }
        }
    }

    private fun validateInputsInternal(currentState: ProfileScreenUiState): ProfileScreenUiState {
        val nameIsValid = currentState.nameInput.isNotBlank()
        val ageIsValid =
            currentState.ageInput.isNotBlank() && currentState.ageInput.toIntOrNull() != null && currentState.ageInput.toInt() > 0
        val genderIsValid = currentState.selectedGender != null
        val bodyFatGoalIsValidOrEmpty = currentState.bodyFatGoalInput.isEmpty() ||
                (currentState.bodyFatGoalInput.toDoubleOrNull() != null && currentState.bodyFatGoalInput.toDouble() > 0 && currentState.bodyFatGoalInput.toDouble() < 100)

        val originalProfile = currentState.userProfile
        val changesMade = if (originalProfile == null) {
            currentState.nameInput.isNotEmpty() || currentState.ageInput.isNotEmpty() ||
                    currentState.selectedGender != null || currentState.bodyFatGoalInput.isNotEmpty() ||
                    currentState.photoPath != null
        } else {
            currentState.nameInput != originalProfile.name ||
                    currentState.ageInput != originalProfile.age.toString() ||
                    currentState.selectedGender != originalProfile.gender ||
                    (currentState.bodyFatGoalInput.toDoubleOrNull()
                        ?: -1.0) != (originalProfile.bodyFatPercentGoal?.toDouble() ?: -1.0) ||
                    currentState.photoPath != originalProfile.photoPath
        }

        val canActuallySave = nameIsValid && ageIsValid && genderIsValid &&
                bodyFatGoalIsValidOrEmpty && changesMade && currentState.isEditing

        return currentState.copy(canSave = canActuallySave)
    }

    fun onCreateProfileClicked() {
        launch {
            updateState {
                copy(
                    isEditing = true,
                    userProfile = null,
                    nameInput = "",
                    ageInput = "",
                    selectedGender = null,
                    bodyFatGoalInput = "",
                    photoPath = null,
                    canSave = false,
                    showCreateProfileButton = false,
                    profileSavedSuccessfully = false,
                    errorMessage = null
                ).let { validateInputsInternal(it) }
            }
        }
    }

    fun saveProfile(
        invalidAgeMessage: String,
        selectAGenderMessage: String,
        invalidBodyFatGoalMessage: String
    ) {
        viewModelScope.launch {
            if (!state.value.canSave) {
                return@launch
            }

            val currentValidatedState = state.value
            val ageToSave = currentValidatedState.ageInput.trim().toIntOrNull()
            val genderToSave = currentValidatedState.selectedGender
            val bodyFatGoalToSave: Double? = currentValidatedState.bodyFatGoalInput.trim().let {
                if (it.isEmpty()) null else it.toDoubleOrNull()
            }

            if (ageToSave == null || ageToSave <= 0) {
                updateState { copy(errorMessage = invalidAgeMessage) }
                return@launch
            }
            if (genderToSave == null) {
                updateState { copy(errorMessage = selectAGenderMessage) }
                return@launch
            }
            if (currentValidatedState.bodyFatGoalInput.isNotEmpty() && (bodyFatGoalToSave == null || bodyFatGoalToSave <= 0 || bodyFatGoalToSave >= 100)) {
                updateState { copy(errorMessage = invalidBodyFatGoalMessage) }
                return@launch
            }

            val newProfile = UserProfile(
                name = currentValidatedState.nameInput.trim(),
                age = ageToSave,
                gender = genderToSave,
                bodyFatPercentGoal = bodyFatGoalToSave?.toInt(),
                photoPath = currentValidatedState.photoPath
            )

            profileRepository.saveProfile(newProfile)
            updateState {
                copy(
                    userProfile = newProfile,
                    isEditing = false,
                    canSave = false,
                    profileSavedSuccessfully = true,
                    errorMessage = null
                )
            }
        }
    }

    fun dismissSuccessMessage() {
        launch {
            updateState { copy(profileSavedSuccessfully = false) }
        }
    }

    fun clearErrorMessage() {
        launch {
            updateState { copy(errorMessage = null) }
        }
    }

    fun onAboutAppClicked() {
        launch {
            updateState { copy(showAboutSheet = true) }
        }
    }

    fun onDismissAboutApp() {
        launch {
            updateState { copy(showAboutSheet = false) }
        }
    }

    // Feedback related functions
    fun onLeaveFeedbackClicked() {
        launch {
            updateState { copy(showFeedbackDialog = true) }
        }
    }

    fun onDismissFeedbackDialog() {
        launch {
            updateState {
                copy(
                    showFeedbackDialog = false,
                    feedbackDialogText = "",
                    isSendingFeedback = false
                )
            }
        }
    }

    fun onFeedbackDialogTextChanged(text: String) {
        launch {
            updateState { copy(feedbackDialogText = text) }
        }
    }

    fun sendFeedback(
        feedbackEmptyMessage: String,
        feedbackSentMessage: String,
        failedToSendMessage: String
    ) {
        if (state.value.feedbackDialogText.isBlank()) {
            launch {
                updateState { copy(feedbackSentMessage = feedbackEmptyMessage) }
            }
            return
        }
        launch {
            updateState { copy(isSendingFeedback = true) }
            val feedback = UserFeedback(
                message = state.value.feedbackDialogText,
                appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                androidVersion = Build.VERSION.RELEASE,
                locale = Locale.getDefault().toString()
            )
            try {
                firestore.collection("feedback").add(feedback).await()
                updateState {
                    copy(
                        isSendingFeedback = false,
                        showFeedbackDialog = false,
                        feedbackDialogText = "",
                        feedbackSentMessage = feedbackSentMessage
                    )
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isSendingFeedback = false,
                        feedbackSentMessage = failedToSendMessage
                    )
                }
            }
        }
    }

    fun clearFeedbackMessage() {
        launch {
            updateState { copy(feedbackSentMessage = null) }
        }
    }
}
