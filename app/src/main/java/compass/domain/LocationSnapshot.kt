package compass.domain

data class LocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speedMetersPerSecond: Float?,
    val hasFix: Boolean,
)

data class BreadcrumbPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val timestampMillis: Long,
)
