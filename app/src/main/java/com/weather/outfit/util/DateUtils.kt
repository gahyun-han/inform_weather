package com.weather.outfit.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun todayString(): String = dateFormat.format(Date())

    fun formatDisplay(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr) ?: return dateStr
            displayFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun currentTimeString(): String = timeFormat.format(Date())

    fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "좋은 아침이에요! ☀️"
            in 12..17 -> "좋은 오후예요! 🌤️"
            in 18..21 -> "좋은 저녁이에요! 🌙"
            else -> "안녕하세요! 🌟"
        }
    }
}
