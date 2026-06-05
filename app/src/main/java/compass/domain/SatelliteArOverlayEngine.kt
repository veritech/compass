package compass.domain

class SatelliteArOverlayEngine {
    fun markers(
        satellites: List<SatelliteInfo>,
        rotationMatrix: FloatArray,
        screenWidth: Float,
        screenHeight: Float,
    ): List<SatelliteArMarker> {
        if (screenWidth <= 0f || screenHeight <= 0f) return emptyList()

        return satellites.mapNotNull { satellite ->
            val position = SatelliteArProjector.project(
                satelliteAzimuthDegrees = satellite.azimuthDegrees,
                satelliteElevationDegrees = satellite.elevationDegrees,
                rotationMatrix = rotationMatrix,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
            ) ?: return@mapNotNull null

            SatelliteArMarker(satellite = satellite, position = position)
        }
    }
}
