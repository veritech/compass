package compass.domain

import kotlin.math.atan2

/**
 * Azimuth from a device-to-world rotation matrix using the same horizontal
 * projection as [android.hardware.SensorManager.getOrientation].
 */
object CompassAzimuth {
    fun fromRotationMatrix(rotationMatrix: FloatArray): Float? {
        if (rotationMatrix.size < 9) return null
        val azimuthRadians = atan2(rotationMatrix[1].toDouble(), rotationMatrix[4].toDouble())
        return ((Math.toDegrees(azimuthRadians).toFloat() + 360f) % 360f)
    }
}
