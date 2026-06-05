package compass.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class GpxParserTest {

    @Test
    fun parseTrackPointsFromGpx() {
        val gpx = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1">
              <trk>
                <trkseg>
                  <trkpt lat="51.5" lon="-0.1"><ele>10.0</ele></trkpt>
                  <trkpt lat="51.6" lon="-0.2"><ele>12.0</ele></trkpt>
                </trkseg>
              </trk>
            </gpx>
        """.trimIndent()

        val points = GpxParser.parse(gpx)

        assertEquals(2, points.size)
        assertEquals(51.5, points[0].latitude, 0.0001)
        assertEquals(-0.1, points[0].longitude, 0.0001)
        assertEquals(10.0, points[0].altitude, 0.0001)
    }
}
