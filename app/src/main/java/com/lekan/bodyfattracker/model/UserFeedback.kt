package com.lekan.bodyfattracker.model

import com.google.firebase.firestore.DocumentId // Import this
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserFeedback(
    @DocumentId val id: String = "", // Firestore document ID
    val message: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val appVersion: String = "",
    val deviceModel: String = "",
    val androidVersion: String = "",
    val locale: String = ""
) {
    constructor() : this("", "", null, "", "", "", "")
}