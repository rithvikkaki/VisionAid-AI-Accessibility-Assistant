package com.visionaid.assistant.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface VisionAidApi {
    @GET("health")
    suspend fun checkHealth(): HealthResponse

    @Multipart
    @POST("upload-image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("lang") lang: RequestBody,
        @Part("reading_level") readingLevel: RequestBody
    ): VisionResponse

    @POST("process-text")
    suspend fun processText(@Body request: TextRequest): VisionResponse

    @POST("translate")
    suspend fun translate(@Body request: TranslateRequest): TranslateResponse
}

data class HealthResponse(val status: String, val message: String)
data class VisionResponse(
    val filename: String?,
    val extracted_text: String?,
    val ai_summary: String?,
    val response: String?, // Alias for AI answer
    val text: String?,     // Alias for AI answer
    val audio_file: String?,
    val audio_path: String?, // Alias for audio URL
    val audio_url: String?,  // Full URL
    val warning: String?,
    val error: String?
)
data class TextRequest(val text: String, val lang: String, val reading_level: String)
data class TranslateRequest(val text: String, val lang: String)
data class TranslateResponse(
    val translated_text: String,
    val audio_file: String?,
    val audio_path: String?,
    val audio_url: String?
)
