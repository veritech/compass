package compass.domain

import kotlin.math.hypot

/**
 * Low-pass filter for device rotation matrices used by satellite AR.
 * Blends toward each new sample then re-normalizes the column vectors.
 */
class RotationMatrixSmoother(
    private val alpha: Float = DEFAULT_ALPHA,
) {
    private var current: FloatArray? = null

    fun smooth(matrix: FloatArray): FloatArray {
        require(matrix.size >= 9) { "Rotation matrix must have at least 9 elements." }

        if (current == null) {
            current = matrix.copyOf()
            return matrix.copyOf()
        }

        val blended = FloatArray(9) { index ->
            alpha * matrix[index] + (1f - alpha) * current!![index]
        }
        normalizeColumns(blended)
        current = blended.copyOf()
        return blended.copyOf()
    }

    fun reset() {
        current = null
    }

    private fun normalizeColumns(matrix: FloatArray) {
        for (column in 0..2) {
            val x = matrix[column]
            val y = matrix[column + 3]
            val z = matrix[column + 6]
            val length = hypot(x, hypot(y, z))
            if (length < 1e-6f) continue
            matrix[column] = x / length
            matrix[column + 3] = y / length
            matrix[column + 6] = z / length
        }
    }

    companion object {
        const val DEFAULT_ALPHA = 0.14f
    }
}
