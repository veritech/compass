package compass.domain

/**
 * Hard- and soft-iron correction for raw magnetometer readings collected during
 * a figure-8 calibration motion.
 */
data class MagnetometerCalibration(
    val offsetX: Float,
    val offsetY: Float,
    val offsetZ: Float,
    val scaleX: Float,
    val scaleY: Float,
    val scaleZ: Float,
) {
    fun apply(raw: FloatArray): FloatArray {
        require(raw.size >= 3)
        return floatArrayOf(
            (raw[0] - offsetX) / scaleX,
            (raw[1] - offsetY) / scaleY,
            (raw[2] - offsetZ) / scaleZ,
        )
    }

    fun isPlausible(): Boolean {
        if (!scaleX.isFinite() || !scaleY.isFinite() || !scaleZ.isFinite()) return false
        if (scaleX !in MIN_SCALE..MAX_SCALE) return false
        if (scaleY !in MIN_SCALE..MAX_SCALE) return false
        if (scaleZ !in MIN_SCALE..MAX_SCALE) return false
        val offsetMagnitude = kotlin.math.hypot(
            offsetX.toDouble(),
            kotlin.math.hypot(offsetY.toDouble(), offsetZ.toDouble()),
        ).toFloat()
        return offsetMagnitude <= MAX_OFFSET_UT
    }

    companion object {
        const val MIN_SCALE = 0.35f
        const val MAX_SCALE = 2.75f
        const val MAX_OFFSET_UT = 120f
        val NONE = MagnetometerCalibration(
            offsetX = 0f,
            offsetY = 0f,
            offsetZ = 0f,
            scaleX = 1f,
            scaleY = 1f,
            scaleZ = 1f,
        )
    }
}
