package com.karnadigital.vyoma.atlas.core.math

import kotlin.math.abs

data class Matrix3x3(
    var xx: Float, var xy: Float, var xz: Float,
    var yx: Float, var yy: Float, var yz: Float,
    var zx: Float, var zy: Float, var zz: Float
) {

    constructor(v1: Vector3, v2: Vector3, v3: Vector3, columnVectors: Boolean = true) : this(
        0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f
    ) {
        if (columnVectors) {
            xx = v1.x; yx = v1.y; zx = v1.z
            xy = v2.x; yy = v2.y; zy = v2.z
            xz = v3.x; yz = v3.y; zz = v3.z
        } else {
            xx = v1.x; xy = v1.y; xz = v1.z
            yx = v2.x; yy = v2.y; yz = v2.z
            zx = v3.x; zy = v3.y; zz = v3.z
        }
    }

    val determinant: Float
        get() = xx * yy * zz + xy * yz * zx + xz * yx * zy - xx * yz * zy - yy * zx * xz - zz * xy * yx

    val inverse: Matrix3x3?
        get() {
            return if (abs(determinant) < 0.00001f) null else Matrix3x3(
                (yy * zz - yz * zy) / determinant,
                (xz * zy - xy * zz) / determinant,
                (xy * yz - xz * yy) / determinant,
                (yz * zx - yx * zz) / determinant,
                (xx * zz - xz * zx) / determinant,
                (xz * yx - xx * yz) / determinant,
                (yx * zy - yy * zx) / determinant,
                (xy * zx - xx * zy) / determinant,
                (xx * yy - xy * yx) / determinant
            )
        }

    fun transpose() {
        var tmp = xy; xy = yx; yx = tmp
        tmp = xz; xz = zx; zx = tmp
        tmp = yz; yz = zy; zy = tmp
    }

    operator fun times(m: Matrix3x3) = Matrix3x3(
        this.xx * m.xx + this.xy * m.yx + this.xz * m.zx,
        this.xx * m.xy + this.xy * m.yy + this.xz * m.zy,
        this.xx * m.xz + this.xy * m.yz + this.xz * m.zz,
        this.yx * m.xx + this.yy * m.yx + this.yz * m.zx,
        this.yx * m.xy + this.yy * m.yy + this.yz * m.zy,
        this.yx * m.xz + this.yy * m.yz + this.yz * m.zz,
        this.zx * m.xx + this.zy * m.yx + this.zz * m.zx,
        this.zx * m.xy + this.zy * m.yy + this.zz * m.zy,
        this.zx * m.xz + this.zy * m.yz + this.zz * m.zz
    )

    operator fun times(v: Vector3) = Vector3(
        this.xx * v.x + this.xy * v.y + this.xz * v.z,
        this.yx * v.x + this.yy * v.y + this.yz * v.z,
        this.zx * v.x + this.zy * v.y + this.zz * v.z
    )

    companion object {
        val identity = Matrix3x3(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
    }
}
