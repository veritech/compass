package compass.data

import compass.domain.BreadcrumbPoint
import compass.domain.LocationSnapshot

class BreadcrumbTracker(
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    private val points = mutableListOf<BreadcrumbPoint>()
    private var intervalSeconds: Int = 60
    private var lastRecordedAt: Long? = null
    private var active = false

    fun configure(intervalSeconds: Int) {
        require(intervalSeconds > 0) { "Interval must be positive" }
        this.intervalSeconds = intervalSeconds
    }

    fun start() {
        active = true
        lastRecordedAt = null
    }

    fun stop() {
        active = false
    }

    fun onLocation(snapshot: LocationSnapshot) {
        if (!active || !snapshot.hasFix) return

        val now = clock()
        val intervalMillis = intervalSeconds * 1_000L
        val previous = lastRecordedAt
        if (previous != null && now - previous < intervalMillis) return

        points.add(
            BreadcrumbPoint(
                latitude = snapshot.latitude,
                longitude = snapshot.longitude,
                altitude = snapshot.altitude,
                timestampMillis = now,
            ),
        )
        lastRecordedAt = now
    }

    fun points(): List<BreadcrumbPoint> = points.toList()

    fun clear() {
        points.clear()
        lastRecordedAt = null
    }
}
