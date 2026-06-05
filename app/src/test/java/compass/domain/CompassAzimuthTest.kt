package compass.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CompassAzimuthTest {

    @Test
    fun northWhenTopPointsNorth() {
        val matrix = floatArrayOf(
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f,
        )
        assertEquals(0f, CompassAzimuth.fromRotationMatrix(matrix)!!, 0.5f)
    }

    @Test
    fun eastWhenTopPointsEast() {
        val matrix = floatArrayOf(
            0f, 1f, 0f,
            -1f, 0f, 0f,
            0f, 0f, 1f,
        )
        assertEquals(90f, CompassAzimuth.fromRotationMatrix(matrix)!!, 0.5f)
    }

    @Test
    fun returnsNullForInvalidMatrix() {
        assertNull(CompassAzimuth.fromRotationMatrix(FloatArray(3)))
    }
}
