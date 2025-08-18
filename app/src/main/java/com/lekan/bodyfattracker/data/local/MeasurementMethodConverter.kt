package com.lekan.bodyfattracker.data.local

import androidx.room.TypeConverter
import com.lekan.bodyfattracker.model.MeasurementMethod // Ensure this path is correct

class MeasurementMethodConverter {
    @TypeConverter
    fun fromMeasurementMethod(method: MeasurementMethod?): String? {
        return method?.name // Stores the enum name as a String
    }

    @TypeConverter
    fun toMeasurementMethod(value: String?): MeasurementMethod? {
        return value?.let {
            try {
                enumValueOf<MeasurementMethod>(it)
            } catch (e: IllegalArgumentException) {
                // Optionally handle cases where the string doesn't match an enum constant
                // For example, return a default or null
                null
            }
        }
    }
}