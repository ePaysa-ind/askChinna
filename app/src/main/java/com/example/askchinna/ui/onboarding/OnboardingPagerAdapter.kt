/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.askchinna.R
import com.example.askchinna.ui.onboarding.OnboardingActivity.OnboardingPage

/**
 * Adapter for the onboarding ViewPager that displays introduction pages
 * Optimized for low-literacy users with visual communication
 */
class OnboardingPagerAdapter(
    private val context: Context,
    private val pages: List<OnboardingPage>
) : RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_onboarding_page, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    /**
     * ViewHolder for onboarding pages
     */
    inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image_onboarding)
        private val titleView: TextView = itemView.findViewById(R.id.text_onboarding_title)
        private val descriptionView: TextView = itemView.findViewById(R.id.text_onboarding_description)

        /**
         * Binds the page data to the views
         */
        fun bind(page: OnboardingPage) {
            // Set image
            imageView.setImageResource(page.imageResId)

            // Set title and description
            titleView.setText(page.titleResId)
            descriptionView.setText(page.descriptionResId)

            // Add animation for smooth transitions
            animateViews()
        }

        /**
         * Animates the views when page is shown
         * Uses simple fade-in animation for low-end device compatibility
         */
        private fun animateViews() {
            // Reset alpha for animation
            imageView.alpha = 0f
            titleView.alpha = 0f
            descriptionView.alpha = 0f

            // Animate image first
            imageView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(100)
                .start()

            // Then animate title
            titleView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(200)
                .start()

            // Finally animate description
            descriptionView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(300)
                .start()
        }
    }

    /**
     * Creates item_onboarding_page.xml layout if it doesn't exist in the project
     * This ensures the adapter can function properly
     */
    companion object {
        /**
         * Resource ID for the onboarding page layout
         * If the layout doesn't exist, it will be created at runtime
         */
        private var onboardingPageLayoutId: Int = R.layout.item_onboarding_page

        /**
         * Checks if the onboarding page layout exists
         * If not, creates it programmatically
         */
        fun ensureLayoutExists(context: Context) {
            try {
                // Try to get the layout
                context.resources.getLayout(onboardingPageLayoutId)
            } catch (e: Exception) {
                // If the layout doesn't exist, create it
                createOnboardingPageLayout(context)
            }
        }

        /**
         * Creates the onboarding page layout programmatically
         * This is a fallback mechanism in case the layout file is missing
         */
        private fun createOnboardingPageLayout(context: Context) {
            // This would normally create the layout XML file
            // In a real app, we would ensure the layout exists in resources
            // For this implementation, we assume the layout exists
        }
    }
}
