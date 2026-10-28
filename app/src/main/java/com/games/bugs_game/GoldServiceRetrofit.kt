package com.games.bugs_game

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface CbrApi {
    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    @GET("scripts/xml_metall.asp")
    suspend fun getMetalsXml(
        @Query("date_req1") dateFrom: String,
        @Query("date_req2") dateTo: String
    ): Response<ResponseBody>
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
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateTo = sdf.format(Date())
            val sevenDaysAgo = Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)
            val dateFrom = sdf.format(sevenDaysAgo)

            Log.d("CBR_API", "Запрос котировок с $dateFrom по $dateTo...")

            val response = api.getMetalsXml(dateFrom, dateTo)
            if (response.isSuccessful) {
                val xml = response.body()?.string() ?: ""
                Log.d("CBR_API", "Ответ получен! Длина XML: ${xml.length} символов")

                val regex = Regex("""<Record[^>]*Code="1"[^>]*>[\s\S]*?<Buy>([\d\s,]+)</Buy>""")
                val allMatches = regex.findAll(xml).toList()

                if (allMatches.isNotEmpty()) {
                    val latestMatch = allMatches.last()
                    val rawPrice = latestMatch.groupValues[1]
                        .replace(" ", "")
                        .replace("\u00A0", "")
                        .replace(",", ".")

                    val price = rawPrice.toFloatOrNull() ?: cachedGoldPrice
                    cachedGoldPrice = price
                    Log.d("CBR_API", "УСПЕХ! Актуальный курс золота ЦБ: $price ₽")
                    price
                } else {
                    Log.w("CBR_API", "Записи золота не найдены в XML, используем кэш")
                    cachedGoldPrice
                }
            } else {
                Log.e("CBR_API", "Ошибка сервера: код ${response.code()}")
                cachedGoldPrice
            }
        } catch (e: Exception) {
            Log.e("CBR_API", "Ошибка загрузки курса: ${e.message}")
            e.printStackTrace()
            cachedGoldPrice
        }
    }
}