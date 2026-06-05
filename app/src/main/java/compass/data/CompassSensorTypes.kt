package compass.data

import android.hardware.Sensor

object CompassSensorTypes {
    val rotationVectorTypes: IntArray = intArrayOf(
        Sensor.TYPE_ROTATION_VECTOR,
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
        Sensor.TYPE_GAME_ROTATION_VECTOR,
    )

    fun isRotationVectorType(type: Int): Boolean = type in rotationVectorTypes
}
