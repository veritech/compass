package compass.domain

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Tilt-compensated compass heading from a device-to-world rotation matrix.
 *
 * When the phone is flat, heading comes from the screen-top (+Y) axis — the same
 * basis as [android.hardware.SensorManager.getOrientation]. When upright, +Y
 * points at the sky (gimbal lock) so heading uses the into-screen (−Z) axis instead.
 */
object CompassHeadingCalculator {
    /**
     * @param rotationMatrix 3×3 row-major matrix mapping device coordinates to world
     *   coordinates (same layout as [android.hardware.SensorManager.getRotationMatrixFromVector]).
     */
    fun headingDegrees(
        rotationMatrix: FloatArray,
        gravity: FloatArray? = null,
    ): Float? {
        if (rotationMatrix.size < 9) return null

        val screenTop = horizontalProjection(rotationMatrix[1], rotationMatrix[4])
        val intoScreen = horizontalProjection(-rotationMatrix[2], -rotationMatrix[5])

        if (gravity != null && gravity.size >= 3) {
            when {
                DeviceTiltDetector.isFlat(gravity) -> return screenTop.asHeading()
                DeviceTiltDetector.isUprightPortrait(gravity) ->
                    return intoScreen.asHeading() ?: screenTop.asHeading()
            }
        }

        val zUp = abs(rotationMatrix[8])
        val yUp = abs(rotationMatrix[7])

        return when {
            zUp > yUp * AXIS_HYSTERESIS_RATIO -> screenTop.asHeading()
            yUp > zUp * AXIS_HYSTERESIS_RATIO -> intoScreen.asHeading() ?: screenTop.asHeading()
            screenTop.weight >= intoScreen.weight -> screenTop.asHeading()
            else -> intoScreen.asHeading() ?: screenTop.asHeading()
        }
    }

    /** True when the screen plane is closer to horizontal than vertical. */
    fun isRelativelyFlat(rotationMatrix: FloatArray): Boolean {
        if (rotationMatrix.size < 9) return true
        val zUp = abs(rotationMatrix[8])
        val yUp = abs(rotationMatrix[7])
        return zUp > yUp * AXIS_HYSTERESIS_RATIO
    }

    private const val AXIS_HYSTERESIS_RATIO = 1.2f

    private data class AxisProjection(val headingDegrees: Float, val weight: Float)

    private fun AxisProjection.asHeading(): Float? =
        headingDegrees.takeIf { weight > 0.01f }

    private fun horizontalProjection(worldEast: Float, worldNorth: Float): AxisProjection {
        val weight = hypot(worldEast, worldNorth)
        if (weight < 1e-6f) {
            return AxisProjection(0f, 0f)
        }
        val azimuthRadians = atan2(worldEast, worldNorth)
        val heading = (
            Math.toDegrees(azimuthRadians.toDouble()).toFloat() + 360f
            ) % 360f
        return AxisProjection(heading, weight)
    }
}
