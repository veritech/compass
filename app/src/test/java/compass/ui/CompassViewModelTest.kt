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
        val viewModel = CompassViewModel(
            locationProvider = locationProvider,
            compassProvider = compassProvider,
            satelliteProvider = satelliteProvider,
            breadcrumbTracker = BreadcrumbTracker(),
        )

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
    fun computesBearingWhenTargetCoordinatesProvided() {
        val locationProvider = FakeLocationProvider()
        val viewModel = CompassViewModel(
            locationProvider = locationProvider,
            compassProvider = FakeCompassProvider(),
            satelliteProvider = FakeSatelliteProvider(),
            breadcrumbTracker = BreadcrumbTracker(),
        )

        var state = CompassUiState()
        viewModel.setOnStateChanged { state = it }

        viewModel.onPermissionResult(true)
        locationProvider.emit(
            LocationSnapshot(0.0, 0.0, 0.0, null, hasFix = true),
        )
        viewModel.updateTargetLatitude("1.0")
        viewModel.updateTargetLongitude("0.0")

        assertNotNull(state.bearingToTarget)
        assertEquals(0.0, state.bearingToTarget!!, 1.0)
    }

    @Test
    fun bearingHiddenWithoutTarget() {
        val viewModel = CompassViewModel(
            locationProvider = FakeLocationProvider(),
            compassProvider = FakeCompassProvider(),
            satelliteProvider = FakeSatelliteProvider(),
            breadcrumbTracker = BreadcrumbTracker(),
        )

        var state = CompassUiState()
        viewModel.setOnStateChanged { state = it }
        viewModel.onPermissionResult(true)

        assertNull(state.bearingToTarget)
    }
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
