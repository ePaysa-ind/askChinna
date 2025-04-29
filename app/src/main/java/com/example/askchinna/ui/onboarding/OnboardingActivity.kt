/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.askchinna.R
import com.example.askchinna.data.local.SharedPreferencesManager
import com.example.askchinna.ui.auth.LoginActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Onboarding activity that introduces the app features to new users
 * Optimized for low-literacy users with minimal text and visual communication
 */
@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var nextButton: Button
    private lateinit var skipButton: TextView
    private lateinit var backButton: ImageView

    private val onboardingPages = listOf(
        OnboardingPage(
            titleResId = R.string.onboarding_welcome_title,
            descriptionResId = R.string.onboarding_welcome_description,
            imageResId = R.drawable.ic_app_logo
        ),
        OnboardingPage(
            titleResId = R.string.onboarding_crop_selection_title,
            descriptionResId = R.string.onboarding_crop_selection_description,
            imageResId = R.drawable.ic_identification
        ),
        OnboardingPage(
            titleResId = R.string.onboarding_camera_title,
            descriptionResId = R.string.onboarding_camera_description,
            imageResId = R.drawable.ic_camera
        ),
        OnboardingPage(
            titleResId = R.string.onboarding_results_title,
            descriptionResId = R.string.onboarding_results_description,
            imageResId = R.drawable.ic_help
        ),
        OnboardingPage(
            titleResId = R.string.onboarding_limits_title,
            descriptionResId = R.string.onboarding_limits_description,
            imageResId = R.drawable.ic_usage
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Check if onboarding has been completed before
        if (sharedPreferencesManager.isOnboardingCompleted()) {
            navigateToLogin()
            return
        }

        // Initialize views
        viewPager = findViewById(R.id.onboarding_view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        nextButton = findViewById(R.id.button_next)
        skipButton = findViewById(R.id.text_skip)
        backButton = findViewById(R.id.image_back)

        // Set up ViewPager with adapter
        val pagerAdapter = OnboardingPagerAdapter(this, onboardingPages)
        viewPager.adapter = pagerAdapter

        // Set up TabLayout with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { _, _ ->
            // No special configuration needed for each tab
        }.attach()

        // Set up button click listeners
        setupClickListeners()

        // Set up page change listener
        setupPageChangeListener()
    }

    /**
     * Sets up click listeners for navigation buttons
     */
    private fun setupClickListeners() {
        // Next button click listener
        nextButton.setOnClickListener {
            if (viewPager.currentItem == onboardingPages.size - 1) {
                // Last page, complete onboarding
                completeOnboarding()
            } else {
                // Move to next page
                viewPager.currentItem += 1
            }
        }

        // Skip button click listener
        skipButton.setOnClickListener {
            completeOnboarding()
        }

        // Back button click listener
        backButton.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }
    }

    /**
     * Sets up page change listener to update navigation UI
     */
    private fun setupPageChangeListener() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateNavigationUI(position)
            }
        })

        // Initialize with first page
        updateNavigationUI(0)
    }

    /**
     * Updates the navigation UI based on current page
     */
    private fun updateNavigationUI(position: Int) {
        // Show/hide back button
        backButton.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE

        // Change next button text on last page
        if (position == onboardingPages.size - 1) {
            nextButton.setText(R.string.onboarding_get_started)
        } else {
            nextButton.setText(R.string.next)
        }

        // Hide skip button on last page
        skipButton.visibility = if (position == onboardingPages.size - 1) View.GONE else View.VISIBLE
    }

    /**
     * Marks onboarding as completed and navigates to login screen
     */
    private fun completeOnboarding() {
        // Mark onboarding as completed
        sharedPreferencesManager.setOnboardingCompleted(true)

        // Navigate to login screen
        navigateToLogin()
    }

    /**
     * Navigates to the login screen
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Data class representing an onboarding page
     */
    data class OnboardingPage(
        val titleResId: Int,
        val descriptionResId: Int,
        val imageResId: Int
    )
}
