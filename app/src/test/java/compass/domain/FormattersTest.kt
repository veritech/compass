package compass.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun formatVelocityFromMetersPerSecond() {
        assertEquals("36.0 km/h", Formatters.formatVelocityKmh(10f))
    }

    @Test
    fun formatVelocityMissingWhenNull() {
        assertEquals("—", Formatters.formatVelocityKmh(null))
    }

    @Test
    fun formatCoordinate() {
        assertEquals("51.500000°", Formatters.formatCoordinate(51.5))
    }

    @Test
    fun formatDmsCoordinateNorthAndEast() {
        assertEquals("51°30'00\"N", Formatters.formatDmsCoordinate(51.5, isLatitude = true))
        assertEquals("0°07'12\"E", Formatters.formatDmsCoordinate(0.12, isLatitude = false))
    }

    @Test
    fun formatDmsCoordinateSouthAndWest() {
        assertEquals("33°52'00\"S", Formatters.formatDmsCoordinate(-33.866667, isLatitude = true))
        assertEquals("151°12'35\"W", Formatters.formatDmsCoordinate(-151.209722, isLatitude = false))
    }

    @Test
    fun formatLatLngLineCombinesLatitudeAndLongitude() {
        assertEquals(
            "51°30'00\"N 0°07'12\"E",
            Formatters.formatLatLngLine(51.5, 0.12),
        )
    }

    @Test
    fun formatLatLngLineMissingWhenNull() {
        assertEquals("—", Formatters.formatLatLngLine(null, 0.12))
        assertEquals("—", Formatters.formatLatLngLine(51.5, null))
    }

    @Test
    fun cardinalDirectionNorth() {
        assertEquals("N", Formatters.cardinalDirection(0f))
    }

    @Test
    fun cardinalDirectionEast() {
        assertEquals("E", Formatters.cardinalDirection(90f))
    }
}
