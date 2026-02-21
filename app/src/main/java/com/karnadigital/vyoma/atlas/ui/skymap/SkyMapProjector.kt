package com.karnadigital.vyoma.atlas.ui.skymap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.karnadigital.vyoma.atlas.core.math.Matrix3x3
import com.karnadigital.vyoma.atlas.core.math.Vector3
import com.karnadigital.vyoma.atlas.data.local.entity.AstronomicalObject
import com.karnadigital.vyoma.atlas.ui.theme.CyanData
import com.karnadigital.vyoma.atlas.ui.theme.GoldenrodStar
import com.karnadigital.vyoma.atlas.ui.theme.TextSecondary
import java.lang.Math.toDegrees
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.tan

object SkyMapProjector {

    fun project(
        obj: AstronomicalObject,
        pointing: Matrix3x3,
        screenSize: Size,
        fovDegrees: Float
    ): ScreenPoint? {
        if (obj.rightAscension == null || obj.declination == null) return null
        
        val celestialPos = getCelestialVector(obj.rightAscension, obj.declination)
        val pos = projectVector(celestialPos, pointing, screenSize, fovDegrees) ?: return null

        val mag = obj.magnitude ?: 5.0
        val brightness = (1.0f - ((mag.toFloat() + 1.5f) / 8.0f)).coerceIn(0.2f, 1.0f)

        return ScreenPoint(
            id = obj.id,
            name = obj.name,
            position = pos,
            brightness = brightness.toFloat(),
            color = if (obj.type == "PLANET") CyanData else GoldenrodStar,
            type = obj.type
        )
    }
    
    fun projectCompassPoint(
        vector: Vector3,
        label: String,
        pointing: Matrix3x3,
        screenSize: Size,
        fovDegrees: Float
    ): ScreenPoint? {
         val pos = projectVector(vector, pointing, screenSize, fovDegrees) ?: return null
         return ScreenPoint(
             id = "compass_$label",
             name = label,
             position = pos,
             brightness = 1.0f,
             color = TextSecondary,
             type = "COMPASS"
         )
    }
    
    fun calculateTargetIndicator(
        obj: AstronomicalObject,
        pointing: Matrix3x3,
        screenSize: Size,
        fovDegrees: Float
    ): TargetIndicator? {
        try {
            if (obj.rightAscension == null || obj.declination == null) return null
            if (fovDegrees <= 0f || fovDegrees >= 180f) return null

            val celestialPos = getCelestialVector(obj.rightAscension, obj.declination)

            // Transform to Device Coordinates
            val devicePos = pointing * celestialPos

            // Check if visible
            val projectedPos = projectVector(celestialPos, pointing, screenSize, fovDegrees)

            if (projectedPos != null) {
                // Is On Screen
                return TargetIndicator(
                    isVisible = true,
                    position = projectedPos,
                    objectName = obj.name
                )
            } else {
                // Off Screen
                // Calculate direction from center (0,0) in device coords (x, y)
                 val dx = devicePos.x
                 // Typically Y is up in 3D, but Screen Y is down.
                 // We want the direction on the screen plane.
                 // In device coords from Stardroid: X=Right, Y=Up, Z=Backward (towards user) if looking at screen?
                 // Let's assume X=Right, Y=Up on the screen plane.
                 val dy = devicePos.y

                 // Angle on screen: atan2(dy, dx)
                 // Check coordinate flip (West/East). Stardroid X axis direction.
                 // If East is Left, then +X might be Left?
                 // Let's stick generic: Standard math.

                 // We want to point a generic arrow.
                 // Let's compute angle. 0 degrees = Right (East?).
                 // We need to map this to UI rotation.
                 // UI Canvas 0 angle usually 3 o'clock.

                 // We need to clamp the position to a circle edge.
                 val radius = (Math.min(screenSize.width, screenSize.height) / 2f) - 50f
                 val angleRad = atan2(dy, dx)
                 // Invert X because sky map X is usually flipped?
                 // Logic: If I look Right (+X), and object is there, vector is +X.
                 // If I turn Right, object should move Left on screen.
                 // Guidance should point Right.
                 // So if object is at +X, Arrow should point +X.

                 // Wait, if Z > 0 (Behind), the vector (x,y) is still valid direction to turn.
                 // e.g. (1, 0, 10). To my right and behind. I turn right -> (1, 0, 0).

                 val clampX = (screenSize.width / 2f) + (radius * kotlin.math.cos(angleRad))
                 val clampY = (screenSize.height / 2f) - (radius * kotlin.math.sin(angleRad)) // Y flip for screen

                 // Arrow rotation:
                 // 0 deg is usually Up or Right depending on icon.
                 // If icon points Up (0 deg), and we want it to point at angleRad.
                 // Compose rotation is degrees clockwise?
                 // angleRad is mathematical counter-clockwise from X-axis.
                 // Let's just pass the angle in degrees relative to X-axis (Right).
                 // Add 90 degrees offset because arrow resource points UP, but 0 degrees (Right) should be 90 degrees rotation?
                 // If arrow points UP, and we want it to point RIGHT (0 angle), we need +90 rotation.
                 val degrees = -toDegrees(angleRad.toDouble()).toFloat() + 90f

                 val x = clampX.toFloat()
                 val y = clampY.toFloat()

                 if (x.isNaN() || y.isNaN() || degrees.isNaN()) return null

                 return TargetIndicator(
                     isVisible = false,
                     position = Offset(x, y),
                     directionAngle = degrees,
                     objectName = obj.name
                 )
            }
        } catch (e: Exception) {
            return null
        }
    }

    private fun getCelestialVector(ra: Double, dec: Double): Vector3 {
        val raRad = Math.toRadians(ra)
        val decRad = Math.toRadians(dec)
        return Vector3(
            (Math.cos(raRad) * Math.cos(decRad)).toFloat(),
            (Math.sin(raRad) * Math.cos(decRad)).toFloat(),
            Math.sin(decRad).toFloat()
        )
    }

    private fun projectVector(
        vec: Vector3,
        pointing: Matrix3x3,
        screenSize: Size,
        fovDegrees: Float
    ): Offset? {
        return try {
            val devicePos = pointing * vec
            
            // Forward is -Z (looking into screen / out of back of phone)
            val z = -devicePos.z
            
            // Safety check: Behind camera, too close, or at zenith/nadir (gimbal lock)
            if (z <= 0.1f || z.isNaN() || z.isInfinite()) return null

            if (fovDegrees <= 0f || fovDegrees >= 180f) return null

            val factor = tan(Math.toRadians(fovDegrees / 2.0)).toFloat()
            
            // Additional safety: check factor validity
            if (factor <= 0.0001f || factor.isNaN() || factor.isInfinite()) return null
            
            val xRaw = devicePos.x / z / factor
            val yRaw = devicePos.y / z / factor
            
            // Safety check for NaN/Infinite values
            if (xRaw.isNaN() || xRaw.isInfinite() || yRaw.isNaN() || yRaw.isInfinite()) return null
            
            // Check bounds (allow slightly off-screen for smooth exit)
            if (abs(xRaw) > 1.2 || abs(yRaw) > 1.2) return null

            val x = (screenSize.width / 2f) + (xRaw * screenSize.width / 2f)
            val y = (screenSize.height / 2f) - (yRaw * screenSize.height / 2f)
            
            // Final safety check on screen coordinates
            if (x.isNaN() || x.isInfinite() || y.isNaN() || y.isInfinite()) return null
            
            Offset(x, y)
        } catch (e: Exception) {
            // Catch any arithmetic exceptions (division by zero, etc.)
            null
        }
    }
}
