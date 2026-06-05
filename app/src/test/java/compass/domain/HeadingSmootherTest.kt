package compass.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadingSmootherTest {

    @Test
    fun firstReadingPassesThrough() {
        val smoother = HeadingSmoother(alpha = 0.1f)
        assertEquals(90f, smoother.smooth(90f), 0.01f)
    }

    @Test
    fun smoothsTowardNewHeading() {
        val smoother = HeadingSmoother(alpha = 0.5f)
        smoother.smooth(0f)
        val blended = smoother.smooth(90f)
        assertTrue(blended in 40f..50f)
    }

    @Test
    fun handlesNorthWrapAround() {
        val smoother = HeadingSmoother(alpha = 0.5f)
        smoother.smooth(350f)
        val blended = smoother.smooth(10f)
        assertTrue(blended >= 350f || blended <= 10f)
    }

    @Test
    fun resetStartsFresh() {
        val smoother = HeadingSmoother(alpha = 0.1f)
        smoother.smooth(0f)
        smoother.reset()
        assertEquals(180f, smoother.smooth(180f), 0.01f)
    }

    @Test
    fun outputDeadbandHoldsHeadingWhenSmoothedChangeIsTiny() {
        val smoother = HeadingSmoother(
            alpha = 0.08f,
            stationaryAlpha = 0.04f,
            outputDeadbandDegrees = 0.75f,
        )
        val first = smoother.smooth(90f)
        val second = smoother.smooth(90.2f)
        assertEquals(first, second, 0.01f)
    }

    @Test
    fun angularDistanceWrapsAcrossNorth() {
        assertEquals(20f, HeadingSmoother.angularDistance(350f, 10f), 0.01f)
    }
}
