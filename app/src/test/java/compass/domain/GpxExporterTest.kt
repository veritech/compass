package compass.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class GpxExporterTest {

    @Test
    fun exportContainsTrackPoints() {
        val points = listOf(
            BreadcrumbPoint(latitude = 51.5, longitude = -0.1, altitude = 10.0, timestampMillis = 1_700_000_000_000),
            BreadcrumbPoint(latitude = 51.6, longitude = -0.2, altitude = 12.0, timestampMillis = 1_700_000_060_000),
        )

        val gpx = GpxExporter.export(points, trackName = "Test trail")

        assertTrue(gpx.contains("<name>Test trail</name>"))
        assertTrue(gpx.contains("""lat="51.5" lon="-0.1""""))
        assertTrue(gpx.contains("""lat="51.6" lon="-0.2""""))
        assertTrue(gpx.contains("<ele>10.0</ele>"))
        assertTrue(gpx.contains("</gpx>"))
    }
}
