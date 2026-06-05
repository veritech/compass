package compass.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Low-pass filter for compass headings using circular (sin/cos) averaging
 * so smoothing works correctly across the 0°/360° boundary.
 */
class HeadingSmoother(
    private val alpha: Float = DEFAULT_ALPHA,
    private val stationaryAlpha: Float = STATIONARY_ALPHA,
    private val stationaryThresholdDegrees: Float = STATIONARY_THRESHOLD_DEGREES,
    private val outputDeadbandDegrees: Float = OUTPUT_DEADBAND_DEGREES,
) {
    private var initialized = false
    private var smoothedSin = 0f
    private var smoothedCos = 0f
    private var lastRawHeading: Float? = null
    private var lastOutputHeading: Float? = null

    fun smooth(degrees: Float): Float {
        val normalized = ((degrees % 360f) + 360f) % 360f
        val effectiveAlpha = effectiveAlpha(normalized)
        val radians = Math.toRadians(normalized.toDouble())
        val sinValue = sin(radians).toFloat()
        val cosValue = cos(radians).toFloat()

        if (!initialized) {
            smoothedSin = sinValue
            smoothedCos = cosValue
            initialized = true
        } else {
            smoothedSin = effectiveAlpha * sinValue + (1f - effectiveAlpha) * smoothedSin
            smoothedCos = effectiveAlpha * cosValue + (1f - effectiveAlpha) * smoothedCos
        }

        lastRawHeading = normalized

        val smoothedDegrees = Math.toDegrees(
            atan2(smoothedSin.toDouble(), smoothedCos.toDouble()),
        ).toFloat()
        val output = ((smoothedDegrees + 360f) % 360f)

        val held = lastOutputHeading
        if (
            held != null &&
            angularDistance(output, held) < outputDeadbandDegrees
        ) {
            return held
        }

        lastOutputHeading = output
        return output
    }

    fun reset() {
        initialized = false
        smoothedSin = 0f
        smoothedCos = 0f
        lastRawHeading = null
        lastOutputHeading = null
    }

    private fun effectiveAlpha(degrees: Float): Float {
        val previous = lastRawHeading ?: return alpha
        return if (angularDistance(degrees, previous) < stationaryThresholdDegrees) {
            stationaryAlpha
        } else {
            alpha
        }
    }

    companion object {
        const val DEFAULT_ALPHA = 0.08f
        const val STATIONARY_ALPHA = 0.04f
        const val STATIONARY_THRESHOLD_DEGREES = 2.5f
        const val OUTPUT_DEADBAND_DEGREES = 0.75f

        fun angularDistance(fromDegrees: Float, toDegrees: Float): Float {
            val delta = ((toDegrees - fromDegrees + 540f) % 360f) - 180f
            return kotlin.math.abs(delta)
        }
    }
}
