package compass.domain

object Formatters {
    fun formatHeading(degrees: Float): String = "${degrees.toInt()}°"

    fun formatCoordinate(value: Double): String = String.format("%.6f°", value)

    fun formatAltitude(meters: Double): String = String.format("%.1f m", meters)

    fun formatVelocityKmh(speedMetersPerSecond: Float?): String {
        if (speedMetersPerSecond == null || speedMetersPerSecond < 0f) {
            return "—"
        }
        return String.format("%.1f km/h", speedMetersPerSecond * 3.6f)
    }
}
