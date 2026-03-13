package com.weather.outfit.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weather.outfit.MainActivity
import com.weather.outfit.R
import com.weather.outfit.WeatherApp
import com.weather.outfit.data.db.AppDatabase
import com.weather.outfit.data.model.WeatherConditionType
import com.weather.outfit.data.repository.Result
import com.weather.outfit.data.repository.WeatherRepository
import com.weather.outfit.util.OutfitRecommender
import java.util.Calendar

class WeatherUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(context)
        val weatherRepo = WeatherRepository(db.weatherCacheDao())
        val prefDao = db.userPreferenceDao()

        val prefs = prefDao.getPreferencesSync() ?: return Result.success()

        // Fetch weather
        val weatherResult = if (prefs.useGpsLocation) {
            // GPS is handled in foreground; use last known coords
            weatherRepo.fetchWeatherByCoords(prefs.latitude, prefs.longitude)
        } else if (prefs.preferredCity.isNotEmpty()) {
            weatherRepo.fetchWeatherByCity(prefs.preferredCity)
        } else {
            return Result.success()
        }

        if (weatherResult is com.weather.outfit.data.repository.Result.Success) {
            val weather = weatherResult.data

            // Check if it's morning notification time
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            val notifTimeParts = prefs.morningNotificationTime.split(":")
            val notifHour = notifTimeParts.getOrNull(0)?.toIntOrNull() ?: 7
            val notifMinute = notifTimeParts.getOrNull(1)?.toIntOrNull() ?: 0

            if (prefs.morningNotificationEnabled &&
                hour == notifHour && minute in notifMinute..(notifMinute + 30)
            ) {
                sendMorningNotification(weather.temperature, weather.weatherCondition, prefs.coldSensitivityOffset)
            }
        }

        return Result.success()
    }

    private fun sendMorningNotification(temp: Float, condition: String, sensitivityOffset: Float) {
        val conditionType = WeatherConditionType.fromCondition(condition)
        val rec = OutfitRecommender.recommend(temp, temp, conditionType, sensitivityOffset)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tempInt = temp.toInt()
        val notification = NotificationCompat.Builder(context, WeatherApp.CHANNEL_MORNING_OUTFIT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("오늘 날씨 ${tempInt}°C - 코디 추천")
            .setContentText(rec.outfitDescription.take(80))
            .setStyle(NotificationCompat.BigTextStyle().bigText(rec.outfitDescription))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_MORNING, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    companion object {
        private const val NOTIF_ID_MORNING = 1001
    }
}
