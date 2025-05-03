/**
 * File: app/src/main/java/com/example/askchinna/util/DateTimeUtils.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Utility class for date and time operations
 * Optimized for low-end devices with simplified calculations
 */
object DateTimeUtils {

    private const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    private const val TIME_FORMAT_DISPLAY = "HH:mm"
    private const val DATE_TIME_FORMAT_DISPLAY = "dd MMM yyyy, HH:mm"
    private const val DATE_TIME_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    private val indianTimeZone = TimeZone.getTimeZone("Asia/Kolkata")

    /**
     * Returns current timestamp in milliseconds
     */
    fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

    /**
     * Returns current date as Date object with Indian timezone
     */
    fun getCurrentDate(): Date {
        val calendar = Calendar.getInstance(indianTimeZone)
        return calendar.time
    }

    /**
     * Format date to display format (dd MMM yyyy)
     */
    fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault())
        sdf.timeZone = indianTimeZone
        return sdf.format(date)
    }

    /**
     * Format time to display format (HH:mm)
     */
    fun formatTime(date: Date): String {
        val sdf = SimpleDateFormat(TIME_FORMAT_DISPLAY, Locale.getDefault())
        sdf.timeZone = indianTimeZone
        return sdf.format(date)
    }

    /**
     * Format datetime to display format (dd MMM yyyy, HH:mm)
     */
    fun formatDateTime(date: Date): String {
        val sdf = SimpleDateFormat(DATE_TIME_FORMAT_DISPLAY, Locale.getDefault())
        sdf.timeZone = indianTimeZone
        return sdf.format(date)
    }

    /**
     * Format datetime to ISO format for API requests
     */
    fun formatDateTimeISO(date: Date): String {
        val sdf = SimpleDateFormat(DATE_TIME_FORMAT_ISO, Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    /**
     * Parse ISO format date string to Date object
     */
    fun parseISODateTime(isoDateString: String): Date? {
        return try {
            val sdf = SimpleDateFormat(DATE_TIME_FORMAT_ISO, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(isoDateString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate time difference in minutes between current time and given time
     */
    private fun getMinutesDifference(timeInMillis: Long): Int {
        val diffMillis = getCurrentTimeMillis() - timeInMillis
        return TimeUnit.MILLISECONDS.toMinutes(diffMillis).toInt()
    }

    /**
     * Calculate remaining session time in minutes
     */
    fun getRemainingSessionMinutes(sessionStartTime: Long): Int {
        val elapsedMinutes = getMinutesDifference(sessionStartTime)
        val remainingMinutes = Constants.MAX_SESSION_DURATION_MINUTES - elapsedMinutes
        return if (remainingMinutes < 0) 0 else remainingMinutes
    }

    /**
     * Check if session has expired
     */
    fun hasSessionExpired(sessionStartTime: Long): Boolean {
        return getMinutesDifference(sessionStartTime) >= Constants.MAX_SESSION_DURATION_MINUTES
    }

    /**
     * Get date at the start of current month
     */
    fun getStartOfCurrentMonth(): Date {
        val calendar = Calendar.getInstance(indianTimeZone)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Get date at the start of previous month
     */
    fun getStartOfPreviousMonth(): Date {
        val calendar = Calendar.getInstance(indianTimeZone)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Calculate if a date is within the last N days
     * Optimized for performance on low-end devices
     */
    fun isWithinLastNDays(date: Date, days: Int): Boolean {
        val calendar = Calendar.getInstance(indianTimeZone)
        val today = calendar.timeInMillis

        calendar.time = date
        val givenDate = calendar.timeInMillis

        val diffMillis = today - givenDate
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return diffDays < days
    }

    /**
     * Convert seconds to formatted time string (MM:SS)
     */
    fun secondsToFormattedTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }
}