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

class AndroidCompassProvider(
    context: Context,
    private val sensorManager: SensorManager = context.getSystemService(SensorManager::class.java),
    private val headingSmoother: HeadingSmoother = HeadingSmoother(),
) : CompassProvider {

    private var listener: HeadingListener? = null
    private val rotationVectorMatrix = FloatArray(9)
    private val fusedMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false
    private var hasRotationVectorMatrix = false

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when {
                CompassSensorTypes.isMagneticRotationVectorType(event.sensor.type) -> {
                    SensorManager.getRotationMatrixFromVector(rotationVectorMatrix, event.values)
                    hasRotationVectorMatrix = true
                    publishHeading()
                }
                event.sensor.type == Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, gravity, 0, gravity.size)
                    hasGravity = true
                    publishHeading()
                }
                event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, geomagnetic, 0, geomagnetic.size)
                    hasGeomagnetic = true
                    publishHeading()
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
        hasRotationVectorMatrix = false

        val rotationSensor = CompassSensorTypes.magneticRotationVectorTypes
            .asSequence()
            .map { type -> sensorManager.getDefaultSensor(type) }
            .firstOrNull { it != null }

        if (rotationSensor != null) {
            sensorManager.registerListener(
                sensorListener,
                rotationSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
            )
        }

        registerAccelerometerAndMagnetometer()
    }

    override fun stop() {
        sensorManager.unregisterListener(sensorListener)
        listener = null
        headingSmoother.reset()
        hasGravity = false
        hasGeomagnetic = false
        hasRotationVectorMatrix = false
    }

    private fun registerAccelerometerAndMagnetometer() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (accelerometer != null) {
            sensorManager.registerListener(
                sensorListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
            )
        }
        if (magnetometer != null) {
            sensorManager.registerListener(
                sensorListener,
                magnetometer,
                SensorManager.SENSOR_DELAY_NORMAL,
            )
        }
    }

    private fun publishHeading() {
        val headingMatrix = resolveHeadingMatrix() ?: return
        val rawHeading = CompassHeadingCalculator.headingDegrees(headingMatrix)
            ?: azimuthFromGetOrientation(headingMatrix)
        listener?.onHeading(headingSmoother.smooth(rawHeading))
    }

    /**
     * Prefer accelerometer + magnetometer fusion for all poses. Geomagnetic rotation
     * vectors omit the accelerometer, which makes flat and portrait headings unstable.
     * Rotation vector is only used when tilt fusion is unavailable.
     */
    private fun resolveHeadingMatrix(): FloatArray? {
        if (
            hasGravity &&
            hasGeomagnetic &&
            SensorManager.getRotationMatrix(fusedMatrix, inclinationMatrix, gravity, geomagnetic)
        ) {
            return fusedMatrix
        }
        return rotationVectorMatrix.takeIf { hasRotationVectorMatrix }
    }

    private fun azimuthFromGetOrientation(rotationMatrix: FloatArray): Float {
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)
        return ((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
    }
}
