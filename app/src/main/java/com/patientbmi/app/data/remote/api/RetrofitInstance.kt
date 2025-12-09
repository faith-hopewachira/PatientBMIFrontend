package com.patientbmi.app.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Assessment Api configures and provides a shared Retrofit instance with
 * proper HTTP client settings, timeouts, and logging for API communication.
 * Configuration Highlights:
 * - Base URL: Points to local Django development server
 * - Timeouts: 30-second timeouts for all network operations
 * - Logging: HTTP request/response logging enabled for debugging
 * - GSON: JSON serialization/deserialization via GsonConverterFactory
 *
 */
object RetrofitInstance {
    val BASE_URL = "http://192.168.88.21:8000/api/"

    /**
     * HTTP logging interceptor for debugging network requests and responses.
     * Logs full HTTP request/response bodies in debug builds.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Configured OkHttpClient with interceptors and timeout settings.
     * Features:
     * - 30-second timeouts for connect, read, and write operations
     * - HTTP logging interceptor for debugging
     * - Automatic retry on connection failures
     */
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Created only when first accessed to improve application startup time.
     * Configured with:
     * - Base API URL
     * - Custom OkHttpClient
     * - GSON converter for JSON serialization
     */
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides access to all API endpoints defined in AssessmentApi interface.
     */
    val api: AssessmentApi by lazy {
        retrofit.create(AssessmentApi::class.java)
    }

    /**
     * @param newBaseUrl The new base URL to use for API calls
     * @return A new AssessmentApi instance with updated base URL
     */
    fun updateBaseUrl(newBaseUrl: String): AssessmentApi {
        val newRetrofit = Retrofit.Builder()
            .baseUrl(newBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return newRetrofit.create(AssessmentApi::class.java)
    }
}
