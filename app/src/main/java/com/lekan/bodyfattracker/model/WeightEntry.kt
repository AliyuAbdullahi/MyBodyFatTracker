package com.lekan.bodyfattracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.lekan.bodyfattracker.data.local.WeightUnitConverter // We'll create this converter

enum class WeightUnit {
    KG,
    LBS
}

@Entity(tableName = "weight_entries")
@TypeConverters(WeightUnitConverter::class) // For WeightUnit enum
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Double,
    val unit: WeightUnit,
    val timeStamp: Long, // Stores the date and time of the entry as a Unix timestamp
    val notes: String? = null // Optional notes
)
