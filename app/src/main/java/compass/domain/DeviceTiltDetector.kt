package compass.domain

import kotlin.math.hypot

/**
 * Detects device pose from raw accelerometer (gravity) readings.
 *
 * [android.hardware.Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR] does not fuse the
 * accelerometer, so its tilt estimate is unreliable when the phone lies flat.
 * Gravity-based flat detection lets us fall back to accelerometer + magnetometer
 * fusion for compass heading in that pose.
 */
object DeviceTiltDetector {
    /**
     * True when the screen plane is roughly horizontal (phone lying on a table).
     * Gravity then aligns with the screen normal (device Z).
     */
    fun isFlat(gravity: FloatArray, flatThreshold: Float = DEFAULT_FLAT_THRESHOLD): Boolean {
        if (gravity.size < 3) return false
        val magnitude = hypot(gravity[0].toDouble(), hypot(gravity[1].toDouble(), gravity[2].toDouble()))
            .toFloat()
        if (magnitude < 1e-3f) return false
        return kotlin.math.abs(gravity[2]) / magnitude >= flatThreshold
    }

    const val DEFAULT_FLAT_THRESHOLD = 0.85f
}
