package compass.ui

import compass.data.BreadcrumbTracker
import compass.domain.DeviceOrientation
import compass.domain.LocationSnapshot
import compass.domain.SatelliteInfo
import compass.domain.SatelliteStatus
import compass.provider.CompassProvider
import compass.provider.HeadingListener
import compass.provider.LocationListener
import compass.provider.LocationProvider
import compass.provider.OrientationListener
import compass.provider.OrientationProvider
import compass.provider.SatelliteListener
import compass.provider.SatelliteProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompassViewModelTest {

    @Test
    fun updatesHeadingAndLocationFromProviders() {
        val locationProvider = FakeLocationProvider()
        val compassProvider = FakeCompassProvider()
        val satelliteProvider = FakeSatelliteProvider()
        val orientationProvider = FakeOrientationProvider()
        val viewModel = createViewModel(
            locationProvider,
            compassProvider,
            satelliteProvider,
            orientationProvider,
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
        satelliteProvider.emitStatus(
            SatelliteStatus(
                satellitesInFix = 1,
                satellites = listOf(
                    SatelliteInfo("GPS", 12, 45f, 120f, 32f, usedInFix = true),
                    SatelliteInfo("Galileo", 7, 30f, 200f, 24f, usedInFix = false),
                ),
            ),
        )

        assertEquals(45f, state.headingDegrees, 0.01f)
        assertEquals(51.5, state.latitude!!, 0.0001)
        assertEquals(-0.12, state.longitude!!, 0.0001)
        assertEquals(25.0, state.altitude!!, 0.0001)
        assertEquals(5f, state.speedMetersPerSecond!!, 0.01f)
        assertEquals(1, state.satelliteCount)
        assertEquals(2, state.satellites.size)
    }

    @Test
    fun updatesCalibrationProgressFromCompassProvider() {
        val compassProvider = FakeCompassProvider()
        val viewModel = createViewModel(compassProvider = compassProvider)
        var state = CompassUiState()
        viewModel.setOnStateChanged { state = it }

        viewModel.onPermissionResult(true)
        viewModel.startCompassCalibration()
        compassProvider.emitCalibrationProgress(0.4f)
        assertEquals(0.4f, state.calibrationProgress, 0.01f)
        assertTrue(state.isCalibrating)

        compassProvider.emitCalibrationComplete(true)
        assertFalse(state.isCalibrating)
        assertEquals(1f, state.calibrationProgress, 0.01f)
        assertTrue(state.hasCompassCalibration)
    }

    @Test
    fun updatesDeviceRotationMatrixFromOrientationProvider() {
        val orientationProvider = FakeOrientationProvider()
        val viewModel = createViewModel(orientationProvider = orientationProvider)
        var state = CompassUiState()
        viewModel.setOnStateChanged { state = it }

        viewModel.onPermissionResult(true)
        val matrix = floatArrayOf(0f, 1f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        orientationProvider.emit(matrix)

        assertTrue(state.deviceRotationMatrix.contentEquals(matrix))
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
        orientationProvider: FakeOrientationProvider = FakeOrientationProvider(),
    ): CompassViewModel = CompassViewModel(
        locationProvider = locationProvider,
        compassProvider = compassProvider,
        satelliteProvider = satelliteProvider,
        orientationProvider = orientationProvider,
        breadcrumbTracker = BreadcrumbTracker(),
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
    private var calibrationListener: compass.provider.CompassCalibrationListener? = null
    var hasCalibration: Boolean = false

    override fun start(listener: HeadingListener) {
        this.listener = listener
    }

    override fun stop() {
        listener = null
        calibrationListener = null
    }

    override fun startCalibration(listener: compass.provider.CompassCalibrationListener) {
        calibrationListener = listener
    }

    override fun cancelCalibration() {
        calibrationListener = null
    }

    override fun hasCalibration(): Boolean = hasCalibration

    fun emitHeading(degrees: Float) {
        listener?.onHeading(degrees)
    }

    fun emitCalibrationProgress(progress: Float) {
        calibrationListener?.onCalibrationProgress(progress)
    }

    fun emitCalibrationComplete(success: Boolean) {
        calibrationListener?.onCalibrationComplete(success)
        if (success) hasCalibration = true
        calibrationListener = null
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

    fun emitStatus(status: SatelliteStatus) {
        listener?.onSatelliteStatus(status)
    }
}

private class FakeOrientationProvider : OrientationProvider {
    private var listener: OrientationListener? = null

    override fun start(listener: OrientationListener) {
        this.listener = listener
    }

    override fun stop() {
        listener = null
    }

    fun emit(rotationMatrix: FloatArray) {
        listener?.onRotationMatrix(rotationMatrix)
    }
}
