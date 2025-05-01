package com.example.askchinna.ui.theme
/*
Copyright (c) 2025 askChinna App Development Team
  /app/src/main/ui/theme/ComposeThemeBridge.kt
  Created: April 30, 2025
  Version: 1.0.0
* **/
import android.content.Context
import android.view.ContextThemeWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.example.askchinna.R

/**
 * Local composition provides context themed with current Compose MaterialTheme colors
 */
val LocalThemeWrappedContext = compositionLocalOf<Context> { error("No themed context provided") }

/**
 * Wrapper composable that provides a bridge between Compose theme and XML views
 * Use this when embedding XML layouts within Compose UI
 */
@Composable
fun ProvideThemeWrappedContext(content: @Composable () -> Unit) {
    val baseContext = LocalContext.current

    // Extract current theme colors
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorSecondary = MaterialTheme.colorScheme.secondary
    val colorSurface = MaterialTheme.colorScheme.surface
    val colorBackground = MaterialTheme.colorScheme.background
    val colorError = MaterialTheme.colorScheme.error

    // Create themed context wrapper
    val themedContext = ContextThemeWrapper(baseContext, R.style.Theme_AskChinna)

    // Provide the wrapped context to all descendants
    CompositionLocalProvider(LocalThemeWrappedContext provides themedContext) {
        content()
    }
}

/**
 * Use this composable to wrap any AndroidView that requires the current theme
 */
@Composable
fun ThemedAndroidViewContent(content: @Composable () -> Unit) {
    ProvideThemeWrappedContext {
        content()
    }
}