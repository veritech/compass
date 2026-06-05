package compass.provider

import compass.domain.LocationSnapshot

fun interface LocationListener {
    fun onLocation(snapshot: LocationSnapshot)
}

interface LocationProvider {
    fun start(listener: LocationListener)

    fun stop()
}
