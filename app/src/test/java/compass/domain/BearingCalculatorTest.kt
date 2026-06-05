package compass.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BearingCalculatorTest {

    @Test
    fun bearingDueNorth() {
        val bearing = BearingCalculator.bearingDegrees(
            fromLatitude = 0.0,
            fromLongitude = 0.0,
            toLatitude = 1.0,
            toLongitude = 0.0,
        )
        assertEquals(0.0, bearing, 0.5)
    }

    @Test
    fun bearingDueEast() {
        val bearing = BearingCalculator.bearingDegrees(
            fromLatitude = 0.0,
            fromLongitude = 0.0,
            toLatitude = 0.0,
            toLongitude = 1.0,
        )
        assertEquals(90.0, bearing, 0.5)
    }

    @Test
    fun bearingWrapsToPositiveDegrees() {
        val bearing = BearingCalculator.bearingDegrees(
            fromLatitude = 51.5,
            fromLongitude = -0.1,
            toLatitude = 51.4,
            toLongitude = -0.2,
        )
        assert(bearing in 0.0..360.0)
    }
}
