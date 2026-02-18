package com.karnadigital.vyoma.atlas.ui.theme

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

// We define only a Dark Color Scheme because "Deep Space" theme is inherently dark.
private val DarkDeepSpaceScheme = darkColorScheme(
    primary = CyanData,
    secondary = GoldenrodStar,
    tertiary = GlassSurface,
    background = DeepCharcoalNavyFull,
    surface = DeepCharcoalNavyTop,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

// We can optionally use dynamic color if the user wants system integration, but for the "Deep Space" specific look,
// we might prefer our custom palette. Let's stick to our custom palette for now to ensure consistency.
// However, I will keep the dynamic flag structure if you want to switch later.

@Composable
fun AstronomyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Force our custom theme for now
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> DarkDeepSpaceScheme // Always use Dark Deep Space by default for now
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepCharcoalNavyTop.toArgb() // Match top gradient
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Always light text
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Uses default for now, can be customized later if needed
        content = content
    )
}
