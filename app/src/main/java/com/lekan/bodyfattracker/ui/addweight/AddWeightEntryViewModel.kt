package com.lekan.bodyfattracker.ui.addweight

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.domain.IWeightEntryRepository
import com.lekan.bodyfattracker.model.WeightEntry
import com.lekan.bodyfattracker.model.WeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddWeightEntryUiState(
    val weight: String = "",
    val selectedUnit: WeightUnit = WeightUnit.KG, // Default to KG
    val notes: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AddWeightEntryViewModel @Inject constructor(
    private val weightEntryRepository: IWeightEntryRepository
) : ViewModel() {

    var uiState by mutableStateOf(AddWeightEntryUiState())
        private set

    fun onWeightChange(newValue: String) {
        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
            uiState = uiState.copy(weight = newValue, saveError = null)
        }
    }

    fun onUnitSelected(unit: WeightUnit) {
        uiState = uiState.copy(selectedUnit = unit, saveError = null)
    }

    fun onNotesChange(newValue: String) {
        uiState = uiState.copy(notes = newValue, saveError = null)
    }

    fun saveWeightEntry() {
        val weightDouble = uiState.weight.toDoubleOrNull()

        if (weightDouble == null || weightDouble <= 0) {
            uiState = uiState.copy(saveError = "Invalid weight value.")
            return
        }

        uiState = uiState.copy(isSaving = true, saveError = null)

        viewModelScope.launch {
            try {
                val entry = WeightEntry(
                    weight = weightDouble,
                    unit = uiState.selectedUnit,
                    notes = uiState.notes.takeIf { it.isNotBlank() },
                    timeStamp = System.currentTimeMillis()
                )
                weightEntryRepository.saveWeightEntry(entry)
                uiState = uiState.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    saveError = "Error saving weight entry: ${e.localizedMessage}"
                )
            }
        }
    }

    fun resetSaveStatus() {
        uiState = uiState.copy(saveSuccess = false, saveError = null)
    }
}

