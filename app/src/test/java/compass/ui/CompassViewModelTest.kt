package compass.ui

import compass.data.BreadcrumbTracker
import compass.domain.LocationSnapshot
import compass.provider.CompassProvider
import compass.provider.HeadingListener
import compass.provider.LocationListener
import compass.provider.LocationProvider
import compass.provider.SatelliteListener
import compass.provider.SatelliteProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CompassViewModelTest {

    @Test
    fun updatesHeadingAndLocationFromProviders() {
        val locationProvider = FakeLocationProvider()
        val compassProvider = FakeCompassProvider()
        val satelliteProvider = FakeSatelliteProvider()
        val viewModel = createViewModel(locationProvider, compassProvider, satelliteProvider)

        var state = CompassUiState()
        viewModel.setOnStateChanged { state = it }

        viewModel.onPermissionResult(true)
        compassProvider.emitHeading(45f)
        locationProvider.emit(
            LocationSnapshot(
                latitude = 51.5,
                longitude = -0.12,
                altitude = 25.0,
                speedMetersPerSecond = 5f,
                hasFix = true,
            ),
        )
        satelliteProvider.emitCount(7)

        assertEquals(45f, state.headingDegrees, 0.01f)
        assertEquals(51.5, state.latitude!!, 0.0001)
        assertEquals(-0.12, state.longitude!!, 0.0001)
        assertEquals(25.0, state.altitude!!, 0.0001)
        assertEquals(7, state.satelliteCount)
    }

    @Test
    fun setTargetLocationUpdatesBearing() {
        val locationProvider = FakeLocationProvider()
        val viewModel = createViewModel(locationProvider)

        var state = CompassUiState()
        viewModel.setOnStateChanged { state = it }

        viewModel.onPermissionResult(true)
        locationProvider.emit(
            LocationSnapshot(0.0, 0.0, 0.0, null, hasFix = true),
        )
        viewModel.setTargetLocation(1.0, 0.0)

        assertEquals(1.0, state.targetLatitude!!, 0.0001)
        assertNotNull(state.bearingToTarget)
        assertEquals(0.0, state.bearingToTarget!!, 1.0)
    }

    @Test
    fun bearingHiddenWithoutTarget() {
        val viewModel = createViewModel(FakeLocationProvider())

        var state = CompassUiState()
        viewModel.setOnStateChanged { state = it }
        viewModel.onPermissionResult(true)

        assertNull(state.bearingToTarget)
    }

    @Test
    fun loadGpxTrailPopulatesLoadedPoints() {
        val viewModel = createViewModel(FakeLocationProvider())
        var state = CompassUiState()
        viewModel.setOnStateChanged { state = it }

        viewModel.loadGpxTrail(
            """
            <gpx><trk><trkseg>
              <trkpt lat="1.0" lon="2.0"><ele>3.0</ele></trkpt>
            </trkseg></trk></gpx>
            """.trimIndent(),
        )

        assertEquals(1, state.loadedTrailPoints.size)
        assertEquals(1.0, state.loadedTrailPoints[0].latitude, 0.0001)
    }

    private fun createViewModel(
        locationProvider: FakeLocationProvider = FakeLocationProvider(),
        compassProvider: FakeCompassProvider = FakeCompassProvider(),
        satelliteProvider: FakeSatelliteProvider = FakeSatelliteProvider(),
    ): CompassViewModel = CompassViewModel(
        locationProvider = locationProvider,
        compassProvider = compassProvider,
        satelliteProvider = satelliteProvider,
        breadcrumbTracker = BreadcrumbTracker(),
        mapsApiKeyConfigured = false,
    )
}

private class FakeLocationProvider : LocationProvider {
    private var listener: LocationListener? = null

    override fun start(listener: LocationListener) {
        this.listener = listener
    }

    override fun stop() {
        listener = null
    }

    fun emit(snapshot: LocationSnapshot) {
        listener?.onLocation(snapshot)
    }
}

private class FakeCompassProvider : CompassProvider {
    private var listener: HeadingListener? = null

    override fun start(listener: HeadingListener) {
        this.listener = listener
    }

    override fun stop() {
        listener = null
    }

    fun emitHeading(degrees: Float) {
        listener?.onHeading(degrees)
    }
}

private class FakeSatelliteProvider : SatelliteProvider {
    private var listener: SatelliteListener? = null

    override fun start(listener: SatelliteListener) {
        this.listener = listener
    }

    override fun stop() {
        listener = null
    }

    fun emitCount(count: Int) {
        listener?.onSatelliteCount(count)
    }
}
