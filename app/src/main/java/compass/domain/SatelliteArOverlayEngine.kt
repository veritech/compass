package compass.domain

class SatelliteArOverlayEngine(
    private val rotationSmoother: RotationMatrixSmoother = RotationMatrixSmoother(),
    private val positionSmoother: SatelliteArPositionSmoother = SatelliteArPositionSmoother(),
) {
    fun markers(
        satellites: List<SatelliteInfo>,
        rotationMatrix: FloatArray,
        screenWidth: Float,
        screenHeight: Float,
    ): List<SatelliteArMarker> {
        if (screenWidth <= 0f || screenHeight <= 0f) return emptyList()

        val smoothedRotation = rotationSmoother.smooth(rotationMatrix)
        return positionSmoother.update(satellites) { satellite ->
            SatelliteArProjector.project(
                satelliteAzimuthDegrees = satellite.azimuthDegrees,
                satelliteElevationDegrees = satellite.elevationDegrees,
                rotationMatrix = smoothedRotation,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
            )
        }
    }

    fun reset() {
        rotationSmoother.reset()
        positionSmoother.reset()
    }
}
