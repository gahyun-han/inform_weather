package com.weather.outfit.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.weather.outfit.data.model.ClothingItem
import com.weather.outfit.data.model.OutfitFeedback
import com.weather.outfit.data.model.UserPreference
import com.weather.outfit.data.model.WeatherData

@Database(
    entities = [
        ClothingItem::class,
        OutfitFeedback::class,
        UserPreference::class,
        WeatherData::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clothingDao(): ClothingDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun weatherCacheDao(): WeatherCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inform_weather_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
