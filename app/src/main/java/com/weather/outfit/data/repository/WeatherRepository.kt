package com.weather.outfit.data.repository

import android.util.Log
import com.weather.outfit.BuildConfig
import com.weather.outfit.api.WeatherApi
import com.weather.outfit.data.db.WeatherCacheDao
import com.weather.outfit.data.model.WeatherData

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class WeatherRepository(private val weatherCacheDao: WeatherCacheDao) {

    private val apiService = WeatherApi.service
    private val apiKey = BuildConfig.WEATHER_API_KEY

    /** Fetch weather by GPS coordinates. Falls back to cache on error. */
    suspend fun fetchWeatherByCoords(lat: Double, lon: Double): Result<WeatherData> {
        return try {
            val response = apiService.getCurrentWeatherByCoords(lat, lon, apiKey)
            val weatherData = WeatherData(
                cityName = response.cityName,
                temperature = response.main.temp,
                feelsLike = response.main.feelsLike,
                tempMin = response.main.tempMin,
                tempMax = response.main.tempMax,
                humidity = response.main.humidity,
                windSpeed = response.wind.speed,
                weatherCondition = response.weather.firstOrNull()?.main ?: "Clear",
                weatherDescription = response.weather.firstOrNull()?.description ?: "",
                weatherIcon = response.weather.firstOrNull()?.icon ?: "01d",
                visibility = response.visibility,
                fetchedAt = System.currentTimeMillis()
            )
            weatherCacheDao.insert(weatherData)
            Result.Success(weatherData)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Failed to fetch weather by coords", e)
            // Return cached data if available
            val cached = weatherCacheDao.getWeather()
            if (cached != null) {
                Result.Success(cached)
            } else {
                Result.Error("날씨 정보를 가져올 수 없습니다: ${e.message}", e)
            }
        }
    }

    /** Fetch weather by city name. */
    suspend fun fetchWeatherByCity(city: String): Result<WeatherData> {
        return try {
            val response = apiService.getCurrentWeatherByCity(city, apiKey)
            val weatherData = WeatherData(
                cityName = response.cityName,
                temperature = response.main.temp,
                feelsLike = response.main.feelsLike,
                tempMin = response.main.tempMin,
                tempMax = response.main.tempMax,
                humidity = response.main.humidity,
                windSpeed = response.wind.speed,
                weatherCondition = response.weather.firstOrNull()?.main ?: "Clear",
                weatherDescription = response.weather.firstOrNull()?.description ?: "",
                weatherIcon = response.weather.firstOrNull()?.icon ?: "01d",
                visibility = response.visibility,
                fetchedAt = System.currentTimeMillis()
            )
            weatherCacheDao.insert(weatherData)
            Result.Success(weatherData)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Failed to fetch weather by city", e)
            val cached = weatherCacheDao.getWeather()
            if (cached != null) {
                Result.Success(cached)
            } else {
                Result.Error("날씨 정보를 가져올 수 없습니다: ${e.message}", e)
            }
        }
    }

    /** Get cached weather (may be stale). */
    suspend fun getCachedWeather(): WeatherData? = weatherCacheDao.getWeather()

    fun getWeatherLive() = weatherCacheDao.getWeatherLive()
}
