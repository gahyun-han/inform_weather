package com.weather.outfit.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.weather.outfit.data.model.ClothingCategory
import com.weather.outfit.data.model.ClothingItem

@Dao
interface ClothingDao {

    @Query("SELECT * FROM clothing_items WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllClothingItems(): LiveData<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getAllClothingItemsSync(): List<ClothingItem>

    @Query("SELECT * FROM clothing_items WHERE category = :category AND isActive = 1 ORDER BY warmthLevel ASC")
    fun getByCategory(category: ClothingCategory): LiveData<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items WHERE minTemp <= :temp AND maxTemp >= :temp AND isActive = 1 ORDER BY warmthLevel ASC")
    suspend fun getItemsSuitableForTemp(temp: Int): List<ClothingItem>

    @Query("SELECT * FROM clothing_items WHERE category = :category AND minTemp <= :temp AND maxTemp >= :temp AND isActive = 1")
    suspend fun getItemsByCategoryAndTemp(category: ClothingCategory, temp: Int): List<ClothingItem>

    @Query("SELECT * FROM clothing_items WHERE id = :id")
    suspend fun getById(id: Long): ClothingItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClothingItem): Long

    @Update
    suspend fun update(item: ClothingItem)

    @Query("UPDATE clothing_items SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Delete
    suspend fun hardDelete(item: ClothingItem)

    @Query("SELECT COUNT(*) FROM clothing_items WHERE isActive = 1")
    suspend fun getCount(): Int
}
