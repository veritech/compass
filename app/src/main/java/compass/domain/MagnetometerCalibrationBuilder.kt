package compass.domain

import kotlin.math.max
import kotlin.math.min

/**
 * Collects magnetometer samples while the user moves the phone in a figure-8 so
 * hard-iron offset and soft-iron scale can be estimated.
 */
class MagnetometerCalibrationBuilder(
    private val minSamples: Int = MIN_SAMPLES,
    private val targetAxisRangeUt: Float = TARGET_AXIS_RANGE_UT,
) {
    private var minX = Float.MAX_VALUE
    private var maxX = -Float.MAX_VALUE
    private var minY = Float.MAX_VALUE
    private var maxY = -Float.MAX_VALUE
    private var minZ = Float.MAX_VALUE
    private var maxZ = -Float.MAX_VALUE
    private var sampleCount = 0

    fun reset() {
        minX = Float.MAX_VALUE
        maxX = -Float.MAX_VALUE
        minY = Float.MAX_VALUE
        maxY = -Float.MAX_VALUE
        minZ = Float.MAX_VALUE
        maxZ = -Float.MAX_VALUE
        sampleCount = 0
    }

    fun addSample(x: Float, y: Float, z: Float) {
        minX = min(minX, x)
        maxX = max(maxX, x)
        minY = min(minY, y)
        maxY = max(maxY, y)
        minZ = min(minZ, z)
        maxZ = max(maxZ, z)
        sampleCount++
    }

    fun progress(): Float {
        if (sampleCount == 0) return 0f
        val axisProgress = listOf(
            range(minX, maxX),
            range(minY, maxY),
            range(minZ, maxZ),
        ).map { axisRange -> (axisRange / targetAxisRangeUt).coerceIn(0f, 1f) }
        return axisProgress.average().toFloat()
    }

    fun isComplete(): Boolean = sampleCount >= minSamples && progress() >= 1f

    fun build(): MagnetometerCalibration? {
        if (!isComplete()) return null

        val offsetX = (maxX + minX) / 2f
        val offsetY = (maxY + minY) / 2f
        val offsetZ = (maxZ + minZ) / 2f
        val rangeX = range(minX, maxX)
        val rangeY = range(minY, maxY)
        val rangeZ = range(minZ, maxZ)
        val averageRange = (rangeX + rangeY + rangeZ) / 3f
        if (averageRange < 1e-3f) return null

        return MagnetometerCalibration(
            offsetX = offsetX,
            offsetY = offsetY,
            offsetZ = offsetZ,
            scaleX = rangeX / averageRange,
            scaleY = rangeY / averageRange,
            scaleZ = rangeZ / averageRange,
        )
    }

    private fun range(min: Float, max: Float): Float = max - min

    companion object {
        const val MIN_SAMPLES = 40
        const val TARGET_AXIS_RANGE_UT = 25f
    }
}
