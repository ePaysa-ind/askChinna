/**
 * File: app/src/main/java/com/example/askchinna/di/AppModule.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 16, 2025
 * Version: 1.6
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
import com.example.askchinna.util.SimpleCoroutineUtils
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

/**
 * Qualifier for IO dispatcher used for background operations.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Qualifier for Main dispatcher used for UI operations.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

/**
 * Hilt module providing application-level dependencies.
 * All dependencies are scoped to SingletonComponent to ensure single instance throughout app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the Room database instance.
     * Uses destructive migration for development, should be replaced with proper migration strategy for production.
     *
     * @param ctx Application context
     * @return Configured AppDatabase instance
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "askchinna-db")
            .fallbackToDestructiveMigration()
            .build()

    /**
     * Provides SharedPreferencesManager for secure local storage.
     *
     * @param ctx Application context
     * @return Configured SharedPreferencesManager instance
     */
    @Provides
    @Singleton
    fun provideSharedPreferencesManager(@ApplicationContext ctx: Context): SharedPreferencesManager =
        SharedPreferencesManager(ctx).apply { initialize() }

    /**
     * Provides NetworkExceptionHandler for handling network-related errors.
     *
     * @return Configured NetworkExceptionHandler instance
     */
    @Provides
    @Singleton
    fun provideNetworkExceptionHandler(): NetworkExceptionHandler =
        NetworkExceptionHandler()

    /**
     * Provides NetworkStateMonitor for tracking network connectivity.
     *
     * @param ctx Application context
     * @return Configured NetworkStateMonitor instance
     */
    @Provides
    @Singleton
    fun provideNetworkStateMonitor(@ApplicationContext ctx: Context): NetworkStateMonitor =
        NetworkStateMonitor(ctx)

    /**
     * Provides SessionManager for handling user sessions.
     *
     * @param ctx Application context
     * @param prefs SharedPreferencesManager instance
     * @return Configured SessionManager instance
     */
    @Provides
    @Singleton
    fun provideSessionManager(
        prefs: SharedPreferencesManager
    ) = SessionManager(prefs)

    /**
     * Provides DateTimeUtils for date/time operations.
     *
     * @return DateTimeUtils object
     */
    @Provides
    @Singleton
    fun provideDateTimeUtils(): DateTimeUtils = DateTimeUtils

    /**
     * Provides ImageHelper for image processing operations.
     *
     * @param ctx Application context
     * @return Configured ImageHelper instance
     */
    @Provides
    @Singleton
    fun provideImageHelper(@ApplicationContext ctx: Context): ImageHelper =
        ImageHelper(ctx)

    /**
     * Provides PdfGenerator for PDF export functionality.
     *
     * @param ctx Application context
     * @param dateTimeUtils DateTimeUtils instance
     * @return Configured PdfGenerator instance
     */
    @Provides
    @Singleton
    fun providePdfGenerator(
        @ApplicationContext ctx: Context,
        dateTimeUtils: DateTimeUtils
    ): PdfGenerator = PdfGenerator(ctx, dateTimeUtils)

    /**
     * Provides FirebaseAuth instance.
     *
     * @return FirebaseAuth instance
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Provides FirebaseFirestore instance.
     *
     * @return FirebaseFirestore instance
     */
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Provides FirebaseStorage instance.
     *
     * @return FirebaseStorage instance
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    /**
     * Provides FirebaseAuthManager for authentication operations.
     *
     * @param auth FirebaseAuth instance
     * @param prefs SharedPreferencesManager instance
     * @param handler NetworkExceptionHandler instance
     * @return Configured FirebaseAuthManager instance
     */
    @Provides
    @Singleton
    fun provideFirebaseAuthManager(
        auth: FirebaseAuth,
        prefs: SharedPreferencesManager,
        handler: NetworkExceptionHandler
    ): FirebaseAuthManager = FirebaseAuthManager(auth, prefs, handler)

    /**
     * Provides FirestoreManager for Firestore operations.
     *
     * @param firestore FirebaseFirestore instance
     * @param prefs SharedPreferencesManager instance
     * @param handler NetworkExceptionHandler instance
     * @return Configured FirestoreManager instance
     */
    @Provides
    @Singleton
    fun provideFirestoreManager(
        firestore: FirebaseFirestore,
        prefs: SharedPreferencesManager,
        handler: NetworkExceptionHandler
    ): FirestoreManager = FirestoreManager(firestore, prefs, handler)

    /**
     * Provides ApiKeyProvider for managing API keys.
     *
     * @param ctx Application context
     * @return Configured ApiKeyProvider instance
     */
    @Provides
    @Singleton
    fun provideApiKeyProvider(@ApplicationContext ctx: Context): ApiKeyProvider =
        ApiKeyProvider(ctx)

    /**
     * Provides GeminiService for AI operations.
     *
     * @param apiKeyProvider ApiKeyProvider instance
     * @param handler NetworkExceptionHandler instance
     * @return Configured GeminiService instance
     */
    @Provides
    @Singleton
    fun provideGeminiService(
        apiKeyProvider: ApiKeyProvider,
        handler: NetworkExceptionHandler
    ): GeminiService = GeminiService(apiKeyProvider, handler)

    /**
     * Provides IO dispatcher for background operations.
     *
     * @return CoroutineDispatcher for IO operations
     */
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Provides Main dispatcher for UI operations.
     *
     * @return CoroutineDispatcher for UI operations
     */
    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    /**
     * Provides Gson instance for JSON operations.
     *
     * @return Configured Gson instance
     */
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    /**
     * Provides SimpleCoroutineUtils for coroutine utilities.
     *
     * @return SimpleCoroutineUtils instance
     */
    @Provides
    @Singleton
    fun provideSimpleCoroutineUtils(): SimpleCoroutineUtils = SimpleCoroutineUtils()

    /**
     * Provides CropRepository for crop data operations.
     *
     * @param ctx Application context
     * @param firestore Firestore instance
     * @return Configured CropRepository instance
     */
    @Provides
    @Singleton
    fun provideCropRepository(
        @ApplicationContext ctx: Context,
        firestore: FirebaseFirestore
    ): CropRepository = CropRepository(ctx, firestore)

    /**
     * Provides UserRepository for user data operations.
     *
     * @param authMgr FirebaseAuthManager instance
     * @param storeMgr FirestoreManager instance
     * @param prefs SharedPreferencesManager instance
     * @param handler NetworkExceptionHandler instance
     * @param utils SimpleCoroutineUtils instance
     * @return Configured UserRepository instance
     */
    @Provides
    @Singleton
    fun provideUserRepository(
        authMgr: FirebaseAuthManager,
        storeMgr: FirestoreManager,
        prefs: SharedPreferencesManager,
        handler: NetworkExceptionHandler,
        utils: SimpleCoroutineUtils
    ): UserRepository = UserRepository(authMgr, storeMgr, prefs, handler, utils)

    /**
     * Provides IdentificationRepository for identification operations.
     *
     * @param gemini GeminiService instance
     * @param imgHelper ImageHelper instance
     * @param sessionMgr SessionManager instance
     * @param ioDispatcher IO dispatcher
     * @param firestore FirebaseFirestore instance
     * @return Configured IdentificationRepository instance
     */
    @Provides
    @Singleton
    fun provideIdentificationRepository(
        gemini: GeminiService,
        imgHelper: ImageHelper,
        sessionMgr: SessionManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        firestore: FirebaseFirestore
    ): IdentificationRepository = IdentificationRepository(
        gemini,
        imgHelper,
        sessionMgr,
        ioDispatcher,
        firestore
    )
}