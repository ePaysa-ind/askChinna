/**
 * File: app/src/main/java/com/example/askchinna/util/DateTimeUtils.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 4, 2025
 * Version: 1.1
 */

package com.example.askchinna.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Utility class for handling date and time operations.
 * Provides methods for formatting, parsing, and manipulating dates.
 */
object DateTimeUtils {
    private const val TAG = "DateTimeUtils"

    private const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    private const val TIME_FORMAT_DISPLAY = "HH:mm"
    private const val DATE_TIME_FORMAT_DISPLAY = "dd MMM yyyy, HH:mm"
    private const val DATE_TIME_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    private val indianTimeZone = TimeZone.getTimeZone("Asia/Kolkata")
    private val mainHandler = Handler(Looper.getMainLooper())

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
     * Formats a date to a string using the specified format.
     *
     * @param date The date to format
     * @param format The format to use
     * @return The formatted date string or null if formatting fails
     */
    fun formatDate(date: Date, format: String = DATE_FORMAT_DISPLAY): String? {
        return try {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            sdf.timeZone = indianTimeZone
            sdf.format(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting date", e)
            null
        }
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
     * Parses a date string using the specified format.
     *
     * @param dateString The date string to parse
     * @param format The format to use
     * @return The parsed date or null if parsing fails
     */
    fun parseDate(dateString: String, format: String = DATE_FORMAT_DISPLAY): Date? {
        return try {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            sdf.timeZone = indianTimeZone
            sdf.parse(dateString)
        } catch (e: ParseException) {
            Log.e(TAG, "Error parsing date: $dateString", e)
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
     * Checks if a date is within the last N days.
     *
     * @param date The date to check
     * @param days The number of days to check against
     * @return true if the date is within the last N days, false otherwise
     */
    fun isWithinLastNDays(date: Date, days: Int): Boolean {
        val calendar = Calendar.getInstance(indianTimeZone)
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val nDaysAgo = calendar.time
        return date.after(nDaysAgo) || date.equals(nDaysAgo)
    }

    /**
     * Convert seconds to formatted time string (MM:SS)
     */
    fun secondsToFormattedTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }

    /**
     * Executes the given action on the main thread after the specified delay
     * @param delayMillis The delay in milliseconds before executing the action
     * @param action The action to execute after the delay
     * @return A Runnable that can be used to cancel the delayed execution
     */
    fun delayOnMain(delayMillis: Long, action: () -> Unit): Runnable {
        val runnable = Runnable { action() }
        mainHandler.postDelayed(runnable, delayMillis)
        return runnable
    }

    /**
     * Executes the given action on the main thread at regular intervals
     * @param intervalMillis The interval in milliseconds between executions
     * @param initialDelayMillis The initial delay in milliseconds before the first execution
     * @param action The action to execute at each interval
     * @return A Runnable that can be used to cancel the interval
     */
    fun intervalOnMain(intervalMillis: Long, initialDelayMillis: Long = 0, action: () -> Unit): Runnable {
        val runnable = object : Runnable {
            override fun run() {
                action()
                mainHandler.postDelayed(this, intervalMillis)
            }
        }

        mainHandler.postDelayed(runnable, initialDelayMillis)
        return runnable
    }

    /**
     * Gets the current date and time.
     *
     * @return The current date and time
     */
    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    /**
     * Gets the start of the current day.
     *
     * @return The start of the current day
     */
    fun getStartOfDay(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    /**
     * Gets the end of the current day.
     *
     * @return The end of the current day
     */
    fun getEndOfDay(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }

    /**
     * Adds days to a date.
     *
     * @param date The date to add days to
     * @param days The number of days to add
     * @return The new date
     */
    fun addDays(date: Date, days: Int): Date {
        return Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_MONTH, days)
        }.time
    }

    /**
     * Gets the difference in days between two dates.
     *
     * @param date1 The first date
     * @param date2 The second date
     * @return The difference in days
     */
    fun getDaysDifference(date1: Date, date2: Date): Int {
        return try {
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = date1
            cal2.time = date2
            val diff = cal2.timeInMillis - cal1.timeInMillis
            (diff / (24 * 60 * 60 * 1000)).toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating days difference", e)
            0
        }
    }

    /**
     * Formats a date for display.
     *
     * @param date The date to format
     * @return The formatted date string or null if formatting fails
     */
    fun formatDateForDisplay(date: Date): String? {
        return formatDate(date, DATE_FORMAT_DISPLAY)
    }

    /**
     * Formats a time for display.
     *
     * @param date The date containing the time to format
     * @return The formatted time string or null if formatting fails
     */
    fun formatTimeForDisplay(date: Date): String? {
        return formatDate(date, TIME_FORMAT_DISPLAY)
    }

    /**
     * Formats a date and time for display.
     *
     * @param date The date to format
     * @return The formatted date and time string or null if formatting fails
     */
    fun formatDateTimeForDisplay(date: Date): String? {
        return formatDate(date, DATE_TIME_FORMAT_DISPLAY)
    }

    /**
     * Converts a date to UTC.
     *
     * @param date The date to convert
     * @return The UTC date
     */
    fun toUTC(date: Date): Date {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            time = date
        }.time
    }

    /**
     * Converts a UTC date to local time.
     *
     * @param date The UTC date to convert
     * @return The local date
     */
    fun fromUTC(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
        }.time
    }
}