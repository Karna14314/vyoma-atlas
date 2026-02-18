package com.karnadigital.vyoma.atlas.ui.skymap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.karnadigital.vyoma.atlas.data.local.entity.AstronomicalObject

data class ScreenPoint(
    val id: String,
    val name: String,
    val position: Offset,
    val brightness: Float, // 0.0 to 1.0
    val color: Color,
    val type: String
)

data class TargetIndicator(
    val isVisible: Boolean, // On screen?
    val position: Offset, // Screen position if visible, or Clamped edge position if not
    val directionAngle: Float? = null, // Angle in degrees for arrow rotation (if off-screen)
    val objectName: String
)

data class SkyMapState(
    val points: List<ScreenPoint> = emptyList(),
    val compassPoints: List<ScreenPoint> = emptyList(),
    val target: TargetIndicator? = null,
    val centerRa: Double = 0.0,
    val centerDec: Double = 0.0,
    val fieldOfView: Float = 45f
)
