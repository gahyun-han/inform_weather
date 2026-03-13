package com.weather.outfit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores personalized user preferences learned from feedback.
 */
@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey
    val id: Int = 1, // Single row table

    /** User's name for character greeting */
    val userName: String = "사용자",

    /** Cold sensitivity adjustment (negative = feels colder, positive = feels warmer) */
    val coldSensitivityOffset: Float = 0f,

    /** Learned comfortable temperature per warmth score (JSON string) */
    val comfortableTemperatureMap: String = "{}",

    /** Preferred city for weather */
    val preferredCity: String = "",

    /** Whether to use GPS for location */
    val useGpsLocation: Boolean = true,

    /** Latitude for weather (when GPS is off) */
    val latitude: Double = 37.5665,

    /** Longitude for weather (when GPS is off) */
    val longitude: Double = 126.9780,

    /** Character appearance: FEMALE, MALE, NEUTRAL */
    val characterGender: String = "FEMALE",

    /** Character skin tone: LIGHT, MEDIUM, DARK */
    val characterSkinTone: String = "LIGHT",

    /** Whether to show weather widget on lock screen */
    val showLockScreenWidget: Boolean = true,

    /** Morning notification time (HH:mm format) */
    val morningNotificationTime: String = "07:00",

    /** Whether morning notification is enabled */
    val morningNotificationEnabled: Boolean = true,

    /** Temperature unit: CELSIUS, FAHRENHEIT */
    val temperatureUnit: String = "CELSIUS",

    /** Last updated timestamp */
    val lastUpdatedAt: Long = System.currentTimeMillis()
)
