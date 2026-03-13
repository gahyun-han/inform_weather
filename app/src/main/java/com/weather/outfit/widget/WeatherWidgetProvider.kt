package com.weather.outfit.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.weather.outfit.MainActivity
import com.weather.outfit.R
import com.weather.outfit.data.db.AppDatabase
import com.weather.outfit.data.model.WeatherConditionType
import com.weather.outfit.util.OutfitRecommender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.PendingIntent
import android.content.Intent

/**
 * Home screen / lock screen widget that shows today's weather and character outfit.
 */
class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val weather = db.weatherCacheDao().getWeather()
            val prefs = db.userPreferenceDao().getPreferencesSync()

            val views = RemoteViews(context.packageName, R.layout.widget_weather)

            if (weather != null) {
                val sensitivityOffset = prefs?.coldSensitivityOffset ?: 0f
                val conditionType = WeatherConditionType.fromCondition(weather.weatherCondition)
                val rec = OutfitRecommender.recommend(
                    temperature = weather.temperature,
                    feelsLike = weather.feelsLike,
                    condition = conditionType,
                    sensitivityOffset = sensitivityOffset
                )

                views.setTextViewText(R.id.widget_temperature, "${weather.temperature.toInt()}°C")
                views.setTextViewText(R.id.widget_city, weather.cityName)
                views.setTextViewText(R.id.widget_outfit_desc, rec.outfitDescription.take(50))

                // Set character image based on outfit key
                val charResId = context.resources.getIdentifier(
                    "character_${rec.characterOutfitKey}", "drawable", context.packageName
                )
                if (charResId != 0) {
                    views.setImageViewResource(R.id.widget_character, charResId)
                } else {
                    views.setImageViewResource(R.id.widget_character, R.drawable.character_outfit_mild)
                }
            } else {
                views.setTextViewText(R.id.widget_temperature, "--°C")
                views.setTextViewText(R.id.widget_city, "날씨 로딩 중")
                views.setTextViewText(R.id.widget_outfit_desc, "앱을 열어 날씨를 불러오세요")
            }

            // Tap opens the app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
