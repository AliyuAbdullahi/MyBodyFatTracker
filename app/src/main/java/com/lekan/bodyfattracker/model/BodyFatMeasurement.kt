package com.lekan.bodyfattracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters // For MeasurementMethodConverter
import com.lekan.bodyfattracker.data.local.MeasurementMethodConverter // Assuming path

// Renaming 'Type' to 'MeasurementMethod' for clarity and to avoid conflict with kotlin.Type
enum class MeasurementMethod {
    THREE_POINTS, // Was THREE_SITE_SKINFOLD in an earlier version, using current name
    SEVEN_POINTS, // Was SEVEN_SITE_SKINFOLD, using current name
    OTHER // For future flexibility or manual entries
}

@Entity(tableName = "body_fat_measurements")
@TypeConverters(MeasurementMethodConverter::class) // Add this for MeasurementMethod enum
data class BodyFatMeasurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // 'date: String' is redundant if we have 'timeStamp: Long'.
    // We'll primarily use timeStamp and convert it for display.
    val timeStamp: Long, // Use this as the definitive source of date/time
    val percentage: Double, // Changed to Double for more precision
    val method: MeasurementMethod, // Changed from 'type' to 'method' and used the new enum
    val notes: String? = null // Optional notes for the measurement
)
