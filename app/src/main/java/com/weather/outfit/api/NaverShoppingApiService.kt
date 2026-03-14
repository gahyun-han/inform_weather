package com.weather.outfit.api

import android.text.Html
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class NaverShoppingResponse(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("start") val start: Int = 1,
    @SerializedName("display") val display: Int = 20,
    @SerializedName("items") val items: List<NaverShoppingItem> = emptyList()
)

data class NaverShoppingItem(
    @SerializedName("title") val title: String = "",
    @SerializedName("link") val link: String = "",
    @SerializedName("image") val image: String = "",
    @SerializedName("lprice") val lprice: String = "",
    @SerializedName("mallName") val mallName: String = "",
    @SerializedName("brand") val brand: String = "",
    @SerializedName("category1") val category1: String = "",
    @SerializedName("category2") val category2: String = "",
    @SerializedName("productId") val productId: String = ""
) {
    val cleanTitle: String
        get() = Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY).toString()

    val priceFormatted: String
        get() = if (lprice.isNotEmpty()) "%,d원".format(lprice.toLongOrNull() ?: 0L) else "가격 미정"
}

interface NaverShoppingApiService {
    @GET("v1/search/shop.json")
    suspend fun searchShop(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int = 20,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "sim"
    ): NaverShoppingResponse
}

object NaverShoppingApi {
    // Set NAVER_CLIENT_ID and NAVER_CLIENT_SECRET in local.properties
    // Get API keys at: https://developers.naver.com/apps/#/register
    val CLIENT_ID: String get() = com.weather.outfit.BuildConfig.NAVER_CLIENT_ID
    val CLIENT_SECRET: String get() = com.weather.outfit.BuildConfig.NAVER_CLIENT_SECRET

    private const val BASE_URL = "https://openapi.naver.com/"

    val service: NaverShoppingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NaverShoppingApiService::class.java)
    }
}

/**
 * Maps outfit key + gender to Naver Shopping search query for main screen reference images.
 */
fun getNaverOutfitSearchQuery(outfitKey: String, gender: String = "female"): String {
    val g = if (gender == "female") "여성" else "남성"
    return when {
        outfitKey.contains("freezing") -> "$g 롱패딩"
        outfitKey.contains("very_cold") -> "$g 두꺼운 패딩"
        outfitKey.contains("cold") -> "$g 울코트 니트"
        outfitKey.contains("chilly") -> "$g 트렌치코트"
        outfitKey.contains("cool") -> "$g 자켓"
        outfitKey.contains("mild_warm") -> "$g 가디건 긴팔"
        outfitKey.contains("mild") -> "$g 봄 가디건"
        outfitKey.contains("warm") -> "$g 반팔 여름"
        outfitKey.contains("hot") -> "$g 민소매 반바지"
        outfitKey.contains("rain") -> "$g 레인코트"
        outfitKey.contains("snow") -> "$g 패딩 방한"
        else -> "$g 패션"
    }
}

/**
 * Maps gender + ClothingCategory key to Naver Shopping search query for catalog browsing.
 */
fun getNaverCatalogQuery(gender: String, categoryKey: String): String {
    val g = if (gender == "female") "여성" else "남성"
    return when (categoryKey) {
        "TOP" -> "$g 상의"
        "BOTTOM" -> "$g 하의"
        "OUTER" -> "$g 아우터"
        "SHOES" -> "$g 신발"
        "ACCESSORY" -> "$g 액세서리"
        "DRESS" -> if (gender == "female") "여성 원피스" else "남성 점프수트"
        "UNDERWEAR" -> "$g 이너웨어"
        else -> "$g 패션"
    }
}
