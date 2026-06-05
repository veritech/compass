package compass.provider

fun interface OrientationListener {
    fun onRotationMatrix(rotationMatrix: FloatArray)
}

interface OrientationProvider {
    fun start(listener: OrientationListener)

    fun stop()
}
