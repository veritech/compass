package compass.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MagnetometerCalibrationBuilderTest {

    @Test
    fun progressIncreasesAsAxesSweep() {
        val builder = MagnetometerCalibrationBuilder(minSamples = 5, targetAxisRangeUt = 10f)
        assertEquals(0f, builder.progress(), 0.01f)

        repeat(5) { step ->
            val offset = step * 4f
            builder.addSample(-10f + offset, -8f + offset, -6f + offset)
        }

        assertTrue(builder.progress() > 0.5f)
    }

    @Test
    fun completesWhenEnoughSamplesAndAxisCoverage() {
        val builder = MagnetometerCalibrationBuilder(minSamples = 40, targetAxisRangeUt = 20f)
        repeat(50) { step ->
            val angle = step * 0.35f
            builder.addSample(
                25f * kotlin.math.cos(angle),
                25f * kotlin.math.sin(angle),
                20f * kotlin.math.cos(angle + 0.8f),
            )
        }

        assertTrue(builder.isComplete())
        val calibration = builder.build()
        assertNotNull(calibration)
    }

    @Test
    fun doesNotCompleteWithTooFewSamples() {
        val builder = MagnetometerCalibrationBuilder(minSamples = 40, targetAxisRangeUt = 5f)
        repeat(10) {
            builder.addSample(-20f, 20f, -20f)
        }
        assertFalse(builder.isComplete())
    }

    @Test
    fun builtCalibrationIsPlausible() {
        val builder = MagnetometerCalibrationBuilder(minSamples = 40, targetAxisRangeUt = 20f)
        repeat(50) { step ->
            val angle = step * 0.35f
            builder.addSample(
                25f * kotlin.math.cos(angle),
                25f * kotlin.math.sin(angle),
                20f * kotlin.math.cos(angle + 0.8f),
            )
        }
        assertNotNull(builder.build()?.isPlausible())
    }

    @Test
    fun applyRemovesHardIronOffset() {
        val calibration = MagnetometerCalibration(
            offsetX = 10f,
            offsetY = -5f,
            offsetZ = 2f,
            scaleX = 1f,
            scaleY = 1f,
            scaleZ = 1f,
        )
        val corrected = calibration.apply(floatArrayOf(30f, 5f, 12f))
        assertEquals(20f, corrected[0], 0.01f)
        assertEquals(10f, corrected[1], 0.01f)
        assertEquals(10f, corrected[2], 0.01f)
    }
}
