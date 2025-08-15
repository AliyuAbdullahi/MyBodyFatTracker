package com.lekan.bodyfattracker.domain

import com.lekan.bodyfattracker.model.UserProfile

interface IProfileRepository {
    fun saveProfile(userProfile: UserProfile)
    fun getProfile(): UserProfile?
}