package com.weather.outfit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached weather data to avoid excessive API calls.
 */
@Entity(tableName = "weather_cache")
data class WeatherData(
    @PrimaryKey
    val id: Int = 1,

    val cityName: String,
    val temperature: Float,
    val feelsLike: Float,
    val tempMin: Float,
    val tempMax: Float,
    val humidity: Int,
    val windSpeed: Float,
    val weatherCondition: String,  // e.g., "Clear", "Rain", "Snow"
    val weatherDescription: String, // e.g., "clear sky", "light rain"
    val weatherIcon: String,        // OpenWeatherMap icon code
    val uvIndex: Float = 0f,
    val visibility: Int = 10000,    // meters
    val fetchedAt: Long = System.currentTimeMillis()
) {
    val isStale: Boolean
        get() = System.currentTimeMillis() - fetchedAt > 30 * 60 * 1000 // 30 minutes

    val temperatureInt: Int
        get() = temperature.toInt()

    val feelsLikeInt: Int
        get() = feelsLike.toInt()
}

/**
 * Hourly/daily forecast entry.
 */
data class ForecastEntry(
    val dateTime: Long,
    val temperature: Float,
    val feelsLike: Float,
    val weatherCondition: String,
    val weatherIcon: String,
    val pop: Float = 0f  // Probability of precipitation
)

/**
 * Determines the weather condition category for outfit selection.
 */
enum class WeatherConditionType {
    CLEAR, PARTLY_CLOUDY, CLOUDY, LIGHT_RAIN, HEAVY_RAIN, SNOW, THUNDERSTORM, FOGGY, WINDY;

    companion object {
        fun fromCondition(condition: String): WeatherConditionType {
            return when (condition.lowercase()) {
                "clear" -> CLEAR
                "clouds" -> PARTLY_CLOUDY
                "overcast clouds" -> CLOUDY
                "drizzle", "light rain" -> LIGHT_RAIN
                "rain", "heavy rain", "shower rain" -> HEAVY_RAIN
                "snow" -> SNOW
                "thunderstorm" -> THUNDERSTORM
                "mist", "fog", "haze" -> FOGGY
                else -> CLEAR
            }
        }
    }
}
