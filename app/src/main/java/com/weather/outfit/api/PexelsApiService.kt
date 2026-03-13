package com.weather.outfit.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// Data classes
data class PexelsSearchResponse(
    @SerializedName("photos") val photos: List<PexelsPhoto> = emptyList()
)

data class PexelsPhoto(
    @SerializedName("id") val id: Long,
    @SerializedName("src") val src: PexelsPhotoSrc,
    @SerializedName("alt") val alt: String = ""
)

data class PexelsPhotoSrc(
    @SerializedName("medium") val medium: String,
    @SerializedName("small") val small: String,
    @SerializedName("portrait") val portrait: String
)

interface PexelsApiService {
    @GET("v1/search")
    suspend fun searchPhotos(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 6,
        @Query("orientation") orientation: String = "portrait"
    ): PexelsSearchResponse
}

object PexelsApi {
    // Get a free API key at: https://www.pexels.com/api/
    // Free tier: 200 req/hour, 20,000 req/month
    const val API_KEY = "YOUR_PEXELS_API_KEY"
    private const val BASE_URL = "https://api.pexels.com/"

    val service: PexelsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PexelsApiService::class.java)
    }
}

/**
 * Maps outfit key to Korean fashion search query for Pexels.
 */
fun getOutfitSearchQuery(outfitKey: String): String = when {
    outfitKey.contains("freezing") -> "롱패딩 패션 겨울 코디"
    outfitKey.contains("very_cold") -> "두꺼운 패딩 겨울 패션"
    outfitKey.contains("cold") -> "울코트 니트 겨울 코디"
    outfitKey.contains("chilly") -> "트렌치코트 야상 가을 패션"
    outfitKey.contains("cool") -> "자켓 청자켓 가을 코디"
    outfitKey.contains("mild_warm") -> "얇은 긴팔 가디건 패션"
    outfitKey.contains("mild") -> "가디건 긴팔 봄 코디"
    outfitKey.contains("warm") -> "반팔 여름 캐주얼 패션"
    outfitKey.contains("hot") -> "민소매 반바지 여름 패션"
    outfitKey.contains("rain") -> "레인코트 우산 비오는날 패션"
    outfitKey.contains("snow") -> "패딩 방한 눈오는날 코디"
    else -> "한국 패션 코디"
}
