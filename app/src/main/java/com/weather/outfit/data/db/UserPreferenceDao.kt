package com.weather.outfit.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.weather.outfit.data.model.UserPreference

@Dao
interface UserPreferenceDao {

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getPreferences(): LiveData<UserPreference?>

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getPreferencesSync(): UserPreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: UserPreference)

    @Update
    suspend fun update(preference: UserPreference)

    @Query("UPDATE user_preferences SET coldSensitivityOffset = :offset WHERE id = 1")
    suspend fun updateColdSensitivity(offset: Float)

    @Query("UPDATE user_preferences SET comfortableTemperatureMap = :map WHERE id = 1")
    suspend fun updateComfortableTemperatureMap(map: String)

    @Query("UPDATE user_preferences SET preferredCity = :city WHERE id = 1")
    suspend fun updatePreferredCity(city: String)
}
