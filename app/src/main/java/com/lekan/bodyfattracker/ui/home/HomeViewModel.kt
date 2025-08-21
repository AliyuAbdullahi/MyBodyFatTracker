package com.lekan.bodyfattracker.ui.home

import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.data.local.ReminderRepository
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
import kotlin.random.Random

data class HomeUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null,
    val latestMeasurement: BodyFatMeasurement? = null,
    val latestWeightEntry: WeightEntry? = null,
    val recentWeightEntries: List<WeightEntry> = emptyList(), // Added this line
    val isReminderEnabled: Boolean = false, // Added
    val reminderHour: Int? = null,          // Added
    val reminderMinute: Int? = null,
    val messageIndex: Int = Random.nextInt(3)
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: IProfileRepository,
    private val bodyFatInfoRepository: IBodyFatInfoRepository,
    private val weightEntryRepository: IWeightEntryRepository,
    private val reminderRepository: ReminderRepository
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
        observeReminderPreferences()
    }

    private fun observeReminderPreferences() {
        viewModelScope.launch {
            reminderRepository.reminderPreferencesFlow.collect { prefs ->
                updateState {
                    copy(
                        isReminderEnabled = prefs.isEnabled,
                        reminderHour = prefs.hour,
                        reminderMinute = prefs.minute
                    )
                }
                // Re-schedule alarm if it was enabled and time is set,
                // or cancel if it was disabled. This handles app startup/data changes.
                if (prefs.isEnabled && prefs.hour != null && prefs.minute != null) {
                    reminderRepository.scheduleReminder(prefs.hour, prefs.minute)
                } else if (!prefs.isEnabled) {
                    reminderRepository.cancelReminder()
                }
            }
        }
    }

    private fun observeAllHomeData() {
        viewModelScope.launch {
            combine(
                profileRepository.getProfile(),
                bodyFatInfoRepository.getLatestMeasurement(),
                weightEntryRepository.getLatestWeightEntry(),
                weightEntryRepository.getAllWeightEntries(), // Added this flow
                // reminderRepository.reminderPreferencesFlow // Removed as it's handled in observeReminderPreferences separately
                                                            // to avoid potential re-scheduling loops with its own updateState.
                                                            // The UI state for reminders will be updated by observeReminderPreferences.
            ) { profile, latestMeasurement, latestWeight, allWeights -> // Added allWeights argument
                // Assuming getAllWeightEntries() returns newest first. If oldest first, use allWeights.takeLast(10).reversed()
                // Or simply allWeights.takeLast(10) if the chart can handle reversed chronological data.
                // For Vico, typically you want data in chronological order for line charts.
                // If getAllWeightEntries is newest first, we need to reverse it for the chart.
                val chartEntries = allWeights.take(10).reversed()

                // Construct a temporary state without reminder fields from this combine
                // Reminder fields are updated by observeReminderPreferences
                 HomeUiState(
                    isLoading = false,
                    userProfile = profile,
                    latestMeasurement = latestMeasurement,
                    latestWeightEntry = latestWeight,
                    recentWeightEntries = chartEntries,
                    isReminderEnabled = state.value.isReminderEnabled, // Preserve current reminder state
                    reminderHour = state.value.reminderHour,           // Preserve current reminder state
                    reminderMinute = state.value.reminderMinute        // Preserve current reminder state
                )
            }.collect { combinedStateFromRepos ->
                // Only update the non-reminder parts of the state from this combine
                updateState {
                    copy(
                        isLoading = combinedStateFromRepos.isLoading,
                        userProfile = combinedStateFromRepos.userProfile,
                        latestMeasurement = combinedStateFromRepos.latestMeasurement,
                        latestWeightEntry = combinedStateFromRepos.latestWeightEntry,
                        recentWeightEntries = combinedStateFromRepos.recentWeightEntries
                        // Reminder fields are managed by observeReminderPreferences
                    )
                }
            }
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            // Persist the choice first
            reminderRepository.updateReminderEnabled(enabled)

            // Let observeReminderPreferences handle UI state update and alarm scheduling/cancelling
            // based on the new persisted state.
            // No need for direct scheduling/cancelling or updateState here as the flow will pick it up.
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            // Persist the new time (this also sets isEnabled = true in the repository)
            reminderRepository.updateReminderTime(hour, minute)

            // Let observeReminderPreferences handle UI state update and alarm scheduling
            // based on the new persisted state.
            // No need for direct scheduling or updateState here as the flow will pick it up.
        }
    }
}
