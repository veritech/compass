package compass.provider

fun interface HeadingListener {
    fun onHeading(degrees: Float)
}

interface CompassProvider {
    fun start(listener: HeadingListener)

    fun stop()
}
