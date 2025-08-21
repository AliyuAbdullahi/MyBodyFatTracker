package com.lekan.bodyfattracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lekan.bodyfattracker.model.WeightUnit // Make sure this import is correct
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Define DataStore instance at the top level
val Context.userSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserSettings(
    val displayWeightUnit: WeightUnit
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val DISPLAY_WEIGHT_UNIT = stringPreferencesKey("display_weight_unit")
    }

    val userSettingsFlow: Flow<UserSettings> = context.userSettingsDataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                // Log.e("UserPreferencesRepo", "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapUserSettings(preferences)
        }

    suspend fun updateDisplayWeightUnit(weightUnit: WeightUnit) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.DISPLAY_WEIGHT_UNIT] = weightUnit.name
        }
    }

    private fun mapUserSettings(preferences: Preferences): UserSettings {
        val displayWeightUnit = WeightUnit.valueOf(
            preferences[PreferencesKeys.DISPLAY_WEIGHT_UNIT] ?: WeightUnit.KG.name // Default to KG
        )
        return UserSettings(displayWeightUnit = displayWeightUnit)
    }
}
