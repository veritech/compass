package compass.domain

import kotlin.math.sqrt

/**
 * Optional light smoothing for AR orientation. Uses quaternion interpolation so
 * rotations stay rigid. Defaults to a high alpha so motion stays responsive.
 */
class QuaternionOrientationSmoother(
    private val alpha: Float = DEFAULT_ALPHA,
) {
    private var currentQuaternion: FloatArray? = null

    fun smoothRotationMatrix(rotationMatrix: FloatArray): FloatArray {
        val incoming = rotationMatrixToQuaternion(rotationMatrix)
        val smoothed = if (currentQuaternion == null) {
            incoming
        } else {
            QuaternionMath.nlerp(currentQuaternion!!, incoming, alpha)
        }
        currentQuaternion = smoothed
        return QuaternionMath.toRotationMatrix(smoothed)
    }

    fun smoothRotationVector(rotationVector: FloatArray): FloatArray {
        val incoming = QuaternionMath.fromRotationVector(rotationVector)
        val smoothed = if (currentQuaternion == null) {
            incoming
        } else {
            QuaternionMath.nlerp(currentQuaternion!!, incoming, alpha)
        }
        currentQuaternion = smoothed
        return QuaternionMath.toRotationMatrix(smoothed)
    }

    fun reset() {
        currentQuaternion = null
    }

    private fun rotationMatrixToQuaternion(matrix: FloatArray): FloatArray {
        val trace = matrix[0] + matrix[4] + matrix[8]
        return if (trace > 0f) {
            val s = sqrt(trace + 1f) * 2f
            floatArrayOf(
                0.25f * s,
                (matrix[7] - matrix[5]) / s,
                (matrix[2] - matrix[6]) / s,
                (matrix[3] - matrix[1]) / s,
            )
        } else if (matrix[0] > matrix[4] && matrix[0] > matrix[8]) {
            val s = sqrt(1f + matrix[0] - matrix[4] - matrix[8]) * 2f
            floatArrayOf(
                (matrix[7] - matrix[5]) / s,
                0.25f * s,
                (matrix[1] + matrix[3]) / s,
                (matrix[2] + matrix[6]) / s,
            )
        } else if (matrix[4] > matrix[8]) {
            val s = sqrt(1f + matrix[4] - matrix[0] - matrix[8]) * 2f
            floatArrayOf(
                (matrix[2] - matrix[6]) / s,
                (matrix[1] + matrix[3]) / s,
                0.25f * s,
                (matrix[5] + matrix[7]) / s,
            )
        } else {
            val s = sqrt(1f + matrix[8] - matrix[0] - matrix[4]) * 2f
            floatArrayOf(
                (matrix[3] - matrix[1]) / s,
                (matrix[2] + matrix[6]) / s,
                (matrix[5] + matrix[7]) / s,
                0.25f * s,
            )
        }
    }

    companion object {
        const val DEFAULT_ALPHA = 0.55f
    }
}
