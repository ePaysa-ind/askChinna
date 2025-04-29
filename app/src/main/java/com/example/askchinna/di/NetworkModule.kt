/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.di

import com.example.askchinna.data.remote.ApiKeyProvider
import com.example.askchinna.data.remote.NetworkExceptionHandler
import com.example.askchinna.util.Constants
import com.example.askchinna.util.NetworkStateMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.CacheControl

/**
 * Hilt module that provides networking-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CACHE_SIZE = 10 * 1024 * 1024L // 10 MB cache
    private const val CACHE_MAX_AGE = 1 // 1 day for cached responses
    private const val CACHE_MAX_STALE = 7 // 7 days for offline mode

    /**
     * Provides the OkHttp cache for efficient network requests
     */
    @Singleton
    @Provides
    fun provideOkHttpCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http-cache")
        return Cache(cacheDir, CACHE_SIZE)
    }

    /**
     * Provides the API key interceptor for authenticating requests
     */
    @Singleton
    @Provides
    @Named("apiKeyInterceptor")
    fun provideApiKeyInterceptor(apiKeyProvider: ApiKeyProvider): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val originalUrl = original.url

            // Add API key as query parameter
            val url = originalUrl.newBuilder()
                .addQueryParameter("key", apiKeyProvider.getApiKey())
                .build()

            val requestBuilder = original.newBuilder().url(url)
            chain.proceed(requestBuilder.build())
        }
    }

    /**
     * Provides offline cache interceptor for supporting offline functionality
     */
    @Singleton
    @Provides
    @Named("offlineCacheInterceptor")
    fun provideOfflineCacheInterceptor(networkStateMonitor: NetworkStateMonitor): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()

            if (!networkStateMonitor.isNetworkAvailable()) {
                // If offline, get from cache and set max-stale to tolerate older responses
                val cacheControl = CacheControl.Builder()
                    .maxStale(CACHE_MAX_STALE, TimeUnit.DAYS)
                    .build()

                request = request.newBuilder()
                    .cacheControl(cacheControl)
                    .build()
            }

            chain.proceed(request)
        }
    }

    /**
     * Provides cache interceptor for efficient network usage
     */
    @Singleton
    @Provides
    @Named("cacheInterceptor")
    fun provideCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            val response = chain.proceed(chain.request())

            // Cache for a day by default
            val cacheControl = CacheControl.Builder()
                .maxAge(CACHE_MAX_AGE, TimeUnit.DAYS)
                .build()

            response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        }
    }

    /**
     * Provides logging interceptor for debugging network requests
     */
    @Singleton
    @Provides
    @Named("loggingInterceptor")
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (Constants.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * Provides OkHttpClient configured for the application needs
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
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(offlineCacheInterceptor)
            .addNetworkInterceptor(cacheInterceptor)
            .addInterceptor(loggingInterceptor)
            // Add timeout settings for low-connectivity rural areas
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // Add retry mechanism for unstable connections
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Provides Retrofit client for the Gemini API
     */
    @Singleton
    @Provides
    @Named("geminiRetrofit")
    fun provideGeminiRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.GEMINI_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides Retrofit client for potential future APIs
     */
    @Singleton
    @Provides
    @Named("backupApiRetrofit")
    fun provideBackupApiRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BACKUP_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}