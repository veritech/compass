package compass.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
    private var activeCalibration: MagnetometerCalibration? = loadStoredCalibration()

    private val fusedMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private var magnetometerSensor: Sensor? = null

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when {
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

    override fun clearCalibration() {
        activeCalibration = null
        calibrationStore.clear()
        headingSmoother.reset()
        registerMagnetometer()
    }

    override fun hasCalibration(): Boolean = activeCalibration != null

    private fun loadStoredCalibration(): MagnetometerCalibration? {
        val stored = calibrationStore.load() ?: return null
        if (stored.isPlausible()) return stored
        calibrationStore.clear()
        return null
    }

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

        val calibration = calibrationBuilder.build()?.takeIf { it.isPlausible() }
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
        if (!hasGravity || !hasGeomagnetic) return
        if (!SensorManager.getRotationMatrix(fusedMatrix, inclinationMatrix, gravity, geomagnetic)) {
            return
        }

        val rawHeading = CompassOrientation.azimuthDegrees(fusedMatrix, remappedMatrix)
        listener?.onHeading(headingSmoother.smooth(rawHeading))
    }
}
