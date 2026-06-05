package compass.ui

import compass.domain.BreadcrumbPoint
import compass.domain.SatelliteInfo

data class CompassUiState(
    val headingDegrees: Float = 0f,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val velocityKmh: String = "—",
    val satelliteCount: Int = 0,
    val satellites: List<SatelliteInfo> = emptyList(),
    val showSatelliteSkyPlot: Boolean = false,
    val targetLatitudeInput: String = "",
    val targetLongitudeInput: String = "",
    val targetLatitude: Double? = null,
    val targetLongitude: Double? = null,
    val bearingToTarget: Double? = null,
    val breadcrumbIntervalSeconds: String = "60",
    val breadcrumbsActive: Boolean = false,
    val breadcrumbPoints: List<BreadcrumbPoint> = emptyList(),
    val loadedTrailPoints: List<BreadcrumbPoint> = emptyList(),
    val hasLocationPermission: Boolean = false,
    val hasGpsFix: Boolean = false,
)
