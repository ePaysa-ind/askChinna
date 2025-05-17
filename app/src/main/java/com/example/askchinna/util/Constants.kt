/**
 * file path: app/src/main/java/com/example/askchinna/util/Constants.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 4, 2025
 * Version: 1.1
 */

package com.example.askchinna.util

/**
 * Constants used throughout the application.
 * All constants are organized by category and documented.
 */
object Constants {
    // Application
    const val APP_NAME = "askChinna"
    const val APP_VERSION = "1.0.0"
    const val TAG_PREFIX = "askChinna_"

    // Session Management
    const val SESSION_TIMEOUT_MINUTES = 10L
    const val MAX_IDENTIFICATIONS_PER_MONTH = 5
    const val USAGE_RESET_DAYS = 30
    const val MAX_SESSION_DURATION_MINUTES = 30
    const val DAYS_IN_MONTH = 30

    // Network
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val MAX_RETRY_ATTEMPTS = 3
    const val RETRY_DELAY_MS = 1000L

    // Image Processing
    const val MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024 // 5MB
    const val IMAGE_COMPRESSION_QUALITY = 80
    const val MIN_IMAGE_DIMENSION = 800
    const val MAX_IMAGE_DIMENSION = 2048
    const val MIN_IMAGE_RESOLUTION_WIDTH = 800
    const val MIN_IMAGE_RESOLUTION_HEIGHT = 800
    const val MAX_IMAGE_SIZE_MB = 5
    const val IMAGE_QUALITY_COMPRESSION = 80
    const val TEMP_IMAGE_PATH = "temp_images"

    // File Management
    const val IMAGE_FILE_PREFIX = "IMG_"
    const val IMAGE_FILE_SUFFIX = ".jpg"
    const val PDF_FILE_PREFIX = "Report_"
    const val PDF_FILE_SUFFIX = ".pdf"

    // Firebase
    const val FIREBASE_COLLECTION_USERS = "users"
    const val FIREBASE_COLLECTION_IDENTIFICATIONS = "identifications"
    const val FIREBASE_COLLECTION_CROPS = "crops"
    const val FIREBASE_STORAGE_IMAGES = "identification_images"

    // Shared Preferences
    const val PREF_NAME = "askChinnaPrefs"
    const val PREF_KEY_USER_ID = "userId"
    const val PREF_KEY_LAST_USAGE = "lastUsage"
    const val PREF_KEY_USAGE_COUNT = "usageCount"
    const val PREF_KEY_SESSION_START = "sessionStart"
    const val PREF_KEY_API_KEY = "apiKey"

    // Error Messages
    const val ERROR_NETWORK = "Network error occurred"
    const val ERROR_IMAGE_PROCESSING = "Error processing image"
    const val ERROR_SESSION_EXPIRED = "Session expired"
    const val ERROR_USAGE_LIMIT = "Usage limit reached"
    const val ERROR_INVALID_INPUT = "Invalid input provided"

    // Success Messages
    const val SUCCESS_IDENTIFICATION = "Identification completed"
    const val SUCCESS_FEEDBACK = "Feedback submitted"
    const val SUCCESS_IMAGE_SAVED = "Image saved successfully"

    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PASSWORD_LENGTH = 20
    const val VALID_EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)\$"
    const val VALID_PHONE_REGEX = "^[0-9]{10}\$"

    // UI
    const val ANIMATION_DURATION_MS = 300L
    const val TOAST_DURATION_MS = 2000L
    const val DIALOG_DISMISS_DELAY_MS = 1500L
}