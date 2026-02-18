package com.karnadigital.vyoma.atlas.core.math

import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.sqrt

data class RaDec(
    var ra: Float, // In degrees
    var dec: Float // In degrees
) {
    companion object {
        fun fromGeocentricCoords(coords: Vector3): RaDec {
            // find the RA and DEC from the rectangular equatorial coords
            val ra = mod2pi(atan2(coords.y, coords.x)) * RADIANS_TO_DEGREES
            val dec = (atan(coords.z / sqrt(coords.x * coords.x + coords.y * coords.y)) * RADIANS_TO_DEGREES)
            return RaDec(ra, dec)
        }
    }
}
