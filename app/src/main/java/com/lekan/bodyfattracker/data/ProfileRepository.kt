package com.lekan.bodyfattracker.data

import com.lekan.bodyfattracker.data.local.ProfileDao
import com.lekan.bodyfattracker.domain.IProfileRepository
import com.lekan.bodyfattracker.model.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Marks this repository as a singleton, managed by Hilt
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao // Hilt will inject the ProfileDao instance
) : IProfileRepository {

    override suspend fun saveProfile(userProfile: UserProfile) {
        // The UserProfile entity has a default id = 1.
        // insertOrUpdateProfile will replace if a profile with id = 1 already exists.
        profileDao.insertOrUpdateProfile(userProfile)
    }

    override fun getProfile(): Flow<UserProfile?> {
        return profileDao.getProfile()
    }

    override suspend fun clearProfile() {
        profileDao.deleteProfile()
    }

    override suspend fun getProfileSync(): UserProfile? = profileDao.getProfileSync()
}