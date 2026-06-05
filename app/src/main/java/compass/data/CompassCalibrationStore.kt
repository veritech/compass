package compass.data

import android.content.Context
import compass.domain.MagnetometerCalibration

class CompassCalibrationStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(calibration: MagnetometerCalibration) {
        preferences.edit()
            .putFloat(KEY_OFFSET_X, calibration.offsetX)
            .putFloat(KEY_OFFSET_Y, calibration.offsetY)
            .putFloat(KEY_OFFSET_Z, calibration.offsetZ)
            .putFloat(KEY_SCALE_X, calibration.scaleX)
            .putFloat(KEY_SCALE_Y, calibration.scaleY)
            .putFloat(KEY_SCALE_Z, calibration.scaleZ)
            .putBoolean(KEY_HAS_CALIBRATION, true)
            .apply()
    }

    fun load(): MagnetometerCalibration? {
        if (!preferences.getBoolean(KEY_HAS_CALIBRATION, false)) return null
        return MagnetometerCalibration(
            offsetX = preferences.getFloat(KEY_OFFSET_X, 0f),
            offsetY = preferences.getFloat(KEY_OFFSET_Y, 0f),
            offsetZ = preferences.getFloat(KEY_OFFSET_Z, 0f),
            scaleX = preferences.getFloat(KEY_SCALE_X, 1f),
            scaleY = preferences.getFloat(KEY_SCALE_Y, 1f),
            scaleZ = preferences.getFloat(KEY_SCALE_Z, 1f),
        )
    }

    fun hasCalibration(): Boolean = preferences.getBoolean(KEY_HAS_CALIBRATION, false)

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "compass_calibration"
        private const val KEY_HAS_CALIBRATION = "has_calibration"
        private const val KEY_OFFSET_X = "offset_x"
        private const val KEY_OFFSET_Y = "offset_y"
        private const val KEY_OFFSET_Z = "offset_z"
        private const val KEY_SCALE_X = "scale_x"
        private const val KEY_SCALE_Y = "scale_y"
        private const val KEY_SCALE_Z = "scale_z"
    }
}
