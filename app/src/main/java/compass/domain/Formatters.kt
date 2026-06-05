package compass.domain

object Formatters {
    private val CARDINALS = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

    fun formatHeading(degrees: Float): String = "${degrees.toInt()}°"

    fun cardinalDirection(degrees: Float): String {
        val index = ((degrees + 22.5f) / 45f).toInt() % CARDINALS.size
        return CARDINALS[index]
    }

    fun formatCoordinate(value: Double): String = String.format("%.6f°", value)

    fun formatAltitude(meters: Double): String = String.format("%.0f m", meters)

    fun formatAltitudeDetailed(meters: Double): String = String.format("%.1f m", meters)

    fun formatVelocityKmh(speedMetersPerSecond: Float?): String {
        if (speedMetersPerSecond == null || speedMetersPerSecond < 0f) {
            return "—"
        }
        return String.format("%.1f km/h", speedMetersPerSecond * 3.6f)
    }
}
