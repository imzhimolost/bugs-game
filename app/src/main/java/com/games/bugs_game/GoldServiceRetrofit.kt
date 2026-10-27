package com.games.bugs_game

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

interface CbrApi {
    @GET("scripts/xml_metall.asp")
    suspend fun getMetalsXml(): Response<ResponseBody>
}

object GoldServiceRetrofit {
    private const val BASE_URL = "https://www.cbr.ru/"
    var cachedGoldPrice: Float = 8500f

    private val api: CbrApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
            .create(CbrApi::class.java)
    }

    suspend fun fetchGoldPrice(): Float {
        return try {
            val response = api.getMetalsXml()
            if (response.isSuccessful) {
                val xml = response.body()?.string() ?: ""
                val regex = Regex("""<Record[^>]*Code="1"[^>]*>[\s\S]*?<Buy>([\d\s,]+)</Buy>""")
                val match = regex.find(xml)
                if (match != null) {
                    val rawPrice = match.groupValues[1]
                        .replace(" ", "")
                        .replace(",", ".")
                    val price = rawPrice.toFloatOrNull() ?: 8500f
                    cachedGoldPrice = price
                    price
                } else cachedGoldPrice
            } else cachedGoldPrice
        } catch (e: Exception) {
            e.printStackTrace()
            cachedGoldPrice
        }
    }
}