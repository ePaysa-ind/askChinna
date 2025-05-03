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
import com.example.askchinna.util.Constants
import com.example.askchinna.util.NetworkExceptionHandler
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

    private const val CACHE_SIZE = 10L * 1024 * 1024        // 10 MB
    private const val CACHE_MAX_AGE = 1                    // in days
    private const val CACHE_MAX_STALE = 7                  // in days

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
        val key = runBlocking(Dispatchers.IO) { apiKeyProvider.getGeminiApiKey() }
        val original = chain.request()
        val newUrl = original.url.newBuilder()
            .addQueryParameter("key", key)
            .build()
        val newRequest = original.newBuilder().url(newUrl).build()
        chain.proceed(newRequest)
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
     * HTTP logging interceptor for debugging network calls.
     */
    @Singleton
    @Provides
    @Named("loggingInterceptor")
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (Constants.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
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
        @Named("loggingInterceptor") loggingInterceptor: HttpLoggingInterceptor,
        networkExceptionHandler: NetworkExceptionHandler
    ): OkHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(offlineCacheInterceptor)
        .addNetworkInterceptor(cacheInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
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
        .baseUrl(Constants.GEMINI_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
