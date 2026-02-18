package com.karnadigital.vyoma.atlas.core.math

import kotlin.math.sqrt

data class Vector3(var x: Float, var y: Float, var z: Float) {

    val length2: Float
        get() = x * x + y * y + z * z

    val length: Float
        get() = sqrt(length2)

    constructor(xyz: FloatArray) : this(xyz[0], xyz[1], xyz[2]) {
        require(xyz.size == 3) { "Trying to create 3 vector from array of length: " + xyz.size }
    }

    fun assign(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun assign(other: Vector3) {
        x = other.x
        y = other.y
        z = other.z
    }

    fun normalize() {
        val norm = length
        if (norm != 0f) {
            x /= norm
            y /= norm
            z /= norm
        }
    }

    operator fun timesAssign(scale: Float) {
        x *= scale
        y *= scale
        z *= scale
    }

    operator fun minusAssign(other: Vector3) {
        x -= other.x
        y -= other.y
        z -= other.z
    }

    infix fun dot(p2: Vector3): Float {
        return x * p2.x + y * p2.y + z * p2.z
    }

    operator fun times(p2: Vector3): Vector3 {
        return Vector3(
            y * p2.z - z * p2.y,
            -x * p2.z + z * p2.x,
            x * p2.y - y * p2.x
        )
    }

    operator fun plus(v2: Vector3): Vector3 {
        return Vector3(x + v2.x, y + v2.y, z + v2.z)
    }

    operator fun minus(v2: Vector3): Vector3 {
        return plus(-v2)
    }

    operator fun times(factor: Float): Vector3 {
        return Vector3(x * factor, y * factor, z * factor)
    }

    operator fun div(factor: Float): Vector3 {
        return this * (1 / factor)
    }

    operator fun unaryMinus(): Vector3 {
        return this * -1f
    }

    fun normalizedCopy(): Vector3 {
        return if (length < 0.000001f) {
            zero()
        } else this / length
    }

    fun projectOnto(unitVector: Vector3): Vector3 {
        return unitVector * (this dot unitVector)
    }

    companion object Factory {
        fun zero() = Vector3(0f, 0f, 0f)
        fun unitX() = Vector3(1f, 0f, 0f)
        fun unitY() = Vector3(0f, 1f, 0f)
        fun unitZ() = Vector3(0f, 0f, 1f)
    }
}
