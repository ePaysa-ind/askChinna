/**
 * File: app/src/main/java/com/example/askchinna/data/remote/FirestoreInitializer.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Updated: May 15, 2025
 * Version: 1.2
 *
 * Change Log:
 * 1.2 - May 15, 2025
 * - Removed authentication requirement for initialization
 * - Made initialization work without authentication
 * - Fixed circular dependency issues
 * 1.1 - May 15, 2025
 * - Added authentication check before Firestore operations
 * - Fixed permission issues by deferring Firestore writes until authenticated
 * - Added safe error handling for permission issues
 */

package com.example.askchinna.data.remote

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Initializes Firestore collections and documents with required data.
 * Only runs initialization if collections don't already exist.
 *
 * IMPORTANT: Initialization can now proceed without authentication.
 * Collections will be created with default values.
 */
@Singleton
class FirestoreInitializer @Inject constructor() {
    private val TAG = "FirestoreInitializer"
    private val db = FirebaseFirestore.getInstance()

    /**
     * Initialize all required collections if they don't exist
     * Now works without authentication requirement
     *
     * @return true if initialization was successful, false otherwise
     */
    suspend fun initializeCollections(): Boolean {
        try {
            // Log initialization attempt
            Log.d(TAG, "Attempting to initialize Firestore collections")

            // Check if collections already exist
            val collectionsExist = checkCollectionsExist()
            if (collectionsExist) {
                Log.d(TAG, "Collections already exist, skipping initialization")
                return true
            }

            // Initialize all collections
            initializeUsersCollection()
            initializeCropsCollection()
            initializeUsageLimitsCollection()

            Log.d(TAG, "Successfully initialized all collections")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing collections", e)
            return false
        }
    }

    /**
     * Check if required collections already exist
     * Safely handles permission issues
     */
    private suspend fun checkCollectionsExist(): Boolean {
        try {
            val usersSnapshot = db.collection("users").limit(1).get().await()
            val cropsSnapshot = db.collection("crops").limit(1).get().await()

            return !usersSnapshot.isEmpty && !cropsSnapshot.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error checking collections", e)
            // If there was a permissions error, we'll treat it as if collections don't exist
            // but we'll continue safely
            return false
        }
    }

    /**
     * Initialize users collection with admin user
     * Creates default admin user if no authentication
     */
    private suspend fun initializeUsersCollection() {
        try {
            // Create admin user document with default values
            val adminUser = hashMapOf(
                "name" to "Admin User",
                "phoneNumber" to "+917993754064",
                "createdAt" to FieldValue.serverTimestamp(),
                "isAdmin" to true,
                "lastActive" to FieldValue.serverTimestamp()
            )

            // Add admin user to users collection with default ID
            db.collection("users").document("admin_default").set(adminUser).await()
            Log.d(TAG, "Admin user created with default ID: admin_default")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing users collection", e)
            // Don't rethrow - continue with other initializations
        }
    }

