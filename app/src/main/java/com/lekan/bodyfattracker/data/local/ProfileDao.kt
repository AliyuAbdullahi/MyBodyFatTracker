package com.lekan.bodyfattracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters // For Gender converter if needed
import com.lekan.bodyfattracker.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(GenderConverter::class) // Add this if Gender is an enum and needs conversion
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // Since we usually have only one profile, we can fetch it like this.
    // The id is fixed to 1 in UserProfile entity.
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): UserProfile?

    @Query("DELETE FROM user_profile WHERE id = 1")
    suspend fun deleteProfile() // Or pass UserProfile object if preferred
}
