package compass.provider

interface CompassCalibrationListener {
    fun onCalibrationProgress(progress: Float)

    fun onCalibrationComplete(success: Boolean)
}
