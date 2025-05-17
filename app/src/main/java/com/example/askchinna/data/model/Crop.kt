/**
 * file path: app/src/main/java/com/example/askchinna/data/model/Crop.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 13, 2025
 * Version: 1.4
 */

package com.example.askchinna.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

/**
 * Data class representing a crop supported by the askChinna app
 * Contains essential information for crop selection and identification
 */
@Parcelize
data class Crop(
    /**
     * Unique identifier for the crop.
     */
    @SerializedName("id")
    val id: String,

    /**
     * Name of the crop.
     */
    @SerializedName("name")
    val name: String,

    /**
     * Resource ID for the crop's icon.
     */
    @SerializedName("iconResId")
    val iconResId: Int = 0
) : Parcelable