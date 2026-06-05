package compass.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RotationMatrixSmootherTest {

    @Test
    fun firstSamplePassesThrough() {
        val smoother = RotationMatrixSmoother(alpha = 0.2f)
        val matrix = DeviceOrientation.identityMatrix

        val smoothed = smoother.smooth(matrix)

        assertEquals(matrix.toList(), smoothed.toList())
    }

    @Test
    fun smoothsTowardNewMatrix() {
        val smoother = RotationMatrixSmoother(alpha = 0.5f)
        smoother.smooth(DeviceOrientation.identityMatrix)

        val target = floatArrayOf(
            0f, 1f, 0f,
            -1f, 0f, 0f,
            0f, 0f, 1f,
        )
        val smoothed = smoother.smooth(target)

        assertTrue(smoothed[1] > 0f)
        assertTrue(smoothed[3] < 0f)
    }

    @Test
    fun resetStartsFresh() {
        val smoother = RotationMatrixSmoother(alpha = 0.1f)
        smoother.smooth(DeviceOrientation.identityMatrix)
        smoother.reset()

        val target = floatArrayOf(
            0f, 1f, 0f,
            -1f, 0f, 0f,
            0f, 0f, 1f,
        )
        val smoothed = smoother.smooth(target)

        assertEquals(target.toList(), smoothed.toList())
    }
}
