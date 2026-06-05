package compass.domain

data class SatelliteInfo(
    val constellation: String,
    val satelliteId: Int,
    val elevationDegrees: Float,
    val azimuthDegrees: Float,
    val signalStrengthDbHz: Float,
    val usedInFix: Boolean,
)

data class SatelliteStatus(
    val satellitesInFix: Int,
    val satellites: List<SatelliteInfo>,
)
