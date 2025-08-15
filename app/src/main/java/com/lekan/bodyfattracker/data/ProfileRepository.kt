package com.lekan.bodyfattracker.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.lekan.bodyfattracker.domain.IProfileRepository
import com.lekan.bodyfattracker.model.UserProfile // Ensure UserProfile is importable and serializable

// Define a key for SharedPreferences
private const val PREFS_NAME = "user_profile_prefs"
private const val PROFILE_KEY = "user_profile_data"

class ProfileRepository(context: Context) : IProfileRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    override fun saveProfile(userProfile: UserProfile) {
        try {
            val userProfileJson = gson.toJson(userProfile)
            sharedPreferences.edit().apply {
                putString(PROFILE_KEY, userProfileJson)
                apply() // Asynchronously saves the data
            }
        } catch (e: Exception) {
            // Handle potential Gson serialization errors or SharedPreferences errors
            // For example, log the error: Log.e("ProfileRepository", "Error saving profile", e)
            // You might want to throw a custom exception or return a result type (e.g., Result<Unit, Error>)
            e.printStackTrace() // Replace with proper error handling
        }
    }

    override fun getProfile(): UserProfile? {
        return try {
            val userProfileJson = sharedPreferences.getString(PROFILE_KEY, null)
            if (userProfileJson != null) {
                gson.fromJson(userProfileJson, UserProfile::class.java)
            } else {
                null // No profile saved yet
            }
        } catch (e: Exception) {
            // Handle potential Gson deserialization errors or SharedPreferences errors
            // Log.e("ProfileRepository", "Error getting profile", e)
            e.printStackTrace() // Replace with proper error handling
            null // Return null or a default state in case of error
        }
    }

    // Optional: Add a method to clear the profile if needed
    fun clearProfile() {
        try {
            sharedPreferences.edit().apply {
                remove(PROFILE_KEY)
                apply()
            }
        } catch (e: Exception) {
            // Log.e("ProfileRepository", "Error clearing profile", e)
            e.printStackTrace()
        }
    }
}

