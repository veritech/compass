package compass.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SatelliteArOverlayEngineTest {

    @Test
    fun returnsMarkersForVisibleSatellites() {
        val engine = SatelliteArOverlayEngine()
        val satellites = listOf(
            SatelliteInfo("GPS", 12, elevationDegrees = 0f, azimuthDegrees = 0f, signalStrengthDbHz = 30f, usedInFix = true),
        )
        val rotationMatrix = floatArrayOf(
            1f, 0f, 0f,
            0f, 0f, -1f,
            0f, 1f, 0f,
        )
        val markers = engine.markers(
            satellites = satellites,
            rotationMatrix = rotationMatrix,
            screenWidth = 400f,
            screenHeight = 800f,
        )

        assertEquals(1, markers.size)
        assertEquals("GPS 12", markers[0].satellite.displayLabel())
        assertTrue(markers[0].position.x in 0f..400f)
        assertTrue(markers[0].position.y in 0f..800f)
    }

    @Test
    fun returnsEmptyListForZeroSizedScreen() {
        val engine = SatelliteArOverlayEngine()

        val markers = engine.markers(
            satellites = listOf(
                SatelliteInfo("GPS", 1, 45f, 0f, 30f, true),
            ),
            rotationMatrix = DeviceOrientation.identityMatrix,
            screenWidth = 0f,
            screenHeight = 800f,
        )

        assertTrue(markers.isEmpty())
    }
}
