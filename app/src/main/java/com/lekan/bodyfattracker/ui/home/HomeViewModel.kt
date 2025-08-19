package com.lekan.bodyfattracker.ui.home

import androidx.compose.animation.core.copy
import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository
import com.lekan.bodyfattracker.domain.IProfileRepository
import com.lekan.bodyfattracker.domain.IWeightEntryRepository
import com.lekan.bodyfattracker.model.BodyFatMeasurement
import com.lekan.bodyfattracker.model.UserProfile
import com.lekan.bodyfattracker.model.WeightEntry
import com.lekan.bodyfattracker.ui.core.CoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null,
    val latestMeasurement: BodyFatMeasurement? = null,
    val latestWeightEntry: WeightEntry? = null,
    val recentWeightEntries: List<WeightEntry> = emptyList(), // Added this line
    val isReminderEnabled: Boolean = false, // Added
    val reminderHour: Int? = null,          // Added
    val reminderMinute: Int? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: IProfileRepository,
    private val bodyFatInfoRepository: IBodyFatInfoRepository,
    private val weightEntryRepository: IWeightEntryRepository
) : CoreViewModel<HomeUiState>() {

    override fun initialize(): HomeUiState {
        return HomeUiState(
            isLoading = true,
            userProfile = null,
            latestMeasurement = null,
            latestWeightEntry = null,
            recentWeightEntries = emptyList(), // Initialize here as well
            isReminderEnabled = false,
            reminderHour = null,
            reminderMinute = null
        )
    }

    init {
        observeAllHomeData()
    }

    private fun observeAllHomeData() {
        viewModelScope.launch {
            combine(
                profileRepository.getProfile(),
                bodyFatInfoRepository.getLatestMeasurement(),
                weightEntryRepository.getLatestWeightEntry(),
                weightEntryRepository.getAllWeightEntries() // Added this flow
            ) { profile, latestMeasurement, latestWeight, allWeights -> // Added allWeights argument
                // Assuming getAllWeightEntries() returns newest first. If oldest first, use allWeights.takeLast(10).reversed()
                // Or simply allWeights.takeLast(10) if the chart can handle reversed chronological data.
                // For Vico, typically you want data in chronological order for line charts.
                // If getAllWeightEntries is newest first, we need to reverse it for the chart.
                val chartEntries = allWeights.take(10).reversed()

                HomeUiState(
                    isLoading = false,
                    userProfile = profile,
                    latestMeasurement = latestMeasurement,
                    latestWeightEntry = latestWeight,
                    recentWeightEntries = chartEntries // Populate the new field with reversed, chronological entries
                )
            }.collect { combinedState ->
                updateState { combinedState }
            }
        }
    }

    fun saveUserProfile(name: String, age: Int, gender: Gender, goal: Int?, photoPath: String?) {
        viewModelScope.launch {
            val profile = UserProfile(
                name = name,
                age = age,
                gender = gender,
                bodyFatPercentGoal = goal,
                photoPath = photoPath
            )
            profileRepository.saveProfile(profile)
        }
    }

    fun clearUserProfile() {
        viewModelScope.launch {
            profileRepository.clearProfile()
        }
    }

    fun saveNewMeasurement(measurement: BodyFatMeasurement) {
        viewModelScope.launch {
            bodyFatInfoRepository.saveMeasurement(measurement)
        }
    }

    fun saveNewWeightEntry(entry: WeightEntry) {
        viewModelScope.launch {
            weightEntryRepository.saveWeightEntry(entry)
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            updateState {
                if (enabled) {
                    // If enabling and no time is set, you might want to default
                    // to a specific time or prompt user. For now, just enables.
                    // Actual scheduling of alarm would happen here in a real scenario.
                    copy(isReminderEnabled = true)
                } else {
                    // Disabling reminder, also clear time and cancel alarm.
                    copy(isReminderEnabled = false)
                }
            }
        }

        // TODO: Persist 'enabled' state and schedule/cancel actual system alarm
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            updateState {
                copy(
                    isReminderEnabled = true, // Ensure reminder is enabled when time is set
                    reminderHour = hour,
                    reminderMinute = minute
                )
            }
        }

        // TODO: Persist new time and reschedule system alarm
    }
}
