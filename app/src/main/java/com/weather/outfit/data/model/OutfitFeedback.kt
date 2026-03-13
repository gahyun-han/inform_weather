package com.weather.outfit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores user's temperature comfort feedback for an outfit worn on a specific day.
 * Used to personalize future outfit recommendations.
 */
@Entity(tableName = "outfit_feedback")
data class OutfitFeedback(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Date of the feedback (YYYY-MM-DD format) */
    val date: String,

    /** Actual temperature when outfit was worn (°C) */
    val temperature: Float,

    /** Felt temperature (wind chill) */
    val feelsLike: Float,

    /** Weather condition (SUNNY, CLOUDY, RAINY, SNOWY, WINDY) */
    val weatherCondition: String,

    /** User's comfort rating */
    val comfortLevel: ComfortLevel,

    /** Clothing item IDs worn on this day (comma-separated) */
    val clothingItemIds: String = "",

    /** Total warmth score of the outfit worn */
    val outfitWarmthScore: Int = 0,

    /** User's additional notes */
    val notes: String = "",

    /** Timestamp */
    val createdAt: Long = System.currentTimeMillis()
)

enum class ComfortLevel(val displayName: String, val koreanName: String, val emoji: String) {
    TOO_COLD("Too Cold", "많이 추웠다", "🥶"),
    SLIGHTLY_COLD("Slightly Cold", "조금 추웠다", "😨"),
    JUST_RIGHT("Just Right", "딱 좋았다", "😊"),
    SLIGHTLY_HOT("Slightly Hot", "조금 더웠다", "😅"),
    TOO_HOT("Too Hot", "많이 더웠다", "🥵")
}
