package com.lekan.bodyfattracker.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserFeedback(
    val message: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val appVersion: String = "",
    val deviceModel: String = "",
    val androidVersion: String = "",
    val locale: String = ""
) {
    // No-argument constructor for Firestore deserialization
    constructor() : this( "", null, "", "", "", "")
}
