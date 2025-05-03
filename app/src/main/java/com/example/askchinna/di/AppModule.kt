/**
 * File: app/src/main/java/com/example/askchinna/di/AppModule.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 4, 2025
 * Version: 1.4
 * Description: Hilt module providing application‑level dependencies,
 * including dispatchers with qualifiers, DateTimeUtils, and unified Room database.
 */

package com.example.askchinna.di

import android.content.Context
import androidx.room.Room
import com.example.askchinna.data.local.AppDatabase
import com.example.askchinna.data.local.SharedPreferencesManager
import com.example.askchinna.data.remote.ApiKeyProvider
import com.example.askchinna.data.remote.FirebaseAuthManager
import com.example.askchinna.data.remote.FirestoreManager
import com.example.askchinna.data.remote.GeminiService
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.util.DateTimeUtils
import com.example.askchinna.util.ImageHelper
import com.example.askchinna.util.NetworkExceptionHandler
import com.example.askchinna.util.NetworkStateMonitor
import com.example.askchinna.util.PdfGenerator
import com.example.askchinna.util.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

// Qualifiers for dispatchers
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** Provides the Room database, using the same name as AppDatabase’s companion */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "askchinna-db")
            .fallbackToDestructiveMigration(false)
            .build()

    /** SharedPreferences manager (plain for now; encryption can be swapped in later) */
    @Provides
    @Singleton
    fun provideSharedPreferencesManager(@ApplicationContext ctx: Context): SharedPreferencesManager =
        SharedPreferencesManager(ctx)

    /** Network exception handler for Firebase and general errors */
    @Provides
    @Singleton
    fun provideNetworkExceptionHandler(): NetworkExceptionHandler =
        NetworkExceptionHandler()

    /** Monitor connectivity state */
    @Provides
    @Singleton
    fun provideNetworkStateMonitor(@ApplicationContext ctx: Context): NetworkStateMonitor =
        NetworkStateMonitor(ctx)

    /** SessionManager orchestrates session timing & auth state */
    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext ctx: Context,
        prefs: SharedPreferencesManager
    ): SessionManager = SessionManager(ctx, prefs)

    /** Utilities for date/time operations (object) */
    @Provides
    @Singleton
    fun provideDateTimeUtils(): DateTimeUtils = DateTimeUtils

    /** Image processing helper */
    @Provides
    @Singleton
    fun provideImageHelper(@ApplicationContext ctx: Context): ImageHelper =
        ImageHelper(ctx)

    /** PDF export utility */
    @Provides
    @Singleton
    fun providePdfGenerator(
            @ApplicationContext ctx: Context,
           dateTimeUtils: DateTimeUtils
       ): PdfGenerator = PdfGenerator(ctx, dateTimeUtils)

    /** FirebaseAuth singleton */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    /** Firestore singleton */
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    /** Firebase Storage singleton */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    /** Auth manager with injected prefs & error handler */
    @Provides
    @Singleton
    fun provideFirebaseAuthManager(
        auth: FirebaseAuth,
        prefs: SharedPreferencesManager,
        handler: NetworkExceptionHandler
    ): FirebaseAuthManager = FirebaseAuthManager(auth, prefs, handler)

    /** Firestore manager aligned to its constructor */
    @Provides
    @Singleton
    fun provideFirestoreManager(
        firestore: FirebaseFirestore,
        prefs: SharedPreferencesManager,
        handler: NetworkExceptionHandler
    ): FirestoreManager = FirestoreManager(firestore, prefs, handler)

    /** Simple API key storage */
    @Provides
    @Singleton
    fun provideApiKeyProvider(@ApplicationContext ctx: Context): ApiKeyProvider =
        ApiKeyProvider(ctx)

    /** Gemini AI service */
    @Provides
    @Singleton
    fun provideGeminiService(
        apiKeyProvider: ApiKeyProvider,
        handler: NetworkExceptionHandler
    ): GeminiService = GeminiService(apiKeyProvider, handler)

    /** IO dispatcher (for background work) */
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /** Main dispatcher (for UI work) */
    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    /** Gson for JSON parsing */
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    /** Crop data repository */
    @Provides
    @Singleton
    fun provideCropRepository(
        @ApplicationContext ctx: Context,
        gson: Gson
    ): CropRepository = CropRepository(ctx, gson)

    /** UserRepository aligned to its constructor */
    @Provides
    @Singleton
    fun provideUserRepository(
        authMgr: FirebaseAuthManager,
        storeMgr: FirestoreManager,
        prefs: SharedPreferencesManager,
        handler: NetworkExceptionHandler,
        utils: com.example.askchinna.util.SimpleCoroutineUtils
    ): UserRepository = UserRepository(authMgr, storeMgr, prefs, handler, utils)

    /** IdentificationRepository must match its 8‑arg constructor */
    @Provides
    @Singleton
    fun provideIdentificationRepository(
        cropRepo: CropRepository,
        storeMgr: FirestoreManager,
        gemini: GeminiService,
        imgHelper: ImageHelper,
        sessionMgr: SessionManager,
        dateTimeUtils: DateTimeUtils,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        firestore: FirebaseFirestore
    ): IdentificationRepository = IdentificationRepository(
        cropRepo,
        storeMgr,
        gemini,
        imgHelper,
        sessionMgr,
        dateTimeUtils,
        ioDispatcher,
        firestore
    )
}
