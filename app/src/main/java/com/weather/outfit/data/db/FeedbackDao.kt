package com.weather.outfit.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.weather.outfit.data.model.ComfortLevel
import com.weather.outfit.data.model.OutfitFeedback

@Dao
interface FeedbackDao {

    @Query("SELECT * FROM outfit_feedback ORDER BY createdAt DESC")
    fun getAllFeedback(): LiveData<List<OutfitFeedback>>

    @Query("SELECT * FROM outfit_feedback ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentFeedback(limit: Int = 30): List<OutfitFeedback>

    @Query("SELECT * FROM outfit_feedback WHERE date = :date LIMIT 1")
    suspend fun getFeedbackForDate(date: String): OutfitFeedback?

    @Query("SELECT * FROM outfit_feedback WHERE temperature BETWEEN :minTemp AND :maxTemp ORDER BY createdAt DESC")
    suspend fun getFeedbackForTempRange(minTemp: Float, maxTemp: Float): List<OutfitFeedback>

    @Query("SELECT AVG(outfitWarmthScore) FROM outfit_feedback WHERE comfortLevel = 'JUST_RIGHT' AND temperature BETWEEN :minTemp AND :maxTemp")
    suspend fun getAvgComfortableWarmthForTemp(minTemp: Float, maxTemp: Float): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feedback: OutfitFeedback): Long

    @Update
    suspend fun update(feedback: OutfitFeedback)

    @Query("SELECT COUNT(*) FROM outfit_feedback")
    suspend fun getCount(): Int

    @Query("SELECT * FROM outfit_feedback ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestFeedback(): OutfitFeedback?
}
