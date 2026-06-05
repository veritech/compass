package compass.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import compass.domain.SatelliteStatus
import compass.provider.SatelliteListener
import compass.provider.SatelliteProvider

class AndroidSatelliteProvider(
    private val context: Context,
    private val locationManager: LocationManager = context.getSystemService(LocationManager::class.java),
) : SatelliteProvider {

    private var listener: SatelliteListener? = null

    private val gnssCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val satellites = GnssStatusMapper.map(status)
            val inFix = satellites.count { it.usedInFix }
            listener?.onSatelliteStatus(
                SatelliteStatus(
                    satellitesInFix = inFix,
                    satellites = satellites,
                ),
            )
        }
    }

    override fun start(listener: SatelliteListener) {
        if (!hasLocationPermission()) {
            listener.onSatelliteStatus(SatelliteStatus(satellitesInFix = 0, satellites = emptyList()))
            return
        }

        this.listener = listener
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.registerGnssStatusCallback(context.mainExecutor, gnssCallback)
        } else {
            @Suppress("DEPRECATION")
            locationManager.registerGnssStatusCallback(gnssCallback)
        }
    }

    override fun stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.unregisterGnssStatusCallback(gnssCallback)
        } else {
            @Suppress("DEPRECATION")
            locationManager.unregisterGnssStatusCallback(gnssCallback)
        }
        listener = null
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED
    }
}
