package compass.provider

import compass.domain.SatelliteStatus

fun interface SatelliteListener {
    fun onSatelliteStatus(status: SatelliteStatus)
}

interface SatelliteProvider {
    fun start(listener: SatelliteListener)

    fun stop()
}
