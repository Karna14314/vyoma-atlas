package com.karnadigital.vyoma.atlas.core.math

import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.tan
import kotlin.math.asin
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.abs

const val PI_FLOAT = PI.toFloat()
const val TWO_PI = 2f * PI_FLOAT
const val DEGREES_TO_RADIANS = PI_FLOAT / 180.0f
const val RADIANS_TO_DEGREES = 180.0f / PI_FLOAT

fun flooredMod(a: Float, n: Float) = (if (a < 0) a % n + n else a) % n

fun mod2pi(x: Float) = positiveMod(x, TWO_PI)

fun positiveMod(x: Float, y: Float): Float {
    var remainder = x % y
    if (remainder < 0) remainder += y
    return remainder
}

fun positiveMod(x: Double, y: Double): Double {
    var remainder = x % y
    if (remainder < 0) remainder += y
    return remainder
}

object MathUtils {
    fun abs(x: Float) = kotlin.math.abs(x)
    fun sqrt(x: Float) = kotlin.math.sqrt(x)
    fun sin(x: Float) = kotlin.math.sin(x)
    fun cos(x: Float) = kotlin.math.cos(x)
    fun tan(x: Float) = kotlin.math.tan(x)
    fun asin(x: Float) = kotlin.math.asin(x)
    fun acos(x: Float) = kotlin.math.acos(x)
    fun atan2(y: Float, x: Float) = kotlin.math.atan2(y, x)
}
