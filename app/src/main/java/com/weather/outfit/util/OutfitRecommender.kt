package com.weather.outfit.util

import com.weather.outfit.data.model.*

/**
 * Core engine that recommends outfits based on temperature, weather conditions,
 * and learned user preferences.
 */
object OutfitRecommender {

    /**
     * Returns the recommended outfit for the given conditions.
     *
     * @param temperature Actual temperature (°C)
     * @param feelsLike Feels-like temperature (°C)
     * @param condition Weather condition
     * @param sensitivityOffset User's personal sensitivity (negative = feels colder)
     * @param userClosetItems Items from user's closet to match against
     */
    fun recommend(
        temperature: Float,
        feelsLike: Float,
        condition: WeatherConditionType,
        sensitivityOffset: Float = 0f,
        userClosetItems: List<ClothingItem> = emptyList()
    ): OutfitRecommendation {
        // Adjust temperature with user's personal sensitivity
        val effectiveTemp = feelsLike - sensitivityOffset

        val outfitKey = OutfitRecommendation.getOutfitKey(effectiveTemp, condition)
        val description = OutfitRecommendation.getOutfitDescription(effectiveTemp, condition)
        val warmthLevel = getWarmthLevelForTemp(effectiveTemp)

        // Find matching items from user's closet
        val suggestedItems = if (userClosetItems.isNotEmpty()) {
            selectBestClosetItems(effectiveTemp, condition, userClosetItems)
        } else {
            emptyList()
        }

        return OutfitRecommendation(
            temperature = temperature,
            condition = condition,
            characterOutfitKey = outfitKey,
            outfitDescription = description,
            warmthLevel = warmthLevel,
            suggestedClosetItems = suggestedItems,
            weatherTip = getWeatherTip(temperature, condition),
            umbrellaRecommended = condition in listOf(
                WeatherConditionType.LIGHT_RAIN,
                WeatherConditionType.HEAVY_RAIN,
                WeatherConditionType.THUNDERSTORM
            ),
            sunscreenRecommended = condition == WeatherConditionType.CLEAR && temperature >= 22
        )
    }

    private fun getWarmthLevelForTemp(temp: Float): Int = when {
        temp >= 28 -> 1
        temp >= 23 -> 1
        temp >= 20 -> 2
        temp >= 17 -> 2
        temp >= 12 -> 3
        temp >= 9  -> 4
        temp >= 5  -> 4
        temp >= 0  -> 5
        else       -> 5
    }

    /**
     * Selects the best matching items from the user's closet.
     * Tries to build a complete outfit: outer, top, bottom, shoes.
     */
    private fun selectBestClosetItems(
        temp: Float,
        condition: WeatherConditionType,
        items: List<ClothingItem>
    ): List<ClothingItem> {
        val result = mutableListOf<ClothingItem>()
        val targetWarmth = getWarmthLevelForTemp(temp)
        val tempInt = temp.toInt()

        val categories = listOf(
            ClothingCategory.OUTER,
            ClothingCategory.TOP,
            ClothingCategory.BOTTOM,
            ClothingCategory.SHOES
        )

        for (category in categories) {
            val suitable = items.filter { item ->
                item.category == category &&
                item.minTemp <= tempInt &&
                item.maxTemp >= tempInt
            }
            if (suitable.isNotEmpty()) {
                // Pick item with warmth closest to target
                val best = suitable.minByOrNull { kotlin.math.abs(it.warmthLevel - targetWarmth) }
                best?.let { result.add(it) }
            }
        }

        return result
    }

    private fun getWeatherTip(temp: Float, condition: WeatherConditionType): String {
        return when {
            condition == WeatherConditionType.SNOW -> "눈이 올 예정이에요! 미끄럼에 주의하고 방수 신발을 추천해요 🌨️"
            condition == WeatherConditionType.THUNDERSTORM -> "천둥번개 예보! 가급적 실내에 계세요 ⛈️"
            condition == WeatherConditionType.HEAVY_RAIN -> "폭우 예보입니다. 큰 우산과 방수 신발 필수! ☔"
            condition == WeatherConditionType.LIGHT_RAIN -> "비가 올 것 같아요. 우산 챙기는 거 잊지 마세요 🌂"
            condition == WeatherConditionType.FOGGY -> "안개가 끼어 있어요. 시야가 낮으니 조심하세요 🌫️"
            condition == WeatherConditionType.WINDY -> "바람이 강하게 불 예정이에요. 바람막이를 준비하세요 💨"
            temp >= 35 -> "폭염 주의보! 야외 활동을 줄이고 충분한 수분을 섭취하세요 🌡️"
            temp >= 28 -> "더운 날씨예요! 자외선 차단제를 꼭 바르세요 ☀️"
            temp <= -10 -> "한파 주의보! 외출 시 체온 유지에 특별히 신경 써요 🥶"
            temp <= 0 -> "영하의 날씨예요. 목도리와 장갑을 꼭 챙기세요 ❄️"
            else -> ""
        }
    }
}
