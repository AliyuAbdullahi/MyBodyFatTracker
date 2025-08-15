package com.lekan.bodyfattracker.model

data class BodyFatInfo(
    val percentage: Int,
    val date: String,
    val timeStamp: Long,
    val type: Type
) {
    enum class Type {
        THREE_POINTS, SEVEN_POINTS
    }
}