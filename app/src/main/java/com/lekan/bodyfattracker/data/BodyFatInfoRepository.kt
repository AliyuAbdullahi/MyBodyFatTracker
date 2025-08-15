package com.lekan.bodyfattracker.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository
import com.lekan.bodyfattracker.model.BodyFatInfo // Make sure to import your BodyFatInfo class
import androidx.core.content.edit

class BodyFatInfoRepository(context: Context) : IBodyFatInfoRepository { // Implement the interface

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("BodyFatInfoPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_BODY_FAT_INFO_LIST = "body_fat_info_list"
    }

    /**
     * Saves a list of BodyFatInfo objects to SharedPreferences.
     * The list is serialized to a JSON string before saving.
     */
    override fun saveBodyFatInfoList(bodyFatInfoList: List<BodyFatInfo>) { // Add 'override'
        val jsonString = serializeBodyFatInfoList(bodyFatInfoList)
        sharedPreferences.edit { putString(KEY_BODY_FAT_INFO_LIST, jsonString) }
    }

    /**
     * Retrieves a list of BodyFatInfo objects from SharedPreferences.
     * The JSON string is deserialized back into a list of BodyFatInfo objects.
     * Returns an empty list if no data is found.
     */
    override fun getBodyFatInfoList(): List<BodyFatInfo> { // Add 'override'
        val jsonString = sharedPreferences.getString(KEY_BODY_FAT_INFO_LIST, null)
        return if (jsonString != null) {
            deserializeBodyFatInfoList(jsonString)
        } else {
            emptyList()
        }
    }

    /**
     * Adds a single BodyFatInfo object to the existing list in SharedPreferences.
     */
    override fun addBodyFatInfo(bodyFatInfo: BodyFatInfo) { // Add 'override'
        val currentList = getBodyFatInfoList().toMutableList()
        currentList.add(bodyFatInfo)
        saveBodyFatInfoList(currentList)
    }

    /**
     * Clears all BodyFatInfo data from SharedPreferences.
     */
    override fun clearAllBodyFatInfo() { // Add 'override'
        sharedPreferences.edit { remove(KEY_BODY_FAT_INFO_LIST) }
    }


    /**
     * Retrieves the most recent BodyFatInfo reading based on the timeStamp.
     * Returns null if no readings are available.
     */
    override fun getLastBodyFatInfo(): BodyFatInfo? {
        val allReadings = getBodyFatInfoList()
        // Sort by timeStamp in descending order to get the latest first,
        // or simply find the maxBy timeStamp.
        return allReadings.maxByOrNull { it.timeStamp }
    }

    /**
     * Serializes a list of BodyFatInfo objects into a JSON string.
     */
    private fun serializeBodyFatInfoList(bodyFatInfoList: List<BodyFatInfo>): String {
        return gson.toJson(bodyFatInfoList)
    }

    /**
     * Deserializes a JSON string into a list of BodyFatInfo objects.
     */
    private fun deserializeBodyFatInfoList(jsonString: String): List<BodyFatInfo> {
        val type = object : TypeToken<List<BodyFatInfo>>() {}.type
        return gson.fromJson(jsonString, type)
    }
}
