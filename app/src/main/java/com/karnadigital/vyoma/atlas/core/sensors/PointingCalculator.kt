package com.karnadigital.vyoma.atlas.core.sensors

import com.karnadigital.vyoma.atlas.core.math.*
import java.util.Date

data class HorizonVectors(
    val north: Vector3,
    val east: Vector3,
    val south: Vector3,
    val west: Vector3,
    val zenith: Vector3
)

class PointingCalculator {

    // North along the ground in celestial coordinates
    private var trueNorthCelestial = Vector3.unitX()
    // Up in celestial coordinates
    private var upCelestial = Vector3.unitY()
    // East in celestial coordinates
    private var trueEastCelestial = Vector3.unitZ()

    private val AXIS_OF_EARTHS_ROTATION = Vector3.unitZ()

    /**
     * Calculates the matrix transforming Phone coordinates to Celestial coordinates.
     */
    fun calculatePointingMatrix(
        rotationMatrix: Matrix3x3, // From sensors
        location: LatLong,
        time: Date
    ): Matrix3x3 {
        // 1. Calculate Local Frame in Phone Coords (Inverse)
        val phoneToLocalMatrix = CoordinateSystem.getPhoneCoordinates(rotationMatrix)

        // 2. Calculate Local Frame in Celestial Coords
        calculateLocalNorthAndUpInCelestialCoords(time, location)
        
        val localToCelestialMatrix = Matrix3x3(
            trueNorthCelestial,
            upCelestial,
            trueEastCelestial
        )

        // 3. Combine: Phone -> Local -> Celestial
        return localToCelestialMatrix * phoneToLocalMatrix
    }

    private fun calculateLocalNorthAndUpInCelestialCoords(time: Date, location: LatLong) {
        val up = calculateRADecOfZenith(time, location)
        upCelestial = getGeocentricCoords(up)
        val z = AXIS_OF_EARTHS_ROTATION
        val zDotu = upCelestial dot z
        trueNorthCelestial = z - upCelestial * zDotu
        trueNorthCelestial.normalize()
        trueEastCelestial = trueNorthCelestial * upCelestial
    }
    
    fun getHorizonVectors(time: Date, location: LatLong): HorizonVectors {
        // Ensure vectors are up to date (though they depend on time/loc, safe to re-calc)
        calculateLocalNorthAndUpInCelestialCoords(time, location)
        return HorizonVectors(
            north = trueNorthCelestial,
            east = trueEastCelestial,
            south = -trueNorthCelestial,
            west = -trueEastCelestial,
            zenith = upCelestial
        )
    }

    private fun getGeocentricCoords(raDec: RaDec): Vector3 {
        val ra = raDec.ra * DEGREES_TO_RADIANS
        val dec = raDec.dec * DEGREES_TO_RADIANS
        return Vector3(
            MathUtils.cos(ra) * MathUtils.cos(dec),
            MathUtils.sin(ra) * MathUtils.cos(dec),
            MathUtils.sin(dec)
        )
    }
}
