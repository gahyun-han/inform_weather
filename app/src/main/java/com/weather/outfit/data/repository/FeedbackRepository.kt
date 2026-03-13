package com.weather.outfit.data.repository

import com.weather.outfit.data.db.FeedbackDao
import com.weather.outfit.data.model.ComfortLevel
import com.weather.outfit.data.model.OutfitFeedback

class FeedbackRepository(private val feedbackDao: FeedbackDao) {

    fun getAllFeedback() = feedbackDao.getAllFeedback()

    suspend fun saveFeedback(feedback: OutfitFeedback): Long = feedbackDao.insert(feedback)

    suspend fun getFeedbackForDate(date: String) = feedbackDao.getFeedbackForDate(date)

    suspend fun getRecentFeedback(limit: Int = 30) = feedbackDao.getRecentFeedback(limit)

    suspend fun getLatestFeedback() = feedbackDao.getLatestFeedback()

    /**
     * Calculates the user's personal temperature sensitivity offset.
     * Returns a float: negative = feels colder than average, positive = feels warmer.
     */
    suspend fun calculateSensitivityOffset(): Float {
        val recentFeedback = feedbackDao.getRecentFeedback(30)
        if (recentFeedback.isEmpty()) return 0f

        var totalOffset = 0f
        var count = 0

        for (feedback in recentFeedback) {
            val offset = when (feedback.comfortLevel) {
                ComfortLevel.TOO_COLD -> -3f
                ComfortLevel.SLIGHTLY_COLD -> -1.5f
                ComfortLevel.JUST_RIGHT -> 0f
                ComfortLevel.SLIGHTLY_HOT -> 1.5f
                ComfortLevel.TOO_HOT -> 3f
            }
            totalOffset += offset
            count++
        }

        return if (count > 0) (totalOffset / count).coerceIn(-5f, 5f) else 0f
    }

    /**
     * Given a temperature, returns the suggested outfit warmth level
     * based on past "Just Right" feedbacks near that temperature.
     */
    suspend fun getSuggestedWarmthForTemp(temp: Float): Float? {
        val margin = 3f
        return feedbackDao.getAvgComfortableWarmthForTemp(temp - margin, temp + margin)
    }
}
