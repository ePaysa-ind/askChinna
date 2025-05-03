package com.example.askchinna.util
/**
 * File: app/src/main/java/com/example/askchinna/util/Constants.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */
/**
 * Application-wide constants
 */
object Constants {
    // Firebase Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_USAGE = "usage"
    const val COLLECTION_IDENTIFICATION_HISTORY = "identification_history"

    // Session and Usage Limits
    const val MAX_SESSION_DURATION_MINUTES = 10
    const val MAX_MONTHLY_IDENTIFICATIONS = 5
    const val DAYS_IN_MONTH = 30

    // Image Capture
    const val MAX_IMAGE_SIZE_MB = 5
    const val IMAGE_QUALITY_COMPRESSION = 80
    const val MIN_IMAGE_RESOLUTION_WIDTH = 640
    const val MIN_IMAGE_RESOLUTION_HEIGHT = 480
    const val IMAGE_ASPECT_RATIO_TOLERANCE = 0.2f

    // Timeouts
    const val NETWORK_TIMEOUT_SECONDS = 30
    const val API_RETRY_COUNT = 3
    const val API_RETRY_DELAY_MS = 1000L

    // Authentication
    const val OTP_TIMEOUT_SECONDS = 60
    const val OTP_LENGTH = 6
    const val PHONE_NUMBER_LENGTH = 10
    const val COUNTRY_CODE_INDIA = "+91"

    // File paths
    const val TEMP_IMAGE_PATH = "temp_images"
    const val FIREBASE_STORAGE_IMAGES_PATH = "crop_images"

    // Shared Preferences
    const val PREF_FILE_NAME = "askchinna_prefs"
    const val PREF_USER_DATA = "user_data"
    const val PREF_USAGE_DATA = "usage_data"
    const val PREF_AUTH_STATE = "auth_state"
    const val PREF_SESSION_START_TIME = "session_start_time"

    // Gemini API
    const val MAX_TOKENS_RESPONSE = 1024
    const val GEMINI_MODEL = "gemini-pro-vision"

    // Content Types
    const val CONTENT_TYPE_IMAGE = "image/jpeg"

    // Intent extras
    const val EXTRA_CROP_ID = "crop_id"
    const val EXTRA_IMAGE_URI = "image_uri"
    const val EXTRA_RESULT_ID = "result_id"

    // Result codes
    const val RC_IMAGE_CAPTURE = 100
    const val RC_IMAGE_PICK = 101
    const val RC_PERMISSIONS = 102

    // Permissions
    const val PERMISSION_REQUEST_DELAY_MS = 500L

    // Debug logging flag
    const val DEBUG = true

    // Retrofit base URLs
    const val GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=GEMINI_API_KEY"

}