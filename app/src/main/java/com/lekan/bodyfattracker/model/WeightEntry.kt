package com.lekan.bodyfattracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
// import androidx.room.TypeConverters // No longer needed for WeightUnit on this entity
// import com.lekan.bodyfattracker.data.local.WeightUnitConverter // No longer needed for this entity

// This enum might still be useful for UI selection, but not directly in WeightEntry
enum class WeightUnit {
    KG,
    LBS
}

@Entity(tableName = "weight_entries")
// @TypeConverters(WeightUnitConverter::class) // Removed
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Double, // This will ALWAYS be in KG
    val unit: WeightUnit, // This will ALWAYS be KG
    val timeStamp: Long, // Stores the date and time of the entry as a Unix timestamp
    val notes: String? = null // Optional notes
)
