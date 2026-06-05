package compass.di

import android.content.Context
import compass.data.AndroidCompassProvider
import compass.data.CompassCalibrationStore
import compass.data.AndroidLocationProvider
import compass.data.AndroidOrientationProvider
import compass.data.AndroidSatelliteProvider
import compass.data.BreadcrumbTracker
import compass.domain.HeadingSmoother
import compass.provider.CompassProvider
import compass.provider.LocationProvider
import compass.provider.OrientationProvider
import compass.provider.SatelliteProvider

class AppContainer(context: Context) {
    val locationProvider: LocationProvider = AndroidLocationProvider(context)
    val headingSmoother: HeadingSmoother = HeadingSmoother()
    val calibrationStore: CompassCalibrationStore = CompassCalibrationStore(context)
    val compassProvider: CompassProvider = AndroidCompassProvider(
        context,
        headingSmoother = headingSmoother,
        calibrationStore = calibrationStore,
    )
    val satelliteProvider: SatelliteProvider = AndroidSatelliteProvider(context)
    val orientationProvider: OrientationProvider = AndroidOrientationProvider(context)
    val breadcrumbTracker: BreadcrumbTracker = BreadcrumbTracker()
}
