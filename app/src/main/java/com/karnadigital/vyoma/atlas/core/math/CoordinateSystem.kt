package com.karnadigital.vyoma.atlas.core.math

import android.hardware.SensorManager

object CoordinateSystem {

    /**
     * Calculates the rotation matrix from the device's sensor data.
     * @param rotationVector The rotation vector from the sensor.
     * @return The 3x3 rotation matrix.
     */
    fun getRotationMatrixFromVector(rotationVector: FloatArray): Matrix3x3 {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
        
        // Convert to our Matrix3x3
        // Row 0: indices 0, 1, 2
        // Row 1: indices 3, 4, 5
        // Row 2: indices 6, 7, 8
        return Matrix3x3(
            rotationMatrix[0], rotationMatrix[1], rotationMatrix[2],
            rotationMatrix[3], rotationMatrix[4], rotationMatrix[5],
            rotationMatrix[6], rotationMatrix[7], rotationMatrix[8]
        )
    }

    /**
     * Calculates the phone's coordinate system (North, East, Up) in the phone's frame
     * using the rotation matrix.
     */
    fun getPhoneCoordinates(rotationMatrix: Matrix3x3): Matrix3x3 {
        // According to Stardroid:
        // magneticNorthPhone = row 1 (indices 3,4,5) ... wait, let's recheck Stardroid's impl.
        // Stardroid says: 
        // magneticNorthPhone = Vector3(rotationMatrix[3], rotationMatrix[4], rotationMatrix[5])
        // upPhone = Vector3(rotationMatrix[6], rotationMatrix[7], rotationMatrix[8])
        // magneticEastPhone = Vector3(rotationMatrix[0], rotationMatrix[1], rotationMatrix[2])
        
        // This means the rows of the rotation matrix (which transforms from World/Earth to Phone)
        // are the basis vectors of the Earth frame expressed in the Phone frame.
        // Or vice versa?
        // SensorManager.getRotationMatrixFromVector returns a matrix R such that
        // v_world = R * v_device. 
        // So columns of R are the device axes in world coordinates.
        // Rows of R are the world axes in device coordinates.
        // Row 0 = East
        // Row 1 = North
        // Row 2 = Up
        
        // So yes, Stardroid is correct.
        
        val east = Vector3(rotationMatrix.xx, rotationMatrix.xy, rotationMatrix.xz)
        val north = Vector3(rotationMatrix.yx, rotationMatrix.yy, rotationMatrix.yz)
        val up = Vector3(rotationMatrix.zx, rotationMatrix.zy, rotationMatrix.zz)
        
        return Matrix3x3(north, up, east, false) // Row vectors
    }
}
