package compass.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import compass.domain.CompassHeadingCalculator
import compass.domain.DeviceTiltDetector
import compass.domain.HeadingSmoother
import compass.domain.MagnetometerCalibration
import compass.domain.MagnetometerCalibrationBuilder
import compass.provider.CompassCalibrationListener
import compass.provider.CompassProvider
import compass.provider.HeadingListener

class AndroidCompassProvider(
    context: Context,
    private val sensorManager: SensorManager = context.getSystemService(SensorManager::class.java),
    private val headingSmoother: HeadingSmoother = HeadingSmoother(),
    private val calibrationStore: CompassCalibrationStore = CompassCalibrationStore(context),
) : CompassProvider {

    private var listener: HeadingListener? = null
    private var calibrationListener: CompassCalibrationListener? = null
    private val calibrationBuilder = MagnetometerCalibrationBuilder()
    private var activeCalibration: MagnetometerCalibration? = calibrationStore.load()

    private val rotationVectorMatrix = FloatArray(9)
    private val fusedMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false
    private var hasRotationVectorMatrix = false

    private var magnetometerSensor: Sensor? = null

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
                CompassSensorTypes.isMagnetometerType(event.sensor.type) -> {
                    onMagnetometerEvent(event.values)
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

        registerAccelerometer()
        registerMagnetometer()
    }

    override fun stop() {
        sensorManager.unregisterListener(sensorListener)
        listener = null
        calibrationListener = null
        calibrationBuilder.reset()
        headingSmoother.reset()
        hasGravity = false
        hasGeomagnetic = false
        hasRotationVectorMatrix = false
        magnetometerSensor = null
    }

    override fun startCalibration(listener: CompassCalibrationListener) {
        calibrationListener = listener
        calibrationBuilder.reset()
        registerMagnetometer(SensorManager.SENSOR_DELAY_GAME)
    }

    override fun cancelCalibration() {
        calibrationListener = null
        calibrationBuilder.reset()
        registerMagnetometer()
    }

    override fun hasCalibration(): Boolean = activeCalibration != null

    private fun registerAccelerometer() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        sensorManager.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL,
        )
    }

    private fun registerMagnetometer(delay: Int = SensorManager.SENSOR_DELAY_NORMAL) {
        magnetometerSensor?.let { sensorManager.unregisterListener(sensorListener, it) }
        magnetometerSensor = selectMagnetometerSensor()
        magnetometerSensor?.let { sensor ->
            sensorManager.registerListener(sensorListener, sensor, delay)
        }
    }

    private fun selectMagnetometerSensor(): Sensor? {
        val useUncalibrated = activeCalibration != null || calibrationListener != null
        if (useUncalibrated) {
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED)?.let { return it }
        }
        return sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    private fun onMagnetometerEvent(values: FloatArray) {
        if (calibrationListener != null) {
            handleCalibrationSample(values)
            return
        }

        val corrected = correctMagnetometer(values)
        System.arraycopy(corrected, 0, geomagnetic, 0, geomagnetic.size)
        hasGeomagnetic = true
        publishHeading()
    }

    private fun handleCalibrationSample(values: FloatArray) {
        val listener = calibrationListener ?: return
        calibrationBuilder.addSample(values[0], values[1], values[2])
        listener.onCalibrationProgress(calibrationBuilder.progress())
        if (!calibrationBuilder.isComplete()) return

        val calibration = calibrationBuilder.build()
        calibrationListener = null
        calibrationBuilder.reset()
        if (calibration == null) {
            listener.onCalibrationComplete(false)
            registerMagnetometer()
            return
        }

        activeCalibration = calibration
        calibrationStore.save(calibration)
        headingSmoother.reset()
        listener.onCalibrationComplete(true)
        registerMagnetometer()
    }

    private fun correctMagnetometer(raw: FloatArray): FloatArray {
        val calibration = activeCalibration ?: return raw.copyOf(3)
        return calibration.apply(raw)
    }

    private fun publishHeading() {
        if (calibrationListener != null) return
        val headingMatrix = resolveHeadingMatrix() ?: return
        val gravitySnapshot = gravity.takeIf { hasGravity }
        val rawHeading = computeRawHeading(headingMatrix, gravitySnapshot)
        listener?.onHeading(headingSmoother.smooth(rawHeading))
    }

    private fun computeRawHeading(headingMatrix: FloatArray, gravitySnapshot: FloatArray?): Float {
        if (gravitySnapshot != null && DeviceTiltDetector.isFlat(gravitySnapshot)) {
            return azimuthFromGetOrientation(headingMatrix)
        }
        return CompassHeadingCalculator.headingDegrees(headingMatrix, gravitySnapshot)
            ?: azimuthFromGetOrientation(headingMatrix)
    }

    private fun resolveHeadingMatrix(): FloatArray? {
        if (hasGravity && hasGeomagnetic && DeviceTiltDetector.isFlat(gravity)) {
            if (SensorManager.getRotationMatrix(fusedMatrix, inclinationMatrix, gravity, geomagnetic)) {
                return fusedMatrix
            }
        }
        if (hasRotationVectorMatrix) {
            return rotationVectorMatrix
        }
        if (
            hasGravity &&
            hasGeomagnetic &&
            SensorManager.getRotationMatrix(fusedMatrix, inclinationMatrix, gravity, geomagnetic)
        ) {
            return fusedMatrix
        }
        return null
    }

    private fun azimuthFromGetOrientation(rotationMatrix: FloatArray): Float {
        val matrix = if (
            SensorManager.remapCoordinateSystem(
                rotationMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Y,
                remappedMatrix,
            )
        ) {
            remappedMatrix
        } else {
            rotationMatrix
        }
        val orientation = FloatArray(3)
        SensorManager.getOrientation(matrix, orientation)
        return ((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
    }
}
