/*
 * file path: app/src/main/java/com/example/askchinna/di/NetworkModule.kt
 * file name: NetworkModule.kt
 * created by Chinna on 2023-10-01
 * version 1.0
 * This file is part of AskChinna.
 * Copyright © 2023 askChinna
 */

package com.example.askchinna.di

import android.content.Context
import com.example.askchinna.data.remote.ApiKeyProvider
import com.example.askchinna.util.NetworkStateMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module that provides networking-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CACHE_SIZE = 20L * 1024 * 1024       // 20 MB for better performance
    private const val CACHE_MAX_AGE = 1                    // 1 day for MVP
    private const val CACHE_MAX_STALE = 3                  // 3 days for MVP
    private const val MAX_RETRIES = 3                      // Maximum retry attempts
    private const val RETRY_DELAY_MS = 1000L              // Initial delay between retries
    private const val RETRY_MULTIPLIER = 2.0f             // Exponential backoff multiplier
    private const val MAX_RETRY_DELAY_MS = 15000L         // Maximum delay between retries

    /**
     * Provides OkHttp cache instance.
     */
    @Singleton
    @Provides
    fun provideOkHttpCache(
        @ApplicationContext context: Context
    ): Cache = Cache(File(context.cacheDir, "http-cache"), CACHE_SIZE)

    /**
     * Interceptor adding the Gemini API key to every request.
     * Uses runBlocking to call the suspend function synchronously.
     */
    @Singleton
    @Provides
    @Named("apiKeyInterceptor")
    fun provideApiKeyInterceptor(
        apiKeyProvider: ApiKeyProvider
    ): Interceptor = Interceptor { chain ->
        try {
            val key = runBlocking(Dispatchers.IO) {
                apiKeyProvider.getGeminiApiKey()
            }
            if (key.isBlank()) {
                throw SecurityException("API key not found")
            }
            val original = chain.request()
            val newUrl = original.url.newBuilder()
                .addQueryParameter("key", key)
                .build()
            val newRequest = original.newBuilder().url(newUrl).build()
            chain.proceed(newRequest)
        } catch (e: Exception) {
            throw SecurityException("Failed to add API key: ${e.message}", e)
        }
    }

    /**
     * Interceptor serving cached responses when offline.
     */
    @Singleton
    @Provides
    @Named("offlineCacheInterceptor")
    fun provideOfflineCacheInterceptor(
        networkStateMonitor: NetworkStateMonitor
    ): Interceptor = Interceptor { chain ->
        var request = chain.request()
        if (!networkStateMonitor.isNetworkAvailable()) {
            val cacheControl = CacheControl.Builder()
                .maxStale(CACHE_MAX_STALE, TimeUnit.DAYS)
                .build()
            request = request.newBuilder()
                .cacheControl(cacheControl)
                .build()
        }
        chain.proceed(request)
    }

    /**
     * Interceptor adding default cache-control headers.
     */
    @Singleton
    @Provides
    @Named("cacheInterceptor")
    fun provideCacheInterceptor(): Interceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val cacheControl = CacheControl.Builder()
            .maxAge(CACHE_MAX_AGE, TimeUnit.DAYS)
            .build()
        response.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .build()
    }

    /**
     * Interceptor for retrying failed requests with exponential backoff.
     */
    @Singleton
    @Provides
    @Named("retryInterceptor")
    fun provideRetryInterceptor(): Interceptor = Interceptor { chain ->
        var retryCount = 0
        var currentDelay = RETRY_DELAY_MS
        var response = chain.proceed(chain.request())

        while (!response.isSuccessful && retryCount < MAX_RETRIES) {
            val isRetryable = when (response.code) {
                408, 429, 500, 502, 503, 504 -> true // Timeout, rate limit, server errors
                else -> false
            }
            
            if (!isRetryable) break
            
            response.close()
            retryCount++
            
            // Exponential backoff with jitter
            val delayWithJitter = currentDelay + (0..1000).random()
            Thread.sleep(minOf(delayWithJitter, MAX_RETRY_DELAY_MS))
            
            // Update delay for next retry
            currentDelay = (currentDelay * RETRY_MULTIPLIER).toLong()
            
            response = chain.proceed(chain.request())
        }

        response
    }

    /**
     * HTTP logging interceptor for debugging network calls.
     */
    @Singleton
    @Provides
    @Named("loggingInterceptor")
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY  // Set to BODY for development
            // In production, you would use: HttpLoggingInterceptor.Level.NONE
        }

    /**
     * OkHttpClient configured with cache, interceptors, and timeouts.
     */
    @Singleton
    @Provides
    fun provideOkHttpClient(
        cache: Cache,
        @Named("apiKeyInterceptor") apiKeyInterceptor: Interceptor,
        @Named("offlineCacheInterceptor") offlineCacheInterceptor: Interceptor,
        @Named("cacheInterceptor") cacheInterceptor: Interceptor,
        @Named("retryInterceptor") retryInterceptor: Interceptor,
        @Named("loggingInterceptor") loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(offlineCacheInterceptor)
        .addNetworkInterceptor(cacheInterceptor)
        .addInterceptor(retryInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)  // Reduced for MVP
        .readTimeout(15, TimeUnit.SECONDS)     // Reduced for MVP
        .writeTimeout(15, TimeUnit.SECONDS)    // Reduced for MVP
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Retrofit client for the Gemini API.
     */
    @Singleton
    @Provides
    @Named("geminiRetrofit")
    fun provideGeminiRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")  // Direct URL instead of constant
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}