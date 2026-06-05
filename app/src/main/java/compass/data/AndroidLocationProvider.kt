package compass.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import compass.domain.LocationSnapshot
import compass.provider.LocationProvider as LocationProviderContract
import compass.provider.LocationListener as LocationUpdateListener

class AndroidLocationProvider(
    private val context: Context,
    private val locationManager: LocationManager = context.getSystemService(LocationManager::class.java),
) : LocationProviderContract {

    private var listener: LocationUpdateListener? = null

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            listener?.onLocation(location.toSnapshot())
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

        override fun onProviderEnabled(provider: String) = Unit

        override fun onProviderDisabled(provider: String) = Unit
    }

    override fun start(listener: LocationUpdateListener) {
        if (!hasLocationPermission()) {
            listener.onLocation(
                LocationSnapshot(
                    latitude = 0.0,
                    longitude = 0.0,
                    altitude = 0.0,
                    speedMetersPerSecond = null,
                    hasFix = false,
                ),
            )
            return
        }

        this.listener = listener
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        } ?: return

        locationManager.requestLocationUpdates(
            provider,
            UPDATE_INTERVAL_MS,
            MIN_DISTANCE_METERS,
            locationListener,
            Looper.getMainLooper(),
        )

        locationManager.getLastKnownLocation(provider)?.let { lastKnown ->
            listener.onLocation(lastKnown.toSnapshot())
        }
    }

    override fun stop() {
        locationManager.removeUpdates(locationListener)
        listener = null
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    private fun Location.toSnapshot(): LocationSnapshot = LocationSnapshot(
        latitude = latitude,
        longitude = longitude,
        altitude = if (hasAltitude()) altitude else 0.0,
        speedMetersPerSecond = if (hasSpeed()) speed else null,
        hasFix = provider != null,
    )

    companion object {
        private const val UPDATE_INTERVAL_MS = 1_000L
        private const val MIN_DISTANCE_METERS = 0f
    }
}
