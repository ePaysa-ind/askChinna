package com.example.askchinna.data.remote

/**
 * app/src/main/java/com/askchinna/data/remote/NetworkExceptionHandler.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */


import android.content.Context
import com.askchinna.R
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enum class for different types of network errors
 */
enum class NetworkError {
    NO_CONNECTION,
    TIMEOUT,
    RATE_LIMIT,
    SERVER_ERROR,
    UNKNOWN
}

/**
 * Handles network exceptions and provides user-friendly error messages
 */
@Singleton
class NetworkExceptionHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Identifies the type of network error from a Throwable
     * @param throwable The throwable to analyze
     * @return NetworkError The identified network error type
     */
    fun handle(throwable: Throwable): NetworkError {
        return when (throwable) {
            is UnknownHostException, is ConnectException ->
                NetworkError.NO_CONNECTION
            is SocketTimeoutException ->
                NetworkError.TIMEOUT
            is HttpException -> {
                when (throwable.code()) {
                    429 -> NetworkError.RATE_LIMIT
                    in 500..599 -> NetworkError.SERVER_ERROR
                    else -> NetworkError.UNKNOWN
                }
            }
            else -> NetworkError.UNKNOWN
        }
    }

    /**
     * Gets a user-friendly error message for a network error
     * @param error The network error type
     * @return String User-friendly error message
     */
    fun getErrorMessage(error: NetworkError): String {
        return when (error) {
            NetworkError.NO_CONNECTION ->
                context.getString(R.string.error_no_connection)
            NetworkError.TIMEOUT ->
                context.getString(R.string.error_timeout)
            NetworkError.RATE_LIMIT ->
                context.getString(R.string.error_rate_limit)
            NetworkError.SERVER_ERROR ->
                context.getString(R.string.error_server)
            NetworkError.UNKNOWN ->
                context.getString(R.string.error_unknown)
        }
    }

    /**
     * Gets a user-friendly error message directly from a Throwable
     * @param throwable The throwable to analyze
     * @return String User-friendly error message
     */
    fun getErrorMessage(throwable: Throwable): String {
        val error = handle(throwable)
        return getErrorMessage(error)
    }

    /**
     * Gets fallback action message for a network error
     * @param error The network error type
     * @return String Fallback action message
     */
    fun getFallbackMessage(error: NetworkError): String {
        return when (error) {
            NetworkError.NO_CONNECTION ->
                context.getString(R.string.fallback_no_connection)
            NetworkError.TIMEOUT ->
                context.getString(R.string.fallback_timeout)
            NetworkError.RATE_LIMIT ->
                context.getString(R.string.fallback_rate_limit)
            NetworkError.SERVER_ERROR ->
                context.getString(R.string.fallback_server_error)
            NetworkError.UNKNOWN ->
                context.getString(R.string.fallback_unknown)
        }
    }
}