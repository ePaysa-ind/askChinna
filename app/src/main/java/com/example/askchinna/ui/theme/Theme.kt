package com.example.askchinna.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light color scheme optimized for outdoor visibility
private val LightColorScheme = lightColorScheme(
    primary = Green500,
    onPrimary = White,
    primaryContainer = Green200,
    onPrimaryContainer = Green800,
    secondary = Orange500,
    onSecondary = Black,
    secondaryContainer = Orange200,
    onSecondaryContainer = Orange700,
    tertiary = Blue500,
    background = Gray50,
    onBackground = Gray900,
    surface = White,
    onSurface = Gray900,
    error = Red500,
    onError = White
)

// Dark color scheme with less eye strain for night use
private val DarkColorScheme = darkColorScheme(
    primary = Green300,
    onPrimary = Green800,
    primaryContainer = Green700,
    onPrimaryContainer = Green200,
    secondary = Orange300,
    onSecondary = Orange700,
    secondaryContainer = Orange700,
    onSecondaryContainer = Orange200,
    tertiary = Blue500,
    background = Color(0xFF121212),
    onBackground = Gray200,
    surface = Gray850,
    onSurface = Gray200,
    error = Red500,
    onError = White
)

@Composable
fun AskChinnaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Make status bar color transparent for edge-to-edge design
            window.statusBarColor = Color.Transparent.toArgb()

            // Set up edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Update status bar appearance based on theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}