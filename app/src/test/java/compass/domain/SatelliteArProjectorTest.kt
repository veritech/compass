package compass.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SatelliteArProjectorTest {

    @Test
    fun projectsSatelliteInFrontOfCamera() {
        // Phone pitched so the horizon (north) sits in the center of the camera view.
        val rotationMatrix = floatArrayOf(
            1f, 0f, 0f,
            0f, 0f, -1f,
            0f, 1f, 0f,
        )
        val position = SatelliteArProjector.project(
            satelliteAzimuthDegrees = 0f,
            satelliteElevationDegrees = 0f,
            rotationMatrix = rotationMatrix,
            screenWidth = 400f,
            screenHeight = 800f,
        )

        assertNotNull(position)
        assertEquals(200f, position!!.x, 1f)
        assertEquals(400f, position.y, 1f)
    }

    @Test
    fun returnsNullWhenSatelliteIsBehindCamera() {
        val rotationMatrix = DeviceOrientation.identityMatrix
        val position = SatelliteArProjector.project(
            satelliteAzimuthDegrees = 0f,
            satelliteElevationDegrees = 90f,
            rotationMatrix = rotationMatrix,
            screenWidth = 400f,
            screenHeight = 800f,
        )

        assertNull(position)
    }
}
