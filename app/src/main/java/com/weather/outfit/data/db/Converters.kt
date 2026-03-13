package com.weather.outfit.data.db

import androidx.room.TypeConverter
import com.weather.outfit.data.model.ClothingCategory
import com.weather.outfit.data.model.ComfortLevel

class Converters {
    @TypeConverter
    fun fromClothingCategory(category: ClothingCategory): String = category.name

    @TypeConverter
    fun toClothingCategory(value: String): ClothingCategory =
        ClothingCategory.valueOf(value)

    @TypeConverter
    fun fromComfortLevel(level: ComfortLevel): String = level.name

    @TypeConverter
    fun toComfortLevel(value: String): ComfortLevel =
        ComfortLevel.valueOf(value)
}
