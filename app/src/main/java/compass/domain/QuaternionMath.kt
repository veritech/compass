package compass.domain

import kotlin.math.sqrt

object QuaternionMath {
    /**
     * Converts an Android rotation vector sensor reading to a unit quaternion [w, x, y, z].
     */
    fun fromRotationVector(rotationVector: FloatArray): FloatArray {
        val x = rotationVector[0]
        val y = rotationVector[1]
        val z = rotationVector[2]
        val w = if (rotationVector.size > 3) {
            rotationVector[3]
        } else {
            val magnitudeSquared = 1f - (x * x + y * y + z * z)
            if (magnitudeSquared > 0f) sqrt(magnitudeSquared) else 0f
        }
        return normalize(floatArrayOf(w, x, y, z))
    }

    fun toRotationMatrix(quaternion: FloatArray): FloatArray {
        val w = quaternion[0]
        val x = quaternion[1]
        val y = quaternion[2]
        val z = quaternion[3]
        return floatArrayOf(
            1f - 2f * (y * y + z * z), 2f * (x * y - z * w), 2f * (x * z + y * w),
            2f * (x * y + z * w), 1f - 2f * (x * x + z * z), 2f * (y * z - x * w),
            2f * (x * z - y * w), 2f * (y * z + x * w), 1f - 2f * (x * x + y * y),
        )
    }

    /** Normalized linear interpolation — fast approximation of slerp for small steps. */
    fun nlerp(from: FloatArray, to: FloatArray, alpha: Float): FloatArray {
        var dot = from[0] * to[0] + from[1] * to[1] + from[2] * to[2] + from[3] * to[3]
        val target = if (dot < 0f) {
            dot = -dot
            floatArrayOf(-to[0], -to[1], -to[2], -to[3])
        } else {
            to
        }
        return normalize(
            floatArrayOf(
                from[0] + alpha * (target[0] - from[0]),
                from[1] + alpha * (target[1] - from[1]),
                from[2] + alpha * (target[2] - from[2]),
                from[3] + alpha * (target[3] - from[3]),
            ),
        )
    }

    private fun normalize(quaternion: FloatArray): FloatArray {
        val length = sqrt(
            quaternion[0] * quaternion[0] +
                quaternion[1] * quaternion[1] +
                quaternion[2] * quaternion[2] +
                quaternion[3] * quaternion[3],
        )
        if (length < 1e-6f) {
            return floatArrayOf(1f, 0f, 0f, 0f)
        }
        return floatArrayOf(
            quaternion[0] / length,
            quaternion[1] / length,
            quaternion[2] / length,
            quaternion[3] / length,
        )
    }
}
