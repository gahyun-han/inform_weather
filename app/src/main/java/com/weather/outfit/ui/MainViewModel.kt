package com.weather.outfit.ui

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.weather.outfit.data.db.AppDatabase
import com.weather.outfit.data.model.*
import com.weather.outfit.data.repository.ClothingRepository
import com.weather.outfit.data.repository.FeedbackRepository
import com.weather.outfit.data.repository.Result
import com.weather.outfit.data.repository.WeatherRepository
import com.weather.outfit.util.DateUtils
import com.weather.outfit.util.OutfitRecommender
import com.weather.outfit.util.TemperatureAnalyzer
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val weatherRepo = WeatherRepository(db.weatherCacheDao())
    private val clothingRepo = ClothingRepository(db.clothingDao())
    private val feedbackRepo = FeedbackRepository(db.feedbackDao())
    private val prefDao = db.userPreferenceDao()

    // --- LiveData ---
    val weather: LiveData<WeatherData?> = weatherRepo.getWeatherLive()
    val userPreferences: LiveData<UserPreference?> = prefDao.getPreferences()

    private val _recommendation = MutableLiveData<OutfitRecommendation?>()
    val recommendation: LiveData<OutfitRecommendation?> = _recommendation

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _feedbackInsight = MutableLiveData<String>()
    val feedbackInsight: LiveData<String> = _feedbackInsight

    private val _greeting = MutableLiveData<String>()
    val greeting: LiveData<String> = _greeting

    val todayFeedbackExists = MutableLiveData<Boolean>(false)

    init {
        _greeting.value = DateUtils.getGreeting()
        checkTodayFeedback()
        loadFeedbackInsight()
    }

    /** Fetch fresh weather using GPS coordinates. */
    fun fetchWeatherByLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = weatherRepo.fetchWeatherByCoords(lat, lon)) {
                is Result.Success -> {
                    _uiState.value = UiState.Success
                    generateRecommendation(result.data)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.message)
                    _errorMessage.value = result.message
                    weatherRepo.getCachedWeather()?.let { generateRecommendation(it) }
                }
                is Result.Loading -> { /* handled above */ }
            }
        }
    }

    /** Fetch fresh weather using city name. */
    fun fetchWeatherByCity(city: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = weatherRepo.fetchWeatherByCity(city)) {
                is Result.Success -> {
                    _uiState.value = UiState.Success
                    generateRecommendation(result.data)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.message)
                    _errorMessage.value = result.message
                }
                is Result.Loading -> { /* handled above */ }
            }
        }
    }

    /** Build outfit recommendation from weather data + user preferences + closet. */
    private suspend fun generateRecommendation(weatherData: WeatherData) {
        val prefs = prefDao.getPreferencesSync()
        val sensitivityOffset = prefs?.coldSensitivityOffset ?: 0f
        val closetItems = clothingRepo.getAllClothingItemsSync()
        val condition = WeatherConditionType.fromCondition(weatherData.weatherCondition)

        val rec = OutfitRecommender.recommend(
            temperature = weatherData.temperature,
            feelsLike = weatherData.feelsLike,
            condition = condition,
            sensitivityOffset = sensitivityOffset,
            userClosetItems = closetItems
        )
        _recommendation.postValue(rec)
    }

    /** Load the latest recommendation using cached weather (no network call). */
    fun loadRecommendationFromCache() {
        viewModelScope.launch {
            val cached = weatherRepo.getCachedWeather() ?: return@launch
            generateRecommendation(cached)
        }
    }

    private fun checkTodayFeedback() {
        viewModelScope.launch {
            val today = DateUtils.todayString()
            val existing = feedbackRepo.getFeedbackForDate(today)
            todayFeedbackExists.value = existing != null
        }
    }

    private fun loadFeedbackInsight() {
        viewModelScope.launch {
            val recentFeedback = feedbackRepo.getRecentFeedback(10)
            _feedbackInsight.value = TemperatureAnalyzer.generateFeedbackInsight(recentFeedback)
        }
    }

    /** Called when user sets a new preferred city. */
    fun updatePreferredCity(city: String) {
        viewModelScope.launch {
            val prefs = prefDao.getPreferencesSync()
            if (prefs != null) {
                prefDao.update(prefs.copy(preferredCity = city, useGpsLocation = false))
            } else {
                prefDao.insert(UserPreference(preferredCity = city, useGpsLocation = false))
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}
