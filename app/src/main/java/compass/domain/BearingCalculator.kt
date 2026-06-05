package compass.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object BearingCalculator {
    fun bearingDegrees(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double,
    ): Double {
        val lat1 = Math.toRadians(fromLatitude)
        val lat2 = Math.toRadians(toLatitude)
        val deltaLongitude = Math.toRadians(toLongitude - fromLongitude)

        val y = sin(deltaLongitude) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLongitude)
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }
}
