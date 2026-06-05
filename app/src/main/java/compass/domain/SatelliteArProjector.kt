package compass.domain

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object SatelliteArProjector {
    /**
     * Builds an east-north-up unit vector from GNSS azimuth (clockwise from north)
     * and elevation above the horizon.
     */
    fun enuFromAzimuthElevation(azimuthDegrees: Float, elevationDegrees: Float): FloatArray {
        val azimuth = Math.toRadians(azimuthDegrees.toDouble())
        val elevation = Math.toRadians(elevationDegrees.coerceIn(0f, 90f).toDouble())
        return floatArrayOf(
            (cos(elevation) * sin(azimuth)).toFloat(),
            (cos(elevation) * cos(azimuth)).toFloat(),
            sin(elevation).toFloat(),
        )
    }

    /** Transforms a world ENU vector into the device frame using Rᵀ. */
    fun worldToDevice(worldVector: FloatArray, rotationMatrix: FloatArray): FloatArray {
        return floatArrayOf(
            rotationMatrix[0] * worldVector[0] + rotationMatrix[3] * worldVector[1] + rotationMatrix[6] * worldVector[2],
            rotationMatrix[1] * worldVector[0] + rotationMatrix[4] * worldVector[1] + rotationMatrix[7] * worldVector[2],
            rotationMatrix[2] * worldVector[0] + rotationMatrix[5] * worldVector[1] + rotationMatrix[8] * worldVector[2],
        )
    }

    /**
     * Projects a satellite into screen space for a portrait back-camera view.
     * The rotation matrix must be in the standard Android device frame (camera looks
     * along device −Z). Returns null when the satellite is behind the camera or
     * outside the field of view.
     */
    fun project(
        satelliteAzimuthDegrees: Float,
        satelliteElevationDegrees: Float,
        rotationMatrix: FloatArray,
        screenWidth: Float,
        screenHeight: Float,
        verticalFovDegrees: Float = 62f,
    ): Offset? {
        if (screenWidth <= 0f || screenHeight <= 0f) return null

        val world = enuFromAzimuthElevation(satelliteAzimuthDegrees, satelliteElevationDegrees)
        val device = worldToDevice(world, rotationMatrix)
        val depth = -device[2]
        if (depth <= 0.05f) return null

        val focalLength = (screenHeight / 2f) / tan(Math.toRadians(verticalFovDegrees / 2.0)).toFloat()
        val x = screenWidth / 2f + (device[0] / depth) * focalLength
        val y = screenHeight / 2f - (device[1] / depth) * focalLength

        if (x < 0f || x > screenWidth || y < 0f || y > screenHeight) return null
        return Offset(x, y)
    }
}
