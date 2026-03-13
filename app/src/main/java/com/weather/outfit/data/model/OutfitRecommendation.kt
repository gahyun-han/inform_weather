package com.weather.outfit.data.model

/**
 * Represents an outfit recommendation for a given weather condition.
 * Contains both default character outfit and user's closet-based recommendation.
 */
data class OutfitRecommendation(
    /** Temperature that triggered this recommendation */
    val temperature: Float,

    /** Weather condition */
    val condition: WeatherConditionType,

    /** Character outfit key - used to select character illustration */
    val characterOutfitKey: String,

    /** Character outfit description (shown to user) */
    val outfitDescription: String,

    /** Warmth level of the recommended outfit (1-5) */
    val warmthLevel: Int,

    /** User's closet items that match this recommendation (may be empty) */
    val suggestedClosetItems: List<ClothingItem> = emptyList(),

    /** Weather tips */
    val weatherTip: String = "",

    /** Whether umbrella is recommended */
    val umbrellaRecommended: Boolean = false,

    /** Whether sunscreen is recommended */
    val sunscreenRecommended: Boolean = false
) {
    companion object {
        /**
         * Determines character outfit key based on temperature and condition.
         * Keys correspond to drawable resource names.
         */
        fun getOutfitKey(temp: Float, condition: WeatherConditionType): String {
            return when {
                temp >= 28 -> "outfit_hot"        // 28°C+ : 민소매/반팔+반바지
                temp >= 23 -> "outfit_warm"        // 23-27°C: 반팔+면바지
                temp >= 20 -> "outfit_mild_warm"   // 20-22°C: 얇은 긴팔+면바지
                temp >= 17 -> "outfit_mild"        // 17-19°C: 긴팔+얇은 카디건
                temp >= 12 -> "outfit_cool"        // 12-16°C: 자켓+긴바지
                temp >= 9  -> "outfit_chilly"      // 9-11°C: 트렌치코트/야상
                temp >= 5  -> "outfit_cold"        // 5-8°C: 울코트+니트
                temp >= 0  -> "outfit_very_cold"   // 0-4°C: 두꺼운 패딩
                else       -> "outfit_freezing"    // -1°C 이하: 롱패딩+목도리+장갑
            }.let { key ->
                // Add weather modifier
                when (condition) {
                    WeatherConditionType.LIGHT_RAIN,
                    WeatherConditionType.HEAVY_RAIN -> "${key}_rain"
                    WeatherConditionType.SNOW -> "${key}_snow"
                    else -> key
                }
            }
        }

        fun getOutfitDescription(temp: Float, condition: WeatherConditionType): String {
            val baseDesc = when {
                temp >= 28 -> "민소매나 반팔, 반바지를 입어요"
                temp >= 23 -> "반팔과 얇은 면바지가 좋아요"
                temp >= 20 -> "얇은 긴팔이나 가디건을 걸치세요"
                temp >= 17 -> "긴팔에 얇은 카디건을 준비하세요"
                temp >= 12 -> "자켓이나 청자켓을 입어보세요"
                temp >= 9  -> "트렌치코트나 야상이 딱이에요"
                temp >= 5  -> "두꺼운 코트에 니트 레이어링!"
                temp >= 0  -> "두꺼운 패딩이 필요해요"
                else       -> "롱패딩에 목도리, 장갑까지 완비!"
            }
            val weatherDesc = when (condition) {
                WeatherConditionType.LIGHT_RAIN -> "\n우산을 챙기세요 ☂️"
                WeatherConditionType.HEAVY_RAIN -> "\n우산 필수! 방수 신발도 추천해요 ☔"
                WeatherConditionType.SNOW -> "\n미끄럼에 주의하고 방한에 신경 써요 ❄️"
                WeatherConditionType.THUNDERSTORM -> "\n실내에 머무르세요. 우산 챙기기! ⛈️"
                WeatherConditionType.WINDY -> "\n바람막이를 챙기면 좋아요 💨"
                WeatherConditionType.FOGGY -> "\n시야가 좋지 않으니 조심하세요 🌫️"
                else -> ""
            }
            return baseDesc + weatherDesc
        }
    }
}
