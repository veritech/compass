package compass.ui

import compass.domain.BreadcrumbPoint
import compass.domain.DeviceOrientation
import compass.domain.SatelliteInfo

data class CompassUiState(
    val headingDegrees: Float = 0f,
    val deviceRotationMatrix: FloatArray = DeviceOrientation.identityMatrix,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val velocityKmh: String = "—",
    val satelliteCount: Int = 0,
    val satellites: List<SatelliteInfo> = emptyList(),
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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompassUiState) return false
        return headingDegrees == other.headingDegrees &&
            deviceRotationMatrix.contentEquals(other.deviceRotationMatrix) &&
            latitude == other.latitude &&
            longitude == other.longitude &&
            altitude == other.altitude &&
            velocityKmh == other.velocityKmh &&
            satelliteCount == other.satelliteCount &&
            satellites == other.satellites &&
            targetLatitudeInput == other.targetLatitudeInput &&
            targetLongitudeInput == other.targetLongitudeInput &&
            targetLatitude == other.targetLatitude &&
            targetLongitude == other.targetLongitude &&
            bearingToTarget == other.bearingToTarget &&
            breadcrumbIntervalSeconds == other.breadcrumbIntervalSeconds &&
            breadcrumbsActive == other.breadcrumbsActive &&
            breadcrumbPoints == other.breadcrumbPoints &&
            loadedTrailPoints == other.loadedTrailPoints &&
            hasLocationPermission == other.hasLocationPermission &&
            hasGpsFix == other.hasGpsFix
    }

    override fun hashCode(): Int {
        var result = headingDegrees.hashCode()
        result = 31 * result + deviceRotationMatrix.contentHashCode()
        result = 31 * result + (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + (altitude?.hashCode() ?: 0)
        result = 31 * result + velocityKmh.hashCode()
        result = 31 * result + satelliteCount
        result = 31 * result + satellites.hashCode()
        result = 31 * result + targetLatitudeInput.hashCode()
        result = 31 * result + targetLongitudeInput.hashCode()
        result = 31 * result + (targetLatitude?.hashCode() ?: 0)
        result = 31 * result + (targetLongitude?.hashCode() ?: 0)
        result = 31 * result + (bearingToTarget?.hashCode() ?: 0)
        result = 31 * result + breadcrumbIntervalSeconds.hashCode()
        result = 31 * result + breadcrumbsActive.hashCode()
        result = 31 * result + breadcrumbPoints.hashCode()
        result = 31 * result + loadedTrailPoints.hashCode()
        result = 31 * result + hasLocationPermission.hashCode()
        result = 31 * result + hasGpsFix.hashCode()
        return result
    }
}
