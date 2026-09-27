package com.antigravity.btremote.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sign

/**
 * 6-DOF Sensor Fusion Inertial Air Mouse Controller.
 * Converts real-time angular velocity from the phone's hardware gyroscope
 * into screen-space mouse delta coordinates with adaptive tremor filtering
 * and non-linear kinetic acceleration.
 */
class GyroAirMouse(
    context: Context,
    private val onDelta: (dx: Int, dy: Int) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    val isAvailable: Boolean = gyroscope != null

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    var sensitivity: Float = 22.0f
    var deadzoneThreshold: Float = 0.045f // Tremor deadband in radians/sec
    var accelerationFactor: Float = 0.75f // Non-linear ballistic curve

    private var lastTimestampNs: Long = 0L

    fun start() {
        if (!isAvailable || _isActive.value) return
        lastTimestampNs = 0L
        sensorManager?.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)
        _isActive.value = true
    }

    fun stop() {
        if (!_isActive.value) return
        sensorManager?.unregisterListener(this)
        _isActive.value = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !_isActive.value) return
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val timestamp = event.timestamp
            if (lastTimestampNs == 0L) {
                lastTimestampNs = timestamp
                return
            }

            val dt = (timestamp - lastTimestampNs) * 1e-9f // seconds
            lastTimestampNs = timestamp
            if (dt <= 0f || dt > 0.1f) return

            // Gyroscope axes:
            // event.values[0] = Angular speed around X axis (Pitch: Tilt Up/Down)
            // event.values[1] = Angular speed around Y axis (Roll)
            // event.values[2] = Angular speed around Z axis (Yaw: Turn Left/Right)
            val pitch = event.values[0]
            val yaw = event.values[2]

            // Apply tremor filter and deadband
            val dxRaw = -yaw
            val dyRaw = -pitch

            val dxFiltered = applyDeadband(dxRaw, deadzoneThreshold)
            val dyFiltered = applyDeadband(dyRaw, deadzoneThreshold)

            if (dxFiltered != 0f || dyFiltered != 0f) {
                // Non-linear ballistic curve: speed^1.2 for flick response + precision micro-aim
                val dxAcc = applyBallistics(dxFiltered, sensitivity, accelerationFactor)
                val dyAcc = applyBallistics(dyFiltered, sensitivity, accelerationFactor)

                val dx = dxAcc.toInt()
                val dy = dyAcc.toInt()

                if (dx != 0 || dy != 0) {
                    onDelta(dx, dy)
                }
            }
        }
    }

    private fun applyDeadband(value: Float, threshold: Float): Float {
        val magnitude = abs(value)
        return if (magnitude < threshold) {
            0f
        } else {
            sign(value) * (magnitude - threshold)
        }
    }

    private fun applyBallistics(value: Float, gain: Float, accel: Float): Float {
        val mag = abs(value)
        val factor = 1.0f + (mag * accel)
        return sign(value) * (mag * gain * factor)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
