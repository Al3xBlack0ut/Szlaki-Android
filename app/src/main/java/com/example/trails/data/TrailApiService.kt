package com.example.trails.data

import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://local.api/"

interface TrailApiService {
    @GET("trails")
    suspend fun getTrails(): List<Trail>
}

/**
 * MockInterceptor symuluje odpowiedź z serwera (API lokalne).
 */
class MockInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Zwracamy pustą listę, aby nie dublować tras domyślnych
        val jsonResponse = "[]"

        return Response.Builder()
            .code(200)
            .message("OK")
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .body(jsonResponse.toResponseBody("application/json".toMediaType()))
            .addHeader("content-type", "application/json")
            .build()
    }
}

object TrailApi {
    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(MockInterceptor())
        .build()
    
    val retrofitService: TrailApiService by lazy {
        Retrofit.Builder()
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(BASE_URL)
            .build()
            .create(TrailApiService::class.java)
    }
}
