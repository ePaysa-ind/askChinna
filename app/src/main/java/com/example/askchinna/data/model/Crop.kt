package com.example.askchinna.data.model
/**
 * File: app/src/main/java/com/example/askchinna/data/model/Crop.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a crop supported by the askChinna app
 * Contains all necessary information for displaying crops in the selection screen
 * and for processing in the identification flow
 */
@Parcelize
data class Crop(
    val id: String,
    val name: String,
    val iconResId: Int,
    val commonPests: List<String> = emptyList(),
    val commonDiseases: List<String> = emptyList()
) : Parcelable

