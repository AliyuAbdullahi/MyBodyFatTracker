package com.lekan.bodyfattracker.ui.home.measurement.viewmodels

import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository
import com.lekan.bodyfattracker.domain.IProfileRepository
import com.lekan.bodyfattracker.model.BodyFatMeasurement
import com.lekan.bodyfattracker.model.MeasurementMethod
import com.lekan.bodyfattracker.ui.core.CoreViewModel
import com.lekan.bodyfattracker.ui.home.Gender
import com.lekan.bodyfattracker.ui.home.calculateBodyFatPercentageThreeSites
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// State Data Class remains the same
data class ThreeSiteMeasurementState(
    val age: String = "",
    val selectedGender: Gender = Gender.FEMALE,
    val skinfold1: String = "", // Represents Chest (Male) or Triceps (Female)
    val skinfold2: String = "", // Represents Abdomen (Male) or Suprailiac (Female)
    val skinfold3: String = "", // Represents Thigh for both
    val isFormComplete: Boolean = false,
    val calculationResult: BodyFatMeasurement? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val canSaveResult: Boolean = false,
    val isShowingResult: Boolean = false
)

@HiltViewModel
class ThreeSiteMeasurementViewModel @Inject constructor(
    private val repository: IBodyFatInfoRepository,
    private val profileRepository: IProfileRepository
) : CoreViewModel<ThreeSiteMeasurementState>() {

    init {
        viewModelScope.launch {
            profileRepository.getProfileSync()?.let { profile ->
                updateState { copy(age = "${profile.age}", selectedGender = profile.gender).validateForm() }
            }
        }
    }

    override fun initialize(): ThreeSiteMeasurementState = ThreeSiteMeasurementState()

    fun onAgeChanged(age: String) {
        viewModelScope.launch {
            updateState { // updateState is now from CoreViewModel
                copy(age = age.filter { it.isDigit() }).validateForm()
            }
        }
    }

    fun start(canSaveResult: Boolean) {
        viewModelScope.launch {
            updateState { copy(canSaveResult = canSaveResult) }
        }
    }

    fun onGenderSelected(gender: Gender) {
        viewModelScope.launch {
            updateState {
                copy(selectedGender = gender).validateForm()
            }
        }
    }

    fun onSkinfold1Changed(value: String) {
        viewModelScope.launch {
            updateState {
                copy(skinfold1 = value.filterValidDoubleInput()).validateForm()
            }
        }
    }

    fun onSkinfold2Changed(value: String) {
        viewModelScope.launch {
            updateState {
                copy(skinfold2 = value.filterValidDoubleInput()).validateForm()
            }
        }
    }

    fun onSkinfold3Changed(value: String) {
        viewModelScope.launch {
            updateState {
                copy(skinfold3 = value.filterValidDoubleInput()).validateForm()
            }
        }
    }

    private fun String.filterValidDoubleInput(): String {
        if (this.isEmpty()) return ""
        val parts = this.split('.')
        return if (parts.size <= 2) {
            parts[0].filter { it.isDigit() } +
                    if (parts.size == 2) "." + parts[1].filter { it.isDigit() } else ""
        } else {
            parts[0].filter { it.isDigit() } + "." + parts[1].filter { it.isDigit() }
        }
    }

    private fun ThreeSiteMeasurementState.validateForm(): ThreeSiteMeasurementState {
        val isComplete = age.isNotBlank() &&
                skinfold1.isNotBlank() &&
                skinfold2.isNotBlank() &&
                skinfold3.isNotBlank() &&
                age.toIntOrNull()?.let { it > 0 } == true &&
                skinfold1.toDoubleOrNull()?.let { it > 0 } == true &&
                skinfold2.toDoubleOrNull()?.let { it > 0 } == true &&
                skinfold3.toDoubleOrNull()?.let { it > 0 } == true
        return this.copy(isFormComplete = isComplete)
    }

    fun calculateBodyFat() {
        viewModelScope.launch {
            // Read the current state via the inherited uiState property
            val currentState = state.value // Or CoreViewModel might provide a getCurrentState()

            if (!currentState.isFormComplete) {
                updateState { copy(errorMessage = "Please fill all fields with valid numbers.") }
                return@launch
            }

            val ageInt = currentState.age.toInt()
            val sf1 = currentState.skinfold1.toDouble()
            val sf2 = currentState.skinfold2.toDouble()
            val sf3 = currentState.skinfold3.toDouble()

            updateState { copy(isLoading = true, errorMessage = null, calculationResult = null) }

            try {
                val bodyFatPercentageDouble = calculateBodyFatPercentageThreeSites(
                    age = ageInt,
                    gender = currentState.selectedGender,
                    skinfold1 = sf1,
                    skinfold2 = sf2,
                    skinfold3 = sf3
                )
                if (bodyFatPercentageDouble.isFinite() && bodyFatPercentageDouble >= 0) {
                    // Create BodyFatInfo object
                    val sdf = SimpleDateFormat(
                        "dd/MM/yyyy - HH:mm",
                        Locale.getDefault()
                    )
                    val currentDateStr = sdf.format(Date())
                    val currentTimeStamp = System.currentTimeMillis()
//
                    val bodyFatMeasurementResult = BodyFatMeasurement(
                        timeStamp = currentTimeStamp,
                        percentage = bodyFatPercentageDouble,
                        method = MeasurementMethod.THREE_POINTS
                        // notes = null // Assuming notes are not collected in this ViewModel
                    )

                    if (state.value.canSaveResult) {
                        repository.saveMeasurement(bodyFatMeasurementResult)
                    }

                    updateState {
                        copy(
                            isLoading = false,
                            calculationResult = bodyFatMeasurementResult,
                            errorMessage = null,
                            isShowingResult = true
                        )
                    }
                } else {
                    // Handle cases where calculation might return null, NaN, Infinity, or negative
                    updateState {
                        copy(
                            isLoading = false,
                            calculationResult = null,
                            errorMessage = "Could not calculate body fat. Invalid result from calculation."
                        )
                    }
                }
            } catch (e: Exception) {
                // Log.e("ThreeSiteVM", "Calculation error", e)
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = "An error occurred during calculation.",
                        calculationResult = null
                    )
                }
            }
        }
    }

    fun clearErrorMessage() {
        viewModelScope.launch {
            updateState { copy(errorMessage = null) }
        }
    }

    fun closeResultForm() {
        viewModelScope.launch {
            updateState { copy(isShowingResult = false) }
        }
    }

    fun resetFormAndResult() {
        viewModelScope.launch {
            val canSaveResult = state.value.canSaveResult // Read from inherited uiState
            val gender = state.value.selectedGender // Read from inherited uiState
            // Re-initialize to the default state but keep the selected gender
            updateState {
                initialize().copy(
                    canSaveResult = canSaveResult,
                    selectedGender = gender
                ).validateForm()
            }
        }
    }
}
