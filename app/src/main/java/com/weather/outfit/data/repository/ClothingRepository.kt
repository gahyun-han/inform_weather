package com.weather.outfit.data.repository

import com.weather.outfit.data.db.ClothingDao
import com.weather.outfit.data.model.ClothingCategory
import com.weather.outfit.data.model.ClothingItem

class ClothingRepository(private val clothingDao: ClothingDao) {

    fun getAllClothingItems() = clothingDao.getAllClothingItems()

    suspend fun getAllClothingItemsSync() = clothingDao.getAllClothingItemsSync()

    fun getByCategory(category: ClothingCategory) = clothingDao.getByCategory(category)

    suspend fun getItemsSuitableForTemp(temp: Int) = clothingDao.getItemsSuitableForTemp(temp)

    suspend fun getItemsByCategoryAndTemp(category: ClothingCategory, temp: Int) =
        clothingDao.getItemsByCategoryAndTemp(category, temp)

    suspend fun addClothingItem(item: ClothingItem): Long = clothingDao.insert(item)

    suspend fun updateClothingItem(item: ClothingItem) = clothingDao.update(item)

    suspend fun removeClothingItem(id: Long) = clothingDao.softDelete(id)

    suspend fun getById(id: Long) = clothingDao.getById(id)

    suspend fun getCount() = clothingDao.getCount()

    /**
     * Selects a complete outfit for the given temperature.
     * Returns a map of category -> best matching item.
     */
    suspend fun selectOutfitForTemp(temp: Int): Map<ClothingCategory, ClothingItem?> {
        val allItems = clothingDao.getAllClothingItemsSync()
        val result = mutableMapOf<ClothingCategory, ClothingItem?>()

        // Priority categories for a complete outfit
        val priorityCategories = listOf(
            ClothingCategory.TOP,
            ClothingCategory.BOTTOM,
            ClothingCategory.OUTER,
            ClothingCategory.SHOES,
            ClothingCategory.ACCESSORY
        )

        for (category in priorityCategories) {
            val suitable = allItems.filter { item ->
                item.category == category &&
                item.isActive &&
                item.minTemp <= temp &&
                item.maxTemp >= temp
            }
            // Pick the one with warmth closest to the ideal for this temperature
            result[category] = suitable.minByOrNull { item ->
                kotlin.math.abs(item.warmthLevel - idealWarmthLevel(temp))
            }
        }

        return result
    }

    private fun idealWarmthLevel(temp: Int): Int = when {
        temp >= 25 -> 1
        temp >= 20 -> 2
        temp >= 15 -> 3
        temp >= 5  -> 4
        else       -> 5
    }
}
