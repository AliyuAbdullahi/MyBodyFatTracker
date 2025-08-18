package com.lekan.bodyfattracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lekan.bodyfattracker.ui.home.Gender // Assuming Gender enum location

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Defaulting to 1 as typically there's only one user profile
    val name: String,
    val age: Int,
    val gender: Gender, // Ensure com.lekan.bodyfattracker.ui.home.Gender exists
    val bodyFatPercentGoal: Int?,
    val photoPath: String?
)