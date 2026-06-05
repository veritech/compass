package compass.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Low-pass filter for compass headings using circular (sin/cos) averaging
 * so smoothing works correctly across the 0°/360° boundary.
 */
class HeadingSmoother(
    private val alpha: Float = DEFAULT_ALPHA,
) {
    private var initialized = false
    private var smoothedSin = 0f
    private var smoothedCos = 0f

    fun smooth(degrees: Float): Float {
        val radians = Math.toRadians(degrees.toDouble())
        val sinValue = sin(radians).toFloat()
        val cosValue = cos(radians).toFloat()

        if (!initialized) {
            smoothedSin = sinValue
            smoothedCos = cosValue
            initialized = true
        } else {
            smoothedSin = alpha * sinValue + (1f - alpha) * smoothedSin
            smoothedCos = alpha * cosValue + (1f - alpha) * smoothedCos
        }

        val smoothedDegrees = Math.toDegrees(
            atan2(smoothedSin.toDouble(), smoothedCos.toDouble()),
        ).toFloat()
        return ((smoothedDegrees + 360f) % 360f).roundToInt().toFloat()
    }

    fun reset() {
        initialized = false
        smoothedSin = 0f
        smoothedCos = 0f
    }

    companion object {
        const val DEFAULT_ALPHA = 0.12f
    }
}
