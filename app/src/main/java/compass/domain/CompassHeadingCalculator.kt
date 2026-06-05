package compass.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Tilt-compensated compass heading from a device-to-world rotation matrix.
 *
 * Android's [android.hardware.SensorManager.getOrientation] assumes a mostly
 * flat device. When the phone is held upright the screen-top (+Y) axis points
 * at the sky and azimuth becomes unstable (gimbal lock). This calculator
 * fuses projections of multiple device axes onto the horizontal plane, weighted
 * by how strongly each axis lies in that plane.
 */
object CompassHeadingCalculator {
    /**
     * @param rotationMatrix 3×3 row-major matrix mapping device coordinates to world
     *   coordinates (same layout as [android.hardware.SensorManager.getRotationMatrixFromVector]).
     */
    fun headingDegrees(rotationMatrix: FloatArray): Float? {
        if (rotationMatrix.size < 9) return null

        val screenTop = horizontalProjection(rotationMatrix[1], rotationMatrix[4])
        val intoScreen = horizontalProjection(-rotationMatrix[2], -rotationMatrix[5])
        val rightEdge = horizontalProjection(rotationMatrix[0], rotationMatrix[3])

        val candidates = listOf(screenTop, intoScreen)
            .filter { it.weight > 0.01f }
            .ifEmpty { listOf(rightEdge).filter { it.weight > 0.01f } }

        if (candidates.isEmpty()) return null
        return weightedCircularMean(candidates)
    }

    private data class AxisProjection(val headingDegrees: Float, val weight: Float)

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

    private fun weightedCircularMean(candidates: List<AxisProjection>): Float {
        var sumSin = 0f
        var sumCos = 0f
        for (candidate in candidates) {
            val radians = Math.toRadians(candidate.headingDegrees.toDouble())
            sumSin += candidate.weight * sin(radians).toFloat()
            sumCos += candidate.weight * cos(radians).toFloat()
        }
        val meanRadians = atan2(sumSin, sumCos)
        return (Math.toDegrees(meanRadians.toDouble()).toFloat() + 360f) % 360f
    }
}
