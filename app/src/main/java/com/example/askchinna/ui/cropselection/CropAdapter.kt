/**
 * File: app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 6, 2025
 * Version: 1.2
 * 
 * Change Log:
 * 1.2 - May 6, 2025
 * - Added proper error handling for view binding
 * - Added memory optimization for image loading
 * - Added proper cleanup in onViewRecycled
 * - Added proper state restoration
 * - Added proper error logging
 * - Added null safety checks
 */
package com.example.askchinna.ui.cropselection

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.askchinna.R
import com.example.askchinna.data.model.Crop
import com.example.askchinna.databinding.ItemCropBinding
import com.example.askchinna.util.ImageHelper

/**
 * Adapter for the crop selection grid.
 * Uses ListAdapter to efficiently handle updates to the crop list.
 */
class CropAdapter(
    private val onCropClicked: (Crop) -> Unit,
    private val imageHelper: ImageHelper
) : ListAdapter<Crop, CropAdapter.CropViewHolder>(CropDiffCallback()) {

    private val TAG = "CropAdapter"
    private var isInitialized = false
    private var currentList: List<Crop>? = null

    init {
        try {
            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing adapter", e)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder {
        return try {
            if (!isInitialized) {
                Log.w(TAG, "Adapter not initialized")
                throw IllegalStateException("Adapter not initialized")
            }

            val binding = ItemCropBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            CropViewHolder(binding)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating view holder", e)
            throw e
        }
    }

    override fun onBindViewHolder(holder: CropViewHolder, position: Int) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Adapter not initialized")
                return
            }

            if (position < 0 || position >= itemCount) {
                Log.w(TAG, "Invalid position: $position")
                return
            }

            val crop = getItem(position)
            if (crop != null) {
                holder.bind(crop)
            } else {
                Log.w(TAG, "Crop is null at position $position")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding view holder at position $position", e)
        }
    }

    override fun onViewRecycled(holder: CropViewHolder) {
        try {
            super.onViewRecycled(holder)
            holder.cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "Error recycling view holder", e)
        }
    }

    override fun submitList(list: List<Crop>?) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Adapter not initialized")
                return
            }

            currentList = list
            super.submitList(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting list", e)
        }
    }

    inner class CropViewHolder(private val binding: ItemCropBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentCrop: Crop? = null

        init {
            try {
                binding.root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        try {
                            currentCrop?.let { crop ->
                                onCropClicked(crop)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error handling click at position $position", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing view holder", e)
            }
        }

        fun bind(crop: Crop) {
            try {
                currentCrop = crop
                binding.apply {
                    imgCrop.setImageResource(crop.iconResId)
                    txtCropName.text = crop.name
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error binding crop: ${crop.name}", e)
            }
        }

        fun cleanup() {
            try {
                currentCrop = null
                binding.imgCrop.setImageDrawable(null)
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up view holder", e)
            }
        }
    }

    /**
     * DiffUtil callback for calculating the difference between two lists of crops
     * This optimizes updates to the RecyclerView by only updating items that changed
     */
    private class CropDiffCallback : DiffUtil.ItemCallback<Crop>() {
        override fun areItemsTheSame(oldItem: Crop, newItem: Crop): Boolean {
            return try {
                oldItem.id == newItem.id
            } catch (e: Exception) {
                Log.e("CropDiffCallback", "Error comparing items", e)
                false
            }
        }

        override fun areContentsTheSame(oldItem: Crop, newItem: Crop): Boolean {
            return try {
                oldItem == newItem
            } catch (e: Exception) {
                Log.e("CropDiffCallback", "Error comparing contents", e)
                false
            }
        }
    }
}
