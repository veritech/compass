package compass.data

import compass.domain.LocationSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class BreadcrumbTrackerTest {

    @Test
    fun recordsPointAtConfiguredInterval() {
        var now = 0L
        val tracker = BreadcrumbTracker(clock = { now })
        tracker.configure(intervalSeconds = 60)
        tracker.start()

        val snapshot = LocationSnapshot(
            latitude = 1.0,
            longitude = 2.0,
            altitude = 3.0,
            speedMetersPerSecond = null,
            hasFix = true,
        )

        tracker.onLocation(snapshot)
        assertEquals(1, tracker.points().size)

        now = 30_000
        tracker.onLocation(snapshot)
        assertEquals(1, tracker.points().size)

        now = 60_000
        tracker.onLocation(snapshot)
        assertEquals(2, tracker.points().size)
    }

    @Test
    fun ignoresLocationsWithoutFix() {
        val tracker = BreadcrumbTracker()
        tracker.configure(10)
        tracker.start()

        tracker.onLocation(
            LocationSnapshot(0.0, 0.0, 0.0, null, hasFix = false),
        )

        assertEquals(0, tracker.points().size)
    }
}
