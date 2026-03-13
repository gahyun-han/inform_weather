package com.weather.outfit.util

import com.weather.outfit.data.model.ComfortLevel
import com.weather.outfit.data.model.OutfitFeedback

/**
 * Analyzes historical feedback to understand user's temperature sensitivity
 * and build a personalized temperature comfort profile.
 */
object TemperatureAnalyzer {

    /**
     * Calculates personal temperature sensitivity offset from feedback history.
     * Negative = user feels colder than average (dress warmer)
     * Positive = user feels warmer than average (dress lighter)
     *
     * @return offset in degrees Celsius, clamped to [-5, 5]
     */
    fun calculateSensitivityOffset(feedbackHistory: List<OutfitFeedback>): Float {
        if (feedbackHistory.isEmpty()) return 0f

        val weights = mapOf(
            ComfortLevel.TOO_COLD to -3f,
            ComfortLevel.SLIGHTLY_COLD to -1.5f,
            ComfortLevel.JUST_RIGHT to 0f,
            ComfortLevel.SLIGHTLY_HOT to 1.5f,
            ComfortLevel.TOO_HOT to 3f
        )

        // Use exponential decay so recent feedback matters more
        var weightedSum = 0f
        var totalWeight = 0f

        feedbackHistory.forEachIndexed { index, feedback ->
            val recencyWeight = Math.pow(0.9, index.toDouble()).toFloat()
            val comfortOffset = weights[feedback.comfortLevel] ?: 0f
            weightedSum += comfortOffset * recencyWeight
            totalWeight += recencyWeight
        }

        return if (totalWeight > 0) (weightedSum / totalWeight).coerceIn(-5f, 5f) else 0f
    }

    /**
     * Generates a human-readable description of the user's sensitivity.
     */
    fun getSensitivityDescription(offset: Float): String = when {
        offset < -3f -> "추위를 많이 타는 편이에요 🧊"
        offset < -1.5f -> "약간 추위를 타는 편이에요"
        offset in -1.5f..1.5f -> "평균적인 체감온도를 가지고 있어요 😊"
        offset < 3f -> "약간 더위를 타는 편이에요"
        else -> "더위를 많이 타는 편이에요 🔥"
    }

    /**
     * Returns how many degrees the effective temperature shifts due to user's sensitivity.
     * Used for display purposes (e.g., "체감온도 +2°C 보정 중").
     */
    fun getAdjustmentText(offset: Float): String {
        return if (offset == 0f) ""
        else if (offset < 0) "체감온도 ${offset.toInt()}°C 조정 (추위를 잘 타요)"
        else "체감온도 +${offset.toInt()}°C 조정 (더위를 잘 타요)"
    }

    /**
     * Based on recent feedback trend, generate a motivational tip for the user.
     */
    fun generateFeedbackInsight(feedbackHistory: List<OutfitFeedback>): String {
        if (feedbackHistory.size < 3) return "피드백을 더 남겨주시면 맞춤형 코디를 추천해드릴게요! 👕"

        val recentFive = feedbackHistory.take(5)
        val coldCount = recentFive.count {
            it.comfortLevel == ComfortLevel.TOO_COLD || it.comfortLevel == ComfortLevel.SLIGHTLY_COLD
        }
        val hotCount = recentFive.count {
            it.comfortLevel == ComfortLevel.TOO_HOT || it.comfortLevel == ComfortLevel.SLIGHTLY_HOT
        }

        return when {
            coldCount >= 3 -> "요즘 추위를 많이 느끼시는 것 같아요. 한 단계 더 따뜻한 코디를 추천할게요! 🧥"
            hotCount >= 3 -> "요즘 더위를 많이 느끼시는 것 같아요. 좀 더 시원한 코디를 추천할게요! 👕"
            recentFive.count { it.comfortLevel == ComfortLevel.JUST_RIGHT } >= 3 ->
                "최근 코디가 딱 좋았군요! 앞으로도 비슷한 스타일로 추천할게요 😊"
            else -> "다양한 피드백을 남겨주셔서 점점 더 정확한 추천이 가능해지고 있어요 📊"
        }
    }
}
