package compass.provider

fun interface SatelliteListener {
    fun onSatelliteCount(count: Int)
}

interface SatelliteProvider {
    fun start(listener: SatelliteListener)

    fun stop()
}
