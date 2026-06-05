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
    fun cardinalDirectionNorth() {
        assertEquals("N", Formatters.cardinalDirection(0f))
    }

    @Test
    fun cardinalDirectionEast() {
        assertEquals("E", Formatters.cardinalDirection(90f))
    }
}
