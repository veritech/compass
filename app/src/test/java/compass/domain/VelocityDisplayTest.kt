package compass.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VelocityDisplayTest {

    @Test
    fun hiddenAtOrBelowOneMeterPerSecond() {
        assertFalse(VelocityDisplay.shouldShow(null))
        assertFalse(VelocityDisplay.shouldShow(0f))
        assertFalse(VelocityDisplay.shouldShow(1f))
    }

    @Test
    fun visibleAboveOneMeterPerSecond() {
        assertTrue(VelocityDisplay.shouldShow(1.1f))
        assertTrue(VelocityDisplay.shouldShow(5f))
    }
}
