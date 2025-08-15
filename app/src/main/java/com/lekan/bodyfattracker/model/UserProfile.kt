package com.lekan.bodyfattracker.model

import com.lekan.bodyfattracker.ui.home.Gender

data class UserProfile(
    val name: String,
    val age: Int,
    val gender: Gender,
    val bodyFatPercentGoal: Int?,
    val photoPath: String?
)