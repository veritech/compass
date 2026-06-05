package compass.data

import android.hardware.SensorManager
import compass.domain.CompassAzimuth

internal object CompassOrientation {
    fun azimuthDegrees(
        rotationMatrix: FloatArray,
        remappedMatrix: FloatArray,
    ): Float {
        val matrix = if (
            SensorManager.remapCoordinateSystem(
                rotationMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Y,
                remappedMatrix,
            )
        ) {
            remappedMatrix
        } else {
            rotationMatrix
        }
        return CompassAzimuth.fromRotationMatrix(matrix) ?: 0f
    }
}
