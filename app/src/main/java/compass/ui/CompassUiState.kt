package compass.ui

data class CompassUiState(
    val headingDegrees: Float = 0f,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val velocityKmh: String = "—",
    val satelliteCount: Int = 0,
    val targetLatitudeInput: String = "",
    val targetLongitudeInput: String = "",
    val bearingToTarget: Double? = null,
    val breadcrumbIntervalSeconds: String = "60",
    val breadcrumbsActive: Boolean = false,
    val breadcrumbCount: Int = 0,
    val gpxExport: String? = null,
    val hasLocationPermission: Boolean = false,
    val hasGpsFix: Boolean = false,
)
