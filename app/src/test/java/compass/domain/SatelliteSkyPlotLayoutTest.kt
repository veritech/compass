package compass.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SatelliteSkyPlotLayoutTest {

    @Test
    fun zenithSatellitePlotsAtCenter() {
        val position = SatelliteSkyPlotLayout.position(
            azimuthDegrees = 0f,
            elevationDegrees = 90f,
            deviceHeadingDegrees = 0f,
            radius = 100f,
            centerX = 50f,
            centerY = 50f,
        )
        assertEquals(50f, position.x, 0.1f)
        assertEquals(50f, position.y, 0.1f)
    }

    @Test
    fun horizonSatellitePlotsNearEdge() {
        val position = SatelliteSkyPlotLayout.position(
            azimuthDegrees = 0f,
            elevationDegrees = 0f,
            deviceHeadingDegrees = 0f,
            radius = 100f,
            centerX = 50f,
            centerY = 50f,
        )
        val distance = kotlin.math.hypot((position.x - 50f).toDouble(), (position.y - 50f).toDouble())
        assertEquals(100.0, distance, 1.0)
    }

    @Test
    fun rotatesWithDeviceHeading() {
        val northFacing = SatelliteSkyPlotLayout.position(
            azimuthDegrees = 90f,
            elevationDegrees = 45f,
            deviceHeadingDegrees = 0f,
            radius = 100f,
            centerX = 0f,
            centerY = 0f,
        )
        val eastFacing = SatelliteSkyPlotLayout.position(
            azimuthDegrees = 90f,
            elevationDegrees = 45f,
            deviceHeadingDegrees = 90f,
            radius = 100f,
            centerX = 0f,
            centerY = 0f,
        )
        assertTrue(northFacing.x != eastFacing.x || northFacing.y != eastFacing.y)
    }
}
