package com.lekan.bodyfattracker.data.local

import androidx.room.TypeConverter
import com.lekan.bodyfattracker.model.WeightUnit // Ensure this path is correct

class WeightUnitConverter {
    @TypeConverter
    fun fromWeightUnit(unit: WeightUnit?): String? {
        return unit?.name // Stores the enum name as a String
    }

    @TypeConverter
    fun toWeightUnit(value: String?): WeightUnit? {
        return value?.let {
            try {
                enumValueOf<WeightUnit>(it)
            } catch (e: IllegalArgumentException) {
                // Optionally handle cases where the string doesn't match an enum constant
                // For example, return a default or null
                null
            }
        }
    }
}
