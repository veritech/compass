package compass.domain

object Formatters {
    private val CARDINALS = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

    fun formatHeading(degrees: Float): String = "${degrees.toInt()}°"

    fun cardinalDirection(degrees: Float): String {
        val index = ((degrees + 22.5f) / 45f).toInt() % CARDINALS.size
        return CARDINALS[index]
    }

    fun formatCoordinate(value: Double): String = String.format("%.6f°", value)

    fun formatLatLngLine(latitude: Double?, longitude: Double?): String {
        if (latitude == null || longitude == null) return "—"
        val lat = formatDmsCoordinate(latitude, isLatitude = true)
        val lng = formatDmsCoordinate(longitude, isLatitude = false)
        return "$lat\u00A0$lng"
    }

    fun formatDmsCoordinate(value: Double, isLatitude: Boolean): String {
        val hemisphere = when {
            isLatitude -> if (value >= 0) "N" else "S"
            else -> if (value >= 0) "E" else "W"
        }
        val limit = if (isLatitude) 90.0 else 180.0
        var absolute = kotlin.math.abs(value).coerceAtMost(limit)
        var degrees = absolute.toInt()
        var minutesFull = (absolute - degrees) * 60.0
        var minutes = minutesFull.toInt()
        var seconds = kotlin.math.round((minutesFull - minutes) * 60.0).toInt()
        if (seconds == 60) {
            seconds = 0
            minutes += 1
        }
        if (minutes == 60) {
            minutes = 0
            degrees += 1
        }
        return String.format("%d°%02d'%02d\"%s", degrees, minutes, seconds, hemisphere)
    }

    fun formatAltitude(meters: Double): String = String.format("%.0f m", meters)

    fun formatAltitudeDetailed(meters: Double): String = String.format("%.1f m", meters)

    fun formatVelocityKmh(speedMetersPerSecond: Float?): String {
        if (speedMetersPerSecond == null || speedMetersPerSecond < 0f) {
            return "—"
        }
        return String.format("%.1f km/h", speedMetersPerSecond * 3.6f)
    }
}
