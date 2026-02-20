package com.karnadigital.vyoma.atlas.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.karnadigital.vyoma.atlas.core.math.Matrix3x3
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrientationSensor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun getSensorUpdates(): Flow<Matrix3x3> = callbackFlow {
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: run {
                close()
                return@callbackFlow
            }

        // Low-Pass Filter State
        val smoothedRotation = FloatArray(9)
        var hasInitialReading = false
        val alpha = 0.1f // Smoothing factor (lower = smoother but more lag)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)
                        
                        if (!hasInitialReading) {
                            System.arraycopy(rotationMatrix, 0, smoothedRotation, 0, 9)
                            hasInitialReading = true
                        } else {
                            // Apply Low-Pass Filter
                            for (i in 0 until 9) {
                                smoothedRotation[i] = smoothedRotation[i] + alpha * (rotationMatrix[i] - smoothedRotation[i])
                            }
                        }
                        
                        val matrix = Matrix3x3(
                           smoothedRotation[0], smoothedRotation[1], smoothedRotation[2],
                           smoothedRotation[3], smoothedRotation[4], smoothedRotation[5],
                           smoothedRotation[6], smoothedRotation[7], smoothedRotation[8]
                        )
                        
                        trySend(matrix)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No-op
            }
        }

        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.conflate()
}
