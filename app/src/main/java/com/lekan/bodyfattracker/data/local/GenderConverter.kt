package com.lekan.bodyfattracker.data.local

import androidx.room.TypeConverter
import com.lekan.bodyfattracker.ui.home.Gender // Assuming Gender enum is in this path

class GenderConverter {
    @TypeConverter
    fun fromGender(gender: Gender?): String? {
        return gender?.name // Stores the enum name as a String (e.g., "MALE", "FEMALE")
    }

    @TypeConverter
    fun toGender(value: String?): Gender? {
        return value?.let { enumValueOf<Gender>(it) } // Converts String back to Gender enum
    }
}
