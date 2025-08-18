package com.lekan.bodyfattracker.domain

import com.lekan.bodyfattracker.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface IProfileRepository {

    /**
     * Saves or updates the user's profile in the database.
     * @param userProfile The profile to save.
     */
    suspend fun saveProfile(userProfile: UserProfile)

    /**
     * Retrieves the user's profile as a Flow.
     * Emits null if no profile is found.
     * @return A Flow emitting the UserProfile or null.
     */
    fun getProfile(): Flow<UserProfile?>

    suspend fun getProfileSync(): UserProfile?

    /**
     * Deletes the current user's profile from the database.
     */
    suspend fun clearProfile()
}