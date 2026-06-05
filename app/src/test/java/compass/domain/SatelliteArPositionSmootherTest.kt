package compass.domain

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SatelliteArPositionSmootherTest {

    @Test
    fun firstProjectedPositionPassesThrough() {
        val smoother = SatelliteArPositionSmoother(alpha = 0.2f)
        val satellite = sampleSatellite(id = 12)

        val markers = smoother.update(listOf(satellite)) { Offset(100f, 200f) }

        assertEquals(1, markers.size)
        assertEquals(Offset(100f, 200f), markers[0].position)
        assertEquals("GPS 12", markers[0].satellite.displayLabel())
    }

    @Test
    fun smoothsTowardNewPosition() {
        val smoother = SatelliteArPositionSmoother(alpha = 0.5f)
        val satellite = sampleSatellite(id = 7)

        smoother.update(listOf(satellite)) { Offset(0f, 0f) }
        val markers = smoother.update(listOf(satellite)) { Offset(100f, 100f) }

        assertEquals(Offset(50f, 50f), markers[0].position)
    }

    @Test
    fun removesSatellitesNoLongerVisible() {
        val smoother = SatelliteArPositionSmoother(alpha = 0.5f)
        val visible = sampleSatellite(id = 1)
        val hidden = sampleSatellite(id = 2)

        smoother.update(listOf(visible, hidden)) { Offset(10f, 10f) }
        val markers = smoother.update(listOf(visible)) { Offset(20f, 20f) }

        assertEquals(1, markers.size)
        assertEquals(1, markers[0].satellite.satelliteId)
    }

    @Test
    fun displayLabelIncludesConstellationAndId() {
        val satellite = SatelliteInfo(
            constellation = "Galileo",
            satelliteId = 15,
            elevationDegrees = 45f,
            azimuthDegrees = 90f,
            signalStrengthDbHz = 30f,
            usedInFix = true,
        )

        assertEquals("Galileo 15", satellite.displayLabel())
        assertEquals("Galileo:15", satellite.trackKey())
    }

    private fun sampleSatellite(id: Int) = SatelliteInfo(
        constellation = "GPS",
        satelliteId = id,
        elevationDegrees = 45f,
        azimuthDegrees = 180f,
        signalStrengthDbHz = 28f,
        usedInFix = false,
    )
}
