package compass.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class QuaternionOrientationSmootherTest {

    @Test
    fun smoothRotationMatrixStaysCloseToIncomingSample() {
        val smoother = QuaternionOrientationSmoother(alpha = 0.6f)
        val target = floatArrayOf(
            0f, 1f, 0f,
            -1f, 0f, 0f,
            0f, 0f, 1f,
        )

        smoother.smoothRotationMatrix(DeviceOrientation.identityMatrix)
        val smoothed = smoother.smoothRotationMatrix(target)

        assertTrue(smoothed[1] > 0.4f)
        assertTrue(smoothed[3] < -0.4f)
    }
}
