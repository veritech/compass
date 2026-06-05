package compass.domain

data class SatelliteInfo(
    val constellation: String,
    val satelliteId: Int,
    val elevationDegrees: Float,
    val azimuthDegrees: Float,
    val signalStrengthDbHz: Float,
    val usedInFix: Boolean,
) {
    fun trackKey(): String = "$constellation:$satelliteId"

    /** Human-readable label from constellation type and space-vehicle id. */
    fun displayLabel(): String = "$constellation $satelliteId"
}

data class SatelliteStatus(
    val satellitesInFix: Int,
    val satellites: List<SatelliteInfo>,
)
