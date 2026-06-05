package compass.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import compass.domain.CompassHeadingCalculator
import compass.domain.DeviceTiltDetector
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
    private val tiltCorrectedMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false
    private var usesRotationVectorSensor = false

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

        usesRotationVectorSensor = rotationSensor != null
        if (rotationSensor != null) {
            sensorManager.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }

        registerAccelerometerAndMagnetometer()
    }

    override fun stop() {
        sensorManager.unregisterListener(sensorListener)
        listener = null
        headingSmoother.reset()
        hasGravity = false
        hasGeomagnetic = false
        usesRotationVectorSensor = false
    }

    private fun registerAccelerometerAndMagnetometer() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (accelerometer != null) {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        if (magnetometer != null) {
            sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun publishHeadingFromRotationVector(values: FloatArray) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
        publishHeadingFromRotationMatrix()
    }

    private fun publishHeadingFromAccelAndMagnetometer() {
        if (!hasGravity || !hasGeomagnetic) return
        if (usesRotationVectorSensor) return
        if (!SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)) {
            return
        }
        publishHeadingFromRotationMatrix()
    }

    private fun publishHeadingFromRotationMatrix() {
        val headingMatrix = headingMatrixForCurrentPose()
        val rawHeading = CompassHeadingCalculator.headingDegrees(headingMatrix)
            ?: azimuthFromGetOrientation(headingMatrix)
        listener?.onHeading(headingSmoother.smooth(rawHeading.roundToInt().toFloat()))
    }

    /**
     * Geomagnetic rotation vectors omit accelerometer fusion, so when the phone is
     * flat their tilt estimate is often wrong and compass heading reads ~180° off.
     * Accelerometer + magnetometer fusion gives the correct flat-table heading.
     */
    private fun headingMatrixForCurrentPose(): FloatArray {
        if (
            usesRotationVectorSensor &&
            hasGravity &&
            hasGeomagnetic &&
            DeviceTiltDetector.isFlat(gravity) &&
            SensorManager.getRotationMatrix(tiltCorrectedMatrix, inclinationMatrix, gravity, geomagnetic)
        ) {
            return tiltCorrectedMatrix
        }
        return rotationMatrix
    }

    private fun azimuthFromGetOrientation(rotationMatrix: FloatArray): Float {
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)
        return ((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
    }
}
