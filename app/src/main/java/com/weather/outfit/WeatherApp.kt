package com.weather.outfit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.*
import com.weather.outfit.worker.WeatherUpdateWorker
import java.util.concurrent.TimeUnit

class WeatherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleWeatherUpdates()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Morning outfit notification channel
            val morningChannel = NotificationChannel(
                CHANNEL_MORNING_OUTFIT,
                "아침 코디 추천",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "매일 아침 날씨에 맞는 코디를 알려드려요"
            }

            // Weather update channel
            val weatherChannel = NotificationChannel(
                CHANNEL_WEATHER_UPDATE,
                "날씨 업데이트",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "날씨 정보 백그라운드 업데이트"
            }

            manager.createNotificationChannels(listOf(morningChannel, weatherChannel))
        }
    }

    private fun scheduleWeatherUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val weatherWorkRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
            30, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weather_update",
            ExistingPeriodicWorkPolicy.KEEP,
            weatherWorkRequest
        )
    }

    companion object {
        const val CHANNEL_MORNING_OUTFIT = "morning_outfit"
        const val CHANNEL_WEATHER_UPDATE = "weather_update"
    }
}
