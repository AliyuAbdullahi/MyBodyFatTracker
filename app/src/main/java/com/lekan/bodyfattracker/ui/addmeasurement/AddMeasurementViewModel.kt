package com.lekan.bodyfattracker.ui.addmeasurement

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository
import com.lekan.bodyfattracker.model.BodyFatMeasurement
import com.lekan.bodyfattracker.model.MeasurementMethod // Ensure this is the correct enum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMeasurementUiState(
    val bodyFatPercentage: String = "",
    val selectedMethod: MeasurementMethod? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AddMeasurementViewModel @Inject constructor(
    private val bodyFatInfoRepository: IBodyFatInfoRepository
) : ViewModel() {

    var uiState by mutableStateOf(AddMeasurementUiState())
        private set

    fun onBodyFatPercentageChange(newValue: String) {
        // Allow only digits and a single decimal point
        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
            uiState = uiState.copy(bodyFatPercentage = newValue, saveError = null)
        }
    }

    fun onMethodSelected(method: MeasurementMethod) {
        uiState = uiState.copy(selectedMethod = method, saveError = null)
    }

    fun saveMeasurement() {
        val percentageDouble = uiState.bodyFatPercentage.toDoubleOrNull()
        val method = uiState.selectedMethod

        if (percentageDouble == null || percentageDouble <= 0 || percentageDouble >= 100) {
            uiState = uiState.copy(saveError = "Invalid percentage value.")
            return
        }
        if (method == null) {
            uiState = uiState.copy(saveError = "Please select a measurement method.")
            return
        }

        uiState = uiState.copy(isSaving = true, saveError = null)

        viewModelScope.launch {
            try {
                val measurement = BodyFatMeasurement(
                    percentage = percentageDouble,
                    method = method,
                    timeStamp = System.currentTimeMillis(),
                    // siteMeasurements can be null or empty if not applicable for manual entry
                )
                bodyFatInfoRepository.saveMeasurement(measurement)
                uiState = uiState.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    saveError = "Error saving measurement: ${e.localizedMessage}"
                )
            }
        }
    }

    fun resetSaveStatus() {
        uiState = uiState.copy(saveSuccess = false, saveError = null)
    }
}