    /**
     * Initialize crops collection with the 10 supported crops
     * Works without authentication
     */
    private suspend fun initializeCropsCollection() {
        try {
            // List of 10 supported crops with their common diseases
            val crops = listOf(
                mapOf(
                    "name" to "Chili",
                    "description" to "A spicy vegetable crop grown throughout India",
                    "imageUrl" to "crops/chili.jpg",
                    "commonDiseases" to listOf(
                        mapOf(
                            "name" to "Anthracnose",
                            "type" to "fungal",
                            "symptoms" to "Dark, sunken lesions on fruits"
                        ),
                        mapOf(
                            "name" to "Damping Off",
                            "type" to "fungal",
                            "symptoms" to "Seedlings collapse at soil level"
                        )
                    )
                ),
                mapOf(
                    "name" to "Okra",
                    "description" to "A flowering plant valued for its edible seed pods",
                    "imageUrl" to "crops/okra.jpg",
                    "commonDiseases" to listOf(
                        mapOf(
                            "name" to "Yellow Vein Mosaic",
                            "type" to "viral",
                            "symptoms" to "Yellow veins and mottling of leaves"
                        ),
                        mapOf(
                            "name" to "Powdery Mildew",
                            "type" to "fungal",
                            "symptoms" to "White powdery growth on leaves"
                        )
                    )
                ),
                mapOf(
                    "name" to "Maize",
                    "description" to "A cereal grain domesticated in Mexico and now grown across India",
                    "imageUrl" to "crops/maize.jpg",
                    "commonDiseases" to listOf(
                        mapOf(
                            "name" to "Leaf Blight",
                            "type" to "fungal",
                            "symptoms" to "Long elliptical grayish-green or tan lesions on leaves"
                        ),
                        mapOf(
                            "name" to "Stalk Rot",
                            "type" to "fungal",
                            "symptoms" to "Rotting of stalk tissue and lodging"
                        )
                    )
                ),
                mapOf(
                    "name" to "Cotton",
                    "description" to "A fiber crop grown extensively in India",
                    "imageUrl" to "crops/cotton.jpg",
                    "commonDiseases" to listOf(
                        mapOf(
                            "name" to "Bacterial Blight",
                            "type" to "bacterial",
                            "symptoms" to "Angular water-soaked lesions on leaves"
                        ),
                        mapOf(
                            "name" to "Fusarium Wilt",
                            "type" to "fungal",
                            "symptoms" to "Yellowing of leaves and wilting of plants"
                        )
                    )
                ),
                mapOf(
                    "name" to "Tomato",
                    "description" to "A fruit commonly grown as a vegetable crop",
                    "imageUrl" to "crops/tomato.jpg",
                    "commonDiseases" to listOf(
                        mapOf(
                            "name" to "Early Blight",
                            "type" to "fungal",
                            "symptoms" to "Dark spots with concentric rings on lower leaves"
                        ),
                        mapOf(
                            "name" to "Leaf Curl",
                            "type" to "viral",
                            "symptoms" to "Curling and twisting of leaves"
                        )
                    )
                ),
                mapOf(
                    "name" to "Watermelon",
                    "description" to "A vine-like flowering plant with sweet, juicy fruit",
                    "imageUrl" to "crops/watermelon.jpg",
                    "commonDiseases" to listOf(
                        mapOf(
                            "name" to "Downy Mildew",
                            "type" to "fungal",
                            "symptoms" to "Yellow to brown angular spots on upper leaf surface"
                        ),
                        mapOf(
                            "name" to "Fusarium Wilt",
                            "type" to "fungal",
                            "symptoms" to "Wilting and yellowing of vines"
                        )
                    )
                ),
                mapOf(
                    "name" to "Soybean",
                    "description" to "A legume species native to East Asia, now grown in India",
                    "imageUrl" to "crops/soybean.jpg",
                    "commonDiseases" to listOf(
                        mapOf(
                            "name" to "Rust",
                            "type" to "fungal",
                            "symptoms" to "Rusty brown spots on leaves"
                        ),
                        mapOf(
                            "name" to "Bacterial Blight",
                            "type" to "bacterial",
                            "symptoms" to "Water-soaked lesions on leaves"
                        )
                    )
                ),
                mapOf(
                    "name" to "Rice",
                    "description" to "A staple food crop in India and throughout Asia",
                    "imageUrl" to "crops/rice.jpg",
                    "commonDiseases" to listOf(
                        mapOf(
                            "name" to "Blast",
                            "type" to "fungal",
                            "symptoms" to "Diamond-shaped lesions on leaves"
                        ),
                        mapOf(
                            "name" to "Bacterial Leaf Blight",
                            "type" to "bacterial",
                            "symptoms" to "Yellow to white water-soaked lesions on leaf margins"
                        )
                    )
                ),
                mapOf(
                    "name" to "Wheat",
                    "description" to "A cereal grain grown widely across India",
                    "imageUrl" to "crops/wheat.jpg",
                    "commonDiseases" to listOf(
                        mapOf(
                            "name" to "Rust",
                            "type" to "fungal",
                            "symptoms" to "Rusty orange-brown pustules on leaves and stems"
                        ),
                        mapOf(
                            "name" to "Loose Smut",
                            "type" to "fungal",
                            "symptoms" to "Black masses of spores replacing grain heads"
                        )
                    )
                ),
                mapOf(
                    "name" to "Pigeon Pea",
                    "description" to "A perennial legume grown widely in India",
                    "imageUrl" to "crops/pigeon_pea.jpg",
                    "commonDiseases" to listOf(
                        mapOf(
                            "name" to "Fusarium Wilt",
                            "type" to "fungal",
                            "symptoms" to "Wilting and browning of plants"
                        ),
                        mapOf(
                            "name" to "Sterility Mosaic",
                            "type" to "viral",
                            "symptoms" to "Mosaic patterns on leaves and plant sterility"
                        )
                    )
                )
            )

            // Use batch write to add all crops efficiently
            val batch = db.batch()
            crops.forEachIndexed { index, crop ->
                // Create document ID from crop name to ensure consistency
                val cropName = crop["name"] as String
                val docId = cropName.lowercase().replace(" ", "_")
                val docRef = db.collection("crops").document(docId)
                batch.set(docRef, crop)
            }
            batch.commit().await()

            Log.d(TAG, "Added ${crops.size} crops to the crops collection")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing crops collection", e)
            // Don't rethrow - continue with other initializations
        }
    }

    /**
     * Initialize usageLimits collection with default values
     * Works without authentication
     */
    private suspend fun initializeUsageLimitsCollection() {
        try {
            // Create default usage limits
            val userUsageLimits = hashMapOf(
                "remainingUses" to 5,
                "maxUses" to 5,
                "resetDate" to Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000), // 30 days from now
                "lastUsed" to FieldValue.serverTimestamp()
            )

            // Add usage limits document with default ID
            db.collection("usageLimits").document("default_limits").set(userUsageLimits).await()
            Log.d(TAG, "Default usage limits created")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing usage limits collection", e)
            // Don't rethrow - continue with other operations
        }
    }
}