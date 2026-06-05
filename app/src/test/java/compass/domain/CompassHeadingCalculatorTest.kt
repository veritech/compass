package compass.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompassHeadingCalculatorTest {

  @Test
  fun northWhenFlatAndTopPointsNorth() {
    val rotationMatrix = identityRotationMatrix()
    val heading = CompassHeadingCalculator.headingDegrees(rotationMatrix)
    assertNotNull(heading)
    assertEquals(0f, heading!!, 0.5f)
  }

  @Test
  fun eastWhenFlatAndTopPointsEast() {
    val rotationMatrix = floatArrayOf(
      0f, 1f, 0f,
      -1f, 0f, 0f,
      0f, 0f, 1f,
    )
    val heading = CompassHeadingCalculator.headingDegrees(rotationMatrix)
    assertNotNull(heading)
    assertEquals(90f, heading!!, 0.5f)
  }

  @Test
  fun northWhenUprightPortraitFacingNorth() {
    // Device Y points up, device Z points south (screen toward user facing north).
    val rotationMatrix = floatArrayOf(
      1f, 0f, 0f,
      0f, 0f, -1f,
      0f, 1f, 0f,
    )
    val heading = CompassHeadingCalculator.headingDegrees(rotationMatrix)
    assertNotNull(heading)
    assertEquals(0f, heading!!, 0.5f)
  }

  @Test
  fun eastWhenUprightPortraitFacingEast() {
    // Device Z points west (screen toward user facing east).
    val rotationMatrix = floatArrayOf(
      0f, 0f, -1f,
      -1f, 0f, 0f,
      0f, 1f, 0f,
    )
    val heading = CompassHeadingCalculator.headingDegrees(rotationMatrix)
    assertNotNull(heading)
    assertEquals(90f, heading!!, 0.5f)
  }

  @Test
  fun northWhenTiltedForwardFortyFiveDegrees() {
    // 45° pitch from flat north — both screen-top and into-screen axes stay north.
    val rotationMatrix = floatArrayOf(
      1f, 0f, 0f,
      0f, 0.707f, -0.707f,
      0f, 0.707f, 0.707f,
    )
    val heading = CompassHeadingCalculator.headingDegrees(rotationMatrix)
    assertNotNull(heading)
    assertEquals(0f, heading!!, 1f)
  }

  @Test
  fun flatHeadingIgnoresOppositeIntoScreenAxis() {
    // Flat, top points north, but into-screen axis would read south if blended.
    val rotationMatrix = floatArrayOf(
      1f, 0f, 0f,
      0f, 1f, 0f,
      0f, 0f, 1f,
    )
    assertTrue(CompassHeadingCalculator.isRelativelyFlat(rotationMatrix))
    assertEquals(0f, CompassHeadingCalculator.headingDegrees(rotationMatrix)!!, 0.5f)
  }

  @Test
  fun uprightHeadingUsesIntoScreenAxis() {
    val rotationMatrix = floatArrayOf(
      1f, 0f, 0f,
      0f, 0f, -1f,
      0f, 1f, 0f,
    )
    assertTrue(!CompassHeadingCalculator.isRelativelyFlat(rotationMatrix))
    assertEquals(0f, CompassHeadingCalculator.headingDegrees(rotationMatrix)!!, 0.5f)
  }

  @Test
  fun returnsNullForInvalidMatrix() {
    assertNull(CompassHeadingCalculator.headingDegrees(FloatArray(3)))
  }

  private fun identityRotationMatrix(): FloatArray = floatArrayOf(
    1f, 0f, 0f,
    0f, 1f, 0f,
    0f, 0f, 1f,
  )
}
