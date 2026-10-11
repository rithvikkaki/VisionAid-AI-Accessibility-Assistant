package com.visionaid.assistant.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Permanent cloud deployment - works from anywhere, no laptop needed!
    private var _baseUrl = "https://visionaid-api.onrender.com/"
    val BASE_URL: String get() = _baseUrl

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var _instance: VisionAidApi? = null

    val instance: VisionAidApi
        get() {
            if (_instance == null) {
                _instance = createInstance(_baseUrl)
            }
            return _instance!!
        }

    fun updateBaseUrl(newUrl: String) {
        val formattedUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        if (_baseUrl != formattedUrl) {
            _baseUrl = formattedUrl
            _instance = createInstance(formattedUrl)
        }
    }

    private fun createInstance(url: String): VisionAidApi {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VisionAidApi::class.java)
    }
}
