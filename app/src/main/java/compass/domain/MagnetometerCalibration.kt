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

    companion object {
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
