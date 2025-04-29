/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Version: 1.0
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
import com.example.askchinna.data.remote.NetworkExceptionHandler
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.util.DateTimeUtils
import com.example.askchinna.util.ImageHelper
import com.example.askchinna.util.NetworkStateMonitor
import com.example.askchinna.util.PdfGenerator
import com.example.askchinna.util.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Hilt module that provides application-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the application context
     */
    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    /**
     * Provides the Room database instance
     */
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "askchinna_database"
        ).fallbackToDestructiveMigration() // Only for MVP phase, to be removed in production
            .build()
    }

    /**
     * Provides the SharedPreferencesManager for secure local storage
     */
    @Singleton
    @Provides
    fun provideSharedPreferencesManager(@ApplicationContext context: Context): SharedPreferencesManager {
        return SharedPreferencesManager(context)
    }

    /**
     * Provides Firebase Authentication instance
     */
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    /**
     * Provides Firebase Firestore instance
     */
    @Singleton
    @Provides
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    /**
     * Provides Firebase Storage instance
     */
    @Singleton
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    /**
     * Provides Firebase Authentication manager
     */
    @Singleton
    @Provides
    fun provideFirebaseAuthManager(
        firebaseAuth: FirebaseAuth,
        sharedPreferencesManager: SharedPreferencesManager
    ): FirebaseAuthManager {
        return FirebaseAuthManager(firebaseAuth, sharedPreferencesManager)
    }

    /**
     * Provides Firestore manager for database operations
     */
    @Singleton
    @Provides
    fun provideFirestoreManager(
        firestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage
    ): FirestoreManager {
        return FirestoreManager(firestore, firebaseStorage)
    }

    /**
     * Provides the API key provider for secure API key storage and retrieval
     */
    @Singleton
    @Provides
    fun provideApiKeyProvider(
        sharedPreferencesManager: SharedPreferencesManager,
        firestoreManager: FirestoreManager
    ): ApiKeyProvider {
        return ApiKeyProvider(sharedPreferencesManager, firestoreManager)
    }

    /**
     * Provides the Gemini AI service
     */
    @Singleton
    @Provides
    fun provideGeminiService(
        apiKeyProvider: ApiKeyProvider,
        networkExceptionHandler: NetworkExceptionHandler
    ): GeminiService {
        return GeminiService(apiKeyProvider, networkExceptionHandler)
    }

    /**
     * Provides the network exception handler for consistent error handling
     */
    @Singleton
    @Provides
    fun provideNetworkExceptionHandler(): NetworkExceptionHandler {
        return NetworkExceptionHandler()
    }

    /**
     * Provides network state monitor to track connectivity
     */
    @Singleton
    @Provides
    fun provideNetworkStateMonitor(@ApplicationContext context: Context): NetworkStateMonitor {
        return NetworkStateMonitor(context)
    }

    /**
     * Provides session manager for session timing
     */
    @Singleton
    @Provides
    fun provideSessionManager(
        sharedPreferencesManager: SharedPreferencesManager,
        dateTimeUtils: DateTimeUtils
    ): SessionManager {
        return SessionManager(sharedPreferencesManager, dateTimeUtils)
    }

    /**
     * Provides date/time utility functions
     */
    @Singleton
    @Provides
    fun provideDateTimeUtils(): DateTimeUtils {
        return DateTimeUtils()
    }

    /**
     * Provides image helper for image processing
     */
    @Singleton
    @Provides
    fun provideImageHelper(@ApplicationContext context: Context): ImageHelper {
        return ImageHelper(context)
    }

    /**
     * Provides PDF generator for exporting results
     */
    @Singleton
    @Provides
    fun providePdfGenerator(@ApplicationContext context: Context): PdfGenerator {
        return PdfGenerator(context)
    }

    /**
     * Provides IO dispatcher for coroutines
     */
    @Provides
    fun provideIODispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    /**
     * Provides main thread dispatcher for coroutines
     */
    @Provides
    fun provideMainDispatcher(): CoroutineDispatcher {
        return Dispatchers.Main
    }

    /**
     * Provides Crop Repository
     */
    @Singleton
    @Provides
    fun provideCropRepository(
        appDatabase: AppDatabase,
        firestoreManager: FirestoreManager,
        @ApplicationContext context: Context,
        ioDispatcher: CoroutineDispatcher
    ): CropRepository {
        return CropRepository(appDatabase, firestoreManager, context, ioDispatcher)
    }

    /**
     * Provides User Repository
     */
    @Singleton
    @Provides
    fun provideUserRepository(
        sharedPreferencesManager: SharedPreferencesManager,
        firebaseAuthManager: FirebaseAuthManager,
        firestoreManager: FirestoreManager,
        ioDispatcher: CoroutineDispatcher
    ): UserRepository {
        return UserRepository(
            sharedPreferencesManager,
            firebaseAuthManager,
            firestoreManager,
            ioDispatcher
        )
    }

    /**
     * Provides Identification Repository
     */
    @Singleton
    @Provides
    fun provideIdentificationRepository(
        appDatabase: AppDatabase,
        firestoreManager: FirestoreManager,
        geminiService: GeminiService,
        cropRepository: CropRepository,
        imageHelper: ImageHelper,
        sharedPreferencesManager: SharedPreferencesManager,
        ioDispatcher: CoroutineDispatcher
    ): IdentificationRepository {
        return IdentificationRepository(
            appDatabase,
            firestoreManager,
            geminiService,
            cropRepository,
            imageHelper,
            sharedPreferencesManager,
            ioDispatcher
        )
    }
}