package com.weather.outfit.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.weather.outfit.data.model.WeatherData

@Dao
interface WeatherCacheDao {

    @Query("SELECT * FROM weather_cache WHERE id = 1")
    fun getWeatherLive(): LiveData<WeatherData?>

    @Query("SELECT * FROM weather_cache WHERE id = 1")
    suspend fun getWeather(): WeatherData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weather: WeatherData)

    @Update
    suspend fun update(weather: WeatherData)

    @Query("DELETE FROM weather_cache")
    suspend fun clearAll()
}
