package com.weather.outfit.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.weather.outfit.data.db.AppDatabase
import com.weather.outfit.data.model.ComfortLevel
import com.weather.outfit.data.model.OutfitFeedback
import com.weather.outfit.data.model.UserPreference
import com.weather.outfit.data.repository.FeedbackRepository
import com.weather.outfit.util.DateUtils
import com.weather.outfit.util.TemperatureAnalyzer
import kotlinx.coroutines.launch

class FeedbackViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val feedbackRepo = FeedbackRepository(db.feedbackDao())
    private val prefDao = db.userPreferenceDao()

    val allFeedback: LiveData<List<OutfitFeedback>> = feedbackRepo.getAllFeedback()

    private val _savedSuccessfully = MutableLiveData<Boolean>(false)
    val savedSuccessfully: LiveData<Boolean> = _savedSuccessfully

    private val _todayFeedback = MutableLiveData<OutfitFeedback?>()
    val todayFeedback: LiveData<OutfitFeedback?> = _todayFeedback

    private val _sensitivityText = MutableLiveData<String>()
    val sensitivityText: LiveData<String> = _sensitivityText

    private val _insight = MutableLiveData<String>()
    val insight: LiveData<String> = _insight

    init {
        loadTodayFeedback()
        loadSensitivityInfo()
    }

    private fun loadTodayFeedback() {
        viewModelScope.launch {
            val today = DateUtils.todayString()
            _todayFeedback.value = feedbackRepo.getFeedbackForDate(today)
        }
    }

    private fun loadSensitivityInfo() {
        viewModelScope.launch {
            val recentFeedback = feedbackRepo.getRecentFeedback(20)
            val offset = TemperatureAnalyzer.calculateSensitivityOffset(recentFeedback)
            _sensitivityText.value = TemperatureAnalyzer.getSensitivityDescription(offset)
            _insight.value = TemperatureAnalyzer.generateFeedbackInsight(recentFeedback)
        }
    }

    /**
     * Saves outfit feedback for today and updates user's personal sensitivity.
     */
    fun saveFeedback(
        comfortLevel: ComfortLevel,
        temperature: Float,
        feelsLike: Float,
        weatherCondition: String,
        clothingItemIds: List<Long>,
        outfitWarmthScore: Int,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val today = DateUtils.todayString()

            val feedback = OutfitFeedback(
                date = today,
                temperature = temperature,
                feelsLike = feelsLike,
                weatherCondition = weatherCondition,
                comfortLevel = comfortLevel,
                clothingItemIds = clothingItemIds.joinToString(","),
                outfitWarmthScore = outfitWarmthScore,
                notes = notes
            )

            feedbackRepo.saveFeedback(feedback)

            // Recalculate and update sensitivity offset
            val allRecent = feedbackRepo.getRecentFeedback(30)
            val newOffset = TemperatureAnalyzer.calculateSensitivityOffset(allRecent)
            updateSensitivityOffset(newOffset)

            _todayFeedback.value = feedback
            _savedSuccessfully.value = true
            loadSensitivityInfo()
        }
    }

    private suspend fun updateSensitivityOffset(offset: Float) {
        val prefs = prefDao.getPreferencesSync()
        if (prefs != null) {
            prefDao.update(prefs.copy(coldSensitivityOffset = offset))
        } else {
            prefDao.insert(UserPreference(coldSensitivityOffset = offset))
        }
    }

    fun resetSavedFlag() {
        _savedSuccessfully.value = false
    }
}
