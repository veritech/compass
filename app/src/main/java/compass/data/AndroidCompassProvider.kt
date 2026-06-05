package compass.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import compass.domain.CompassHeadingCalculator
import compass.domain.HeadingSmoother
import compass.provider.CompassProvider
import compass.provider.HeadingListener
import kotlin.math.roundToInt

class AndroidCompassProvider(
    context: Context,
    private val sensorManager: SensorManager = context.getSystemService(SensorManager::class.java),
    private val headingSmoother: HeadingSmoother = HeadingSmoother(),
) : CompassProvider {

    private var listener: HeadingListener? = null
    private val rotationMatrix = FloatArray(9)
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when {
                CompassSensorTypes.isMagneticRotationVectorType(event.sensor.type) -> {
                    publishHeadingFromRotationVector(event.values)
                }
                event.sensor.type == Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, gravity, 0, gravity.size)
                    hasGravity = true
                    publishHeadingFromAccelAndMagnetometer()
                }
                event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, geomagnetic, 0, geomagnetic.size)
                    hasGeomagnetic = true
                    publishHeadingFromAccelAndMagnetometer()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    override fun start(listener: HeadingListener) {
        this.listener = listener
        headingSmoother.reset()
        hasGravity = false
        hasGeomagnetic = false

        val rotationSensor = CompassSensorTypes.magneticRotationVectorTypes
            .asSequence()
            .map { type -> sensorManager.getDefaultSensor(type) }
            .firstOrNull { it != null }

        if (rotationSensor != null) {
            sensorManager.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            return
        }

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun stop() {
        sensorManager.unregisterListener(sensorListener)
        listener = null
        headingSmoother.reset()
        hasGravity = false
        hasGeomagnetic = false
    }

    private fun publishHeadingFromRotationVector(values: FloatArray) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
        publishHeadingFromRotationMatrix()
    }

    private fun publishHeadingFromAccelAndMagnetometer() {
        if (!hasGravity || !hasGeomagnetic) return
        val inclinationMatrix = FloatArray(9)
        if (!SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)) {
            return
        }
        publishHeadingFromRotationMatrix()
    }

    private fun publishHeadingFromRotationMatrix() {
        val rawHeading = if (CompassHeadingCalculator.isRelativelyFlat(rotationMatrix)) {
            azimuthFromGetOrientation(rotationMatrix)
        } else {
            CompassHeadingCalculator.headingDegrees(rotationMatrix)
                ?: azimuthFromGetOrientation(rotationMatrix)
        }
        listener?.onHeading(headingSmoother.smooth(rawHeading.roundToInt().toFloat()))
    }

    private fun azimuthFromGetOrientation(rotationMatrix: FloatArray): Float {
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)
        return ((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
    }
}
