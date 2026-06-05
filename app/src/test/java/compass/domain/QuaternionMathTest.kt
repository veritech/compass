package compass.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuaternionMathTest {

    @Test
    fun fromRotationVectorAddsScalarComponentWhenMissing() {
        val quaternion = QuaternionMath.fromRotationVector(floatArrayOf(0f, 0f, 0f))

        assertEquals(1f, quaternion[0], 0.01f)
        assertEquals(0f, quaternion[1], 0.01f)
    }

    @Test
    fun toRotationMatrixPreservesIdentityQuaternion() {
        val matrix = QuaternionMath.toRotationMatrix(floatArrayOf(1f, 0f, 0f, 0f))

        assertEquals(DeviceOrientation.identityMatrix.toList(), matrix.toList())
    }

    @Test
    fun nlerpMovesTowardTargetQuaternion() {
        val from = floatArrayOf(1f, 0f, 0f, 0f)
        val to = QuaternionMath.fromRotationVector(floatArrayOf(0f, 0f, 0.7071068f, 0.7071068f))
        val blended = QuaternionMath.nlerp(from, to, 0.5f)

        assertTrue(blended[3] > 0f)
    }
}
