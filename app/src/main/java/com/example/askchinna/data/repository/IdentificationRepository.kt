/**
 * File: app/src/main/java/com/example/askchinna/data/repository/IdentificationRepository.kt
 * Copyright © 2025 askChinna
 * Created: April 29, 2025
 * Updated: May 4, 2025
 * Version: 1.4
 * Description: Handles crop disease identification via online (Gemini API)
 * and offline models, plus Firestore caching.
 */
package com.example.askchinna.data.repository

import com.example.askchinna.data.remote.FirestoreManager
import com.example.askchinna.data.remote.GeminiService
import com.example.askchinna.util.DateTimeUtils
import com.example.askchinna.util.ImageHelper
import com.example.askchinna.util.SessionManager
import com.example.askchinna.di.IoDispatcher
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentificationRepository @Inject constructor(
    private val cropRepository: CropRepository,
    private val firestoreManager: FirestoreManager,
    private val geminiService: GeminiService,
    private val imageHelper: ImageHelper,
    private val sessionManager: SessionManager,
    private val dateTimeUtils: DateTimeUtils,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val firestore: FirebaseFirestore
) {
    private val TAG = "IdentificationRepo"
    private val storageRef = FirebaseStorage.getInstance().reference.child("crop_images")

    // … rest of the class unchanged …
}
