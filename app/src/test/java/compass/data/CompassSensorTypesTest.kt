package compass.data

import android.hardware.Sensor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompassSensorTypesTest {

    @Test
    fun recognizesStandardRotationVector() {
        assertTrue(CompassSensorTypes.isRotationVectorType(Sensor.TYPE_ROTATION_VECTOR))
    }

    @Test
    fun recognizesGeomagneticRotationVector() {
        assertTrue(CompassSensorTypes.isRotationVectorType(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR))
    }

    @Test
    fun recognizesMagnetometerTypes() {
        assertTrue(CompassSensorTypes.isMagnetometerType(Sensor.TYPE_MAGNETIC_FIELD))
        assertTrue(CompassSensorTypes.isMagnetometerType(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED))
    }

    @Test
    fun recognizesGameRotationVector() {
        assertTrue(CompassSensorTypes.isRotationVectorType(Sensor.TYPE_GAME_ROTATION_VECTOR))
    }

    @Test
    fun gameRotationVectorIsNotMagnetic() {
        assertFalse(CompassSensorTypes.isMagneticRotationVectorType(Sensor.TYPE_GAME_ROTATION_VECTOR))
    }

    @Test
    fun geomagneticRotationVectorIsMagnetic() {
        assertTrue(CompassSensorTypes.isMagneticRotationVectorType(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR))
    }

    @Test
    fun rejectsUnrelatedSensorTypes() {
        assertFalse(CompassSensorTypes.isRotationVectorType(Sensor.TYPE_ACCELEROMETER))
        assertFalse(CompassSensorTypes.isRotationVectorType(Sensor.TYPE_MAGNETIC_FIELD))
    }
}
