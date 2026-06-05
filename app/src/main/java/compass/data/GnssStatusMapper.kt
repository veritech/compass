package compass.data

import android.location.GnssStatus
import compass.domain.SatelliteInfo

object GnssStatusMapper {
    fun map(status: GnssStatus): List<SatelliteInfo> {
        return (0 until status.satelliteCount).map { index ->
            SatelliteInfo(
                constellation = constellationLabel(status.getConstellationType(index)),
                satelliteId = status.getSvid(index),
                elevationDegrees = status.getElevationDegrees(index),
                azimuthDegrees = status.getAzimuthDegrees(index),
                signalStrengthDbHz = status.getCn0DbHz(index),
                usedInFix = status.usedInFix(index),
            )
        }
    }

    private fun constellationLabel(type: Int): String = when (type) {
        GnssStatus.CONSTELLATION_GPS -> "GPS"
        GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
        GnssStatus.CONSTELLATION_GALILEO -> "Galileo"
        GnssStatus.CONSTELLATION_BEIDOU -> "BeiDou"
        GnssStatus.CONSTELLATION_QZSS -> "QZSS"
        GnssStatus.CONSTELLATION_IRNSS -> "IRNSS"
        GnssStatus.CONSTELLATION_SBAS -> "SBAS"
        else -> "Other"
    }
}
