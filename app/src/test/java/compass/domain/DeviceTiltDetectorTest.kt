package compass.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceTiltDetectorTest {

    @Test
    fun flatWhenGravityAlongDeviceZ() {
        assertTrue(DeviceTiltDetector.isFlat(floatArrayOf(0f, 0f, -9.8f)))
        assertTrue(DeviceTiltDetector.isFlat(floatArrayOf(0f, 0f, 9.8f)))
    }

    @Test
    fun notFlatWhenUprightPortrait() {
        assertFalse(DeviceTiltDetector.isFlat(floatArrayOf(0f, -9.8f, 0f)))
    }

    @Test
    fun notFlatWhenTiltedFortyFiveDegrees() {
        val component = 9.8f * 0.707f
        assertFalse(DeviceTiltDetector.isFlat(floatArrayOf(0f, -component, -component)))
    }
}
