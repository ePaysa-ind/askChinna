/**
 * File: app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */
package com.example.askchinna.ui.cropselection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.askchinna.data.model.Crop
import com.example.askchinna.databinding.ItemCropBinding

/**
 * Adapter for the crop selection grid.
 * Uses ListAdapter to efficiently handle updates to the crop list.
 */
class CropAdapter(private val onCropClicked: (Crop) -> Unit) :
    ListAdapter<Crop, CropAdapter.CropViewHolder>(CropDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder {
        val binding = ItemCropBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CropViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CropViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CropViewHolder(private val binding: ItemCropBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCropClicked(getItem(position))
                }
            }
        }

        fun bind(crop: Crop) {
            binding.apply {
                imgCrop.setImageResource(crop.iconResId)
                txtCropName.text = crop.name
            }
        }
    }

    /**
     * DiffUtil callback for calculating the difference between two lists of crops
     * This optimizes updates to the RecyclerView by only updating items that changed
     */
    private class CropDiffCallback : DiffUtil.ItemCallback<Crop>() {
        override fun areItemsTheSame(oldItem: Crop, newItem: Crop): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Crop, newItem: Crop): Boolean {
            return oldItem == newItem
        }
    }
}
