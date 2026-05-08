package com.example.gamefiedsarvya.network

import com.example.gamefiedsarvya.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Groq API client.
 *
 * Security:
 * - API key is read from BuildConfig, which is populated from local.properties at build time.
 * - local.properties is gitignored — the key never enters source control.
 * - The key is only sent over HTTPS to api.groq.com.
 * - Logging interceptor is DEBUG-only; release builds send no logs.
 */
object GroqClient {

    private const val BASE_URL = "https://api.groq.com/"

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()
    }

    val api: GroqApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApi::class.java)
    }

    /** Returns "Bearer <key>" header value. Empty string if key not configured. */
    val authHeader: String
        get() {
            val key = BuildConfig.GROQ_API_KEY
            return if (key.isNotBlank()) "Bearer $key" else ""
        }

    val isConfigured: Boolean
        get() = BuildConfig.GROQ_API_KEY.isNotBlank()
}
