package com.example.ui

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<Content>
) {
    @JsonClass(generateAdapter = true)
    data class Content(
        @Json(name = "parts") val parts: List<Part>
    )

    @JsonClass(generateAdapter = true)
    data class Part(
        @Json(name = "text") val text: String
    )
}

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
) {
    @JsonClass(generateAdapter = true)
    data class Candidate(
        @Json(name = "content") val content: Content?
    )

    @JsonClass(generateAdapter = true)
    data class Content(
        @Json(name = "parts") val parts: List<Part>?
    )

    @JsonClass(generateAdapter = true)
    data class Part(
        @Json(name = "text") val text: String?
    )
}

interface GeminiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiService = retrofit.create(GeminiService::class.java)

    suspend fun askGemini(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Błąd: Brak skonfigurowanego klucza Gemini API. Dodaj poprawny klucz w pliku .env"
        }
        val request = GeminiRequest(
            contents = listOf(
                GeminiRequest.Content(
                    parts = listOf(
                        GeminiRequest.Part(text = prompt)
                    )
                )
            )
        )
        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Nie otrzymano odpowiedzi od modelu AI."
        } catch (e: Exception) {
            "Wystąpił błąd podczas komunikacji z Gemini AI: ${e.localizedMessage ?: e.message}"
        }
    }
}
