package com.lekan.bodyfattracker.ui.home.measurement.viewmodels

import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository
import com.lekan.bodyfattracker.domain.IProfileRepository
import com.lekan.bodyfattracker.model.BodyFatInfo
import com.lekan.bodyfattracker.ui.core.CoreViewModel
import com.lekan.bodyfattracker.ui.home.Gender
import com.lekan.bodyfattracker.ui.home.calculate7SiteBodyFatPercentage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SevenSiteMeasurementState(
    val age: String = "",
    val selectedGender: Gender = Gender.FEMALE, // Default to one gender
    val chest: String = "",
    val midaxillary: String = "",
    val triceps: String = "",
    val subscapular: String = "",
    val abdomen: String = "",
    val suprailiac: String = "",
    val thigh: String = "",
    val isFormComplete: Boolean = false,
    val calculationResult: BodyFatInfo? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val canSaveResult: Boolean = false,
    val isShowingResult: Boolean = false
)

@HiltViewModel
class SevenSitesMeasurementViewModel @Inject constructor(
    private val repository: IBodyFatInfoRepository,
    private val profileRepository: IProfileRepository
) : CoreViewModel<SevenSiteMeasurementState>() {

    override fun initialize(): SevenSiteMeasurementState = SevenSiteMeasurementState()

    init {
        viewModelScope.launch {
            updateState { copy(age = profileRepository.getProfile()?.age?.toString() ?: "") }
        }
    }

    fun onAgeChanged(age: String) {
        viewModelScope.launch {
            updateState {
                copy(age = age.filter { it.isDigit() }).validateForm()
            }
        }
    }

    fun onGenderSelected(gender: Gender) {
        viewModelScope.launch {
            updateState {
                copy(selectedGender = gender).validateForm()
            }
        }
    }

    fun start(canSaveResult: Boolean) {
        viewModelScope.launch {
            updateState { copy(canSaveResult = canSaveResult) }
        }
    }

    // Individual skinfold update functions
    fun onChestChanged(value: String) {
        viewModelScope.launch { updateState { copy(chest = value.filterValidDoubleInput()).validateForm() } }
    }
    fun onMidaxillaryChanged(value: String) {
        viewModelScope.launch { updateState { copy(midaxillary = value.filterValidDoubleInput()).validateForm() } }
    }
    fun onTricepsChanged(value: String) {
        viewModelScope.launch { updateState { copy(triceps = value.filterValidDoubleInput()).validateForm() } }
    }
    fun onSubscapularChanged(value: String) {
        viewModelScope.launch { updateState { copy(subscapular = value.filterValidDoubleInput()).validateForm() } }
    }
    fun onAbdomenChanged(value: String) {
        viewModelScope.launch { updateState { copy(abdomen = value.filterValidDoubleInput()).validateForm() } }
    }
    fun onSuprailiacChanged(value: String) {
        viewModelScope.launch { updateState { copy(suprailiac = value.filterValidDoubleInput()).validateForm() } }
    }
    fun onThighChanged(value: String) {
        viewModelScope.launch { updateState { copy(thigh = value.filterValidDoubleInput()).validateForm() } }
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

    private fun SevenSiteMeasurementState.validateForm(): SevenSiteMeasurementState {
        val isComplete = age.isNotBlank() &&
                chest.isNotBlank() &&
                midaxillary.isNotBlank() &&
                triceps.isNotBlank() &&
                subscapular.isNotBlank() &&
                abdomen.isNotBlank() &&
                suprailiac.isNotBlank() &&
                thigh.isNotBlank() &&
                age.toIntOrNull()?.let { it > 0 } == true &&
                chest.toDoubleOrNull()?.let { it > 0 } == true &&
                midaxillary.toDoubleOrNull()?.let { it > 0 } == true &&
                triceps.toDoubleOrNull()?.let { it > 0 } == true &&
                subscapular.toDoubleOrNull()?.let { it > 0 } == true &&
                abdomen.toDoubleOrNull()?.let { it > 0 } == true &&
                suprailiac.toDoubleOrNull()?.let { it > 0 } == true &&
                thigh.toDoubleOrNull()?.let { it > 0 } == true
        return this.copy(isFormComplete = isComplete)
    }

    fun calculateBodyFat() {
        viewModelScope.launch {
            val currentState = state.value

            if (!currentState.isFormComplete) {
                updateState { copy(errorMessage = "Please fill all fields with valid numbers.") }
                return@launch
            }

            // Values are validated by isFormComplete
            val ageInt = currentState.age.toInt()
            val chestD = currentState.chest.toDouble()
            val midaxillaryD = currentState.midaxillary.toDouble()
            val tricepsD = currentState.triceps.toDouble()
            val subscapularD = currentState.subscapular.toDouble()
            val abdomenD = currentState.abdomen.toDouble()
            val suprailiacD = currentState.suprailiac.toDouble()
            val thighD = currentState.thigh.toDouble()

            updateState { copy(isLoading = true, errorMessage = null, calculationResult = null) }

            try {
                val bodyFatPercentageDouble: Double? = calculate7SiteBodyFatPercentage(
                    age = ageInt,
                    gender = currentState.selectedGender,
                    chest = chestD,
                    midaxillary = midaxillaryD,
                    triceps = tricepsD,
                    subscapular = subscapularD,
                    abdomen = abdomenD,
                    suprailiac = suprailiacD,
                    thigh = thighD
                )

                if (bodyFatPercentageDouble != null && bodyFatPercentageDouble.isFinite() && bodyFatPercentageDouble >= 0) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
                    val currentDateStr = sdf.format(Date())
                    val currentTimeStamp = System.currentTimeMillis()

                    val bodyFatInfoResult = BodyFatInfo(
                        percentage = bodyFatPercentageDouble.toInt(),
                        date = currentDateStr,
                        timeStamp = currentTimeStamp,
                        type = BodyFatInfo.Type.SEVEN_POINTS // Ensure this type exists
                    )
                    if (state.value.canSaveResult) {
                        repository.addBodyFatInfo(bodyFatInfoResult)
                    }
                    updateState {
                        copy(
                            isLoading = false,
                            calculationResult = bodyFatInfoResult,
                            errorMessage = null,
                            isShowingResult = true
                        )
                    }
                } else {
                    updateState {
                        copy(
                            isLoading = false,
                            calculationResult = null,
                            errorMessage = "Could not calculate body fat. Invalid result from calculation."
                        )
                    }
                }
            } catch (e: Exception) {
                // Log.e("SevenSiteVM", "Calculation error", e)
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
            val currentGender = state.value.selectedGender
            val canSaveResult = state.value.canSaveResult
            updateState { initialize().copy(selectedGender = currentGender, canSaveResult = canSaveResult).validateForm() }
        }
    }
}

