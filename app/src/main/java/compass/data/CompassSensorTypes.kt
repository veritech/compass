package compass.data

import android.hardware.Sensor

object CompassSensorTypes {
    /** Magnetometer-fused rotation vectors suitable for compass heading. */
    val magneticRotationVectorTypes: IntArray = intArrayOf(
        Sensor.TYPE_ROTATION_VECTOR,
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
    )

    val rotationVectorTypes: IntArray = intArrayOf(
        Sensor.TYPE_ROTATION_VECTOR,
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
        Sensor.TYPE_GAME_ROTATION_VECTOR,
    )

    fun isRotationVectorType(type: Int): Boolean = type in rotationVectorTypes

    fun isMagneticRotationVectorType(type: Int): Boolean = type in magneticRotationVectorTypes

    fun isMagnetometerType(type: Int): Boolean =
        type == Sensor.TYPE_MAGNETIC_FIELD || type == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED
}
