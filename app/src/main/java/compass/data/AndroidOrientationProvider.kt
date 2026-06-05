package compass.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import compass.domain.QuaternionOrientationSmoother
import compass.provider.OrientationListener
import compass.provider.OrientationProvider

class AndroidOrientationProvider(
    context: Context,
    private val sensorManager: SensorManager = context.getSystemService(SensorManager::class.java),
    private val orientationSmoother: QuaternionOrientationSmoother = QuaternionOrientationSmoother(),
) : OrientationProvider {

    private var listener: OrientationListener? = null
    private val rotationMatrix = FloatArray(9)
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false
    private var hasRotationVectorSensor = false

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when {
                CompassSensorTypes.isMagneticRotationVectorType(event.sensor.type) -> {
                    publishFromRotationVector(event.values)
                }
                event.sensor.type == Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, gravity, 0, gravity.size)
                    hasGravity = true
                    publishFromAccelAndMagnetometer()
                }
                event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, geomagnetic, 0, geomagnetic.size)
                    hasGeomagnetic = true
                    publishFromAccelAndMagnetometer()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    override fun start(listener: OrientationListener) {
        this.listener = listener
        orientationSmoother.reset()
        hasGravity = false
        hasGeomagnetic = false

        val rotationSensor = CompassSensorTypes.magneticRotationVectorTypes
            .asSequence()
            .map { type -> sensorManager.getDefaultSensor(type) }
            .firstOrNull { it != null }

        hasRotationVectorSensor = rotationSensor != null
        if (rotationSensor != null) {
            sensorManager.registerListener(
                sensorListener,
                rotationSensor,
                SensorManager.SENSOR_DELAY_GAME,
            )
        }

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (accelerometer != null) {
            sensorManager.registerListener(
                sensorListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME,
            )
        }
        if (magnetometer != null) {
            sensorManager.registerListener(
                sensorListener,
                magnetometer,
                SensorManager.SENSOR_DELAY_GAME,
            )
        }
    }

    override fun stop() {
        sensorManager.unregisterListener(sensorListener)
        listener = null
        orientationSmoother.reset()
        hasGravity = false
        hasGeomagnetic = false
        hasRotationVectorSensor = false
    }

    private fun publishFromRotationVector(values: FloatArray) {
        val smoothedMatrix = orientationSmoother.smoothRotationVector(values)
        listener?.onRotationMatrix(smoothedMatrix)
    }

    private fun publishFromAccelAndMagnetometer() {
        if (hasRotationVectorSensor || !hasGravity || !hasGeomagnetic) return

        val inclinationMatrix = FloatArray(9)
        if (!SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)) {
            return
        }
        val smoothedMatrix = orientationSmoother.smoothRotationMatrix(rotationMatrix)
        listener?.onRotationMatrix(smoothedMatrix)
    }
}
