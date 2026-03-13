package com.weather.outfit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a clothing item stored in the user's closet.
 */
@Entity(tableName = "clothing_items")
data class ClothingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Display name of the clothing item */
    val name: String,

    /** Category: TOP, BOTTOM, OUTER, SHOES, ACCESSORY */
    val category: ClothingCategory,

    /** Warmth level: 1(very light) ~ 5(very warm) */
    val warmthLevel: Int,

    /** Local file path of the clothing photo */
    val imagePath: String,

    /** Suitable temperature range - minimum (°C) */
    val minTemp: Int,

    /** Suitable temperature range - maximum (°C) */
    val maxTemp: Int,

    /** Color tag for the clothing */
    val colorTag: String = "",

    /** User notes */
    val notes: String = "",

    /** Timestamp when added */
    val createdAt: Long = System.currentTimeMillis(),

    /** Whether the item is currently active/available */
    val isActive: Boolean = true
)

enum class ClothingCategory(val displayName: String, val koreanName: String) {
    TOP("Top", "상의"),
    BOTTOM("Bottom", "하의"),
    OUTER("Outer", "아우터"),
    SHOES("Shoes", "신발"),
    ACCESSORY("Accessory", "악세서리"),
    DRESS("Dress/One-piece", "원피스/드레스"),
    UNDERWEAR("Underwear/Base Layer", "이너웨어")
}
