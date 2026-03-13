package com.weather.outfit.api

import com.google.gson.annotations.SerializedName

/** OpenWeatherMap Current Weather API Response */
data class WeatherResponse(
    @SerializedName("name") val cityName: String,
    @SerializedName("main") val main: MainData,
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("wind") val wind: WindData,
    @SerializedName("visibility") val visibility: Int = 10000,
    @SerializedName("dt") val timestamp: Long,
    @SerializedName("sys") val sys: SysData?
)

data class MainData(
    @SerializedName("temp") val temp: Float,
    @SerializedName("feels_like") val feelsLike: Float,
    @SerializedName("temp_min") val tempMin: Float,
    @SerializedName("temp_max") val tempMax: Float,
    @SerializedName("humidity") val humidity: Int
)

data class WeatherCondition(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class WindData(
    @SerializedName("speed") val speed: Float,
    @SerializedName("deg") val deg: Int = 0
)

data class SysData(
    @SerializedName("country") val country: String = "",
    @SerializedName("sunrise") val sunrise: Long = 0,
    @SerializedName("sunset") val sunset: Long = 0
)

/** OpenWeatherMap Forecast API Response */
data class ForecastResponse(
    @SerializedName("list") val list: List<ForecastItem>,
    @SerializedName("city") val city: ForecastCity
)

data class ForecastItem(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: MainData,
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("pop") val pop: Float = 0f
)

data class ForecastCity(
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String
)
