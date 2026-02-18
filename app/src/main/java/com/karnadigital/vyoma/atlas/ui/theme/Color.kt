package com.karnadigital.vyoma.atlas.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// Deep Space Palette
val DeepCharcoalNavyFull = Color(0xFF0F172A)
val DeepCharcoalNavyTop = Color(0xFF1E293B)  // Slightly lighter variant for gradient top
val DeepCharcoalNavyBottom = Color(0xFF0F172A) // Darker for gradient bottom

// Accents
val CyanData = Color(0xFF00E5FF)
val GoldenrodStar = Color(0xFFFFD700)

// Glassmorphism
val GlassSurface = Color.White.copy(alpha = 0.1f)
val GlassBorder = Color.White.copy(alpha = 0.2f)

// Text
val TextPrimary = Color.White
val TextSecondary = Color.White.copy(alpha = 0.7f)
val MonospaceData = Color.White.copy(alpha = 0.9f) // High contrast for data

// Gradients
val SpaceGradient = Brush.verticalGradient(
    colors = listOf(DeepCharcoalNavyTop, DeepCharcoalNavyBottom)
)
