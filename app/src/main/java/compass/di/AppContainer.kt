package compass.di

import android.content.Context
import compass.data.AndroidCompassProvider
import compass.data.AndroidLocationProvider
import compass.data.AndroidSatelliteProvider
import compass.data.BreadcrumbTracker
import compass.provider.CompassProvider
import compass.provider.LocationProvider
import compass.provider.SatelliteProvider

class AppContainer(context: Context) {
    val locationProvider: LocationProvider = AndroidLocationProvider(context)
    val compassProvider: CompassProvider = AndroidCompassProvider(context)
    val satelliteProvider: SatelliteProvider = AndroidSatelliteProvider(context)
    val breadcrumbTracker: BreadcrumbTracker = BreadcrumbTracker()
}
