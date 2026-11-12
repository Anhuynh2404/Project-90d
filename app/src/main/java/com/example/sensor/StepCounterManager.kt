package com.example.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import kotlin.math.sqrt

/**
 * PHẦN 1: Quản lý cảm biến đếm bước chân 24/7 (Sensor.TYPE_STEP_COUNTER & TYPE_STEP_DETECTOR)
 * - Công thức chuẩn: Số bước trong ngày = (Số bước hiện tại - Số bước lúc đầu ngày) + Số bước bù
 * - Lưu trữ bền vững vào SharedPreferences mỗi khi có thay đổi để không mất dữ liệu khi app/service bị kill
 * - Xử lý tự động khi qua ngày mới (00:00) hoặc khi điện thoại khởi động lại (reboot làm reset counter về 0)
 */
class StepCounterManager(context: Context) : SensorEventListener {

    companion object {
        const val PREFS_PEDOMETER = "project90_pedometer_prefs"
        const val KEY_SAVED_EPOCH_DAY = "saved_epoch_day"
        const val KEY_START_OF_DAY_SENSOR_STEPS = "start_of_day_sensor_steps"
        const val KEY_LAST_RAW_SENSOR_STEPS = "last_raw_sensor_steps"
        const val KEY_OFFSET_STEPS_TODAY = "offset_steps_today"
        const val KEY_TODAY_TOTAL_STEPS = "today_total_steps"
    }

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_PEDOMETER, Context.MODE_PRIVATE)
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val stepCounterSensor: Sensor? = try {
        sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    } catch (_: Exception) {
        null
    }

    private val stepDetectorSensor: Sensor? = try {
        sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    } catch (_: Exception) {
        null
    }

    private val accelerometerSensor: Sensor? = try {
        if (stepCounterSensor == null && stepDetectorSensor == null) {
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        } else null
    } catch (_: Exception) {
        null
    }

    private val _stepCount = MutableStateFlow(loadPersistedTodaySteps())
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    private val _hasHardwareSensor = MutableStateFlow(
        stepCounterSensor != null || stepDetectorSensor != null || accelerometerSensor != null
    )
    val hasHardwareSensor: StateFlow<Boolean> = _hasHardwareSensor.asStateFlow()

    private val _isPermissionGranted = MutableStateFlow(hasActivityRecognitionPermission())
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private var isRegistered = false
    private var lastAccelPeakTimeMs = 0L

    fun hasActivityRecognitionPermission(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        } catch (_: Exception) {
            true
        }
    }

    private fun loadPersistedTodaySteps(): Int {
        return try {
            val todayEpoch = LocalDate.now().toEpochDay()
            val savedDay = prefs.getLong(KEY_SAVED_EPOCH_DAY, -1L)
            if (savedDay != todayEpoch) {
                // Qua ngày mới: reset số bước hiển thị trong ngày về 0, giữ lại last_raw để làm mốc đầu ngày mới
                val lastRaw = prefs.getInt(KEY_LAST_RAW_SENSOR_STEPS, -1)
                prefs.edit()
                    .putLong(KEY_SAVED_EPOCH_DAY, todayEpoch)
                    .putInt(KEY_START_OF_DAY_SENSOR_STEPS, lastRaw)
                    .putInt(KEY_OFFSET_STEPS_TODAY, 0)
                    .putInt(KEY_TODAY_TOTAL_STEPS, 0)
                    .apply()
                0
            } else {
                prefs.getInt(KEY_TODAY_TOTAL_STEPS, 0).coerceAtLeast(0)
            }
        } catch (_: Exception) {
            0
        }
    }

    fun startListening() {
        try {
            _isPermissionGranted.value = hasActivityRecognitionPermission()
            _stepCount.value = loadPersistedTodaySteps()

            if (isRegistered) {
                sensorManager?.unregisterListener(this)
                isRegistered = false
            }

            var registeredAny = false

            // 1. Đăng ký Sensor.TYPE_STEP_COUNTER với maxReportLatencyUs = 0 để nhận sự kiện tức thì (không bị batching trễ)
            stepCounterSensor?.let { sensor ->
                val ok = sensorManager?.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_FASTEST,
                    0
                ) ?: false
                registeredAny = registeredAny || ok
            }

            // 2. Đăng ký thêm Sensor.TYPE_STEP_DETECTOR để cập nhật từng bước chân ngay khi người dùng di chuyển
            stepDetectorSensor?.let { sensor ->
                val ok = sensorManager?.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_FASTEST,
                    0
                ) ?: false
                registeredAny = registeredAny || ok
            }

            // 3. Fallback cảm biến gia tốc nếu máy không có chip Step Counter chuyên dụng
            if (!registeredAny && accelerometerSensor != null) {
                val ok = sensorManager?.registerListener(
                    this,
                    accelerometerSensor,
                    SensorManager.SENSOR_DELAY_GAME
                ) ?: false
                registeredAny = registeredAny || ok
            }

            isRegistered = registeredAny
        } catch (_: Exception) {
            // Đảm bảo không crash trên mọi thiết bị Android thực tế
        }
    }

    fun refreshPermissionAndReconnect() {
        isRegistered = false
        startListening()
    }

    fun stopListening() {
        try {
            sensorManager?.unregisterListener(this)
            isRegistered = false
        } catch (_: Exception) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        try {
            when (event.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    val currentRawSteps = event.values[0].toInt()
                    if (currentRawSteps < 0) return
                    handleHardwareStepCounterReading(currentRawSteps)
                }

                Sensor.TYPE_STEP_DETECTOR -> {
                    if (event.values.isNotEmpty() && event.values[0] >= 1.0f) {
                        handleSingleStepDetected()
                    }
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    val now = System.currentTimeMillis()
                    if (magnitude > 12.8f && (now - lastAccelPeakTimeMs) > 380L) {
                        lastAccelPeakTimeMs = now
                        handleSingleStepDetected()
                    }
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Xử lý logic tính số bước trong ngày chuẩn:
     * Số bước trong ngày = (Số bước cảm biến hiện tại - Số bước cảm biến lúc đầu ngày) + Số bước bù (nếu có)
     */
    fun handleHardwareStepCounterReading(currentRawSteps: Int): Int {
        val todayEpoch = LocalDate.now().toEpochDay()
        val savedDay = prefs.getLong(KEY_SAVED_EPOCH_DAY, -1L)
        var startOfDaySteps = prefs.getInt(KEY_START_OF_DAY_SENSOR_STEPS, -1)
        var offsetSteps = prefs.getInt(KEY_OFFSET_STEPS_TODAY, 0)
        val previousRawSteps = prefs.getInt(KEY_LAST_RAW_SENSOR_STEPS, -1)

        if (savedDay != todayEpoch) {
            // Bước sang ngày mới: chốt số bước lúc đầu ngày = currentRawSteps
            startOfDaySteps = currentRawSteps
            offsetSteps = 0
        } else if (startOfDaySteps < 0) {
            // Lần đầu tiên đọc cảm biến trong ngày
            startOfDaySteps = currentRawSteps
        } else if (currentRawSteps < startOfDaySteps) {
            // Điện thoại vừa khởi động lại (reboot) trong ngày khiến currentRawSteps quay về 0
            val stepsBeforeReboot = if (previousRawSteps >= startOfDaySteps) {
                (previousRawSteps - startOfDaySteps).coerceAtLeast(0)
            } else {
                prefs.getInt(KEY_TODAY_TOTAL_STEPS, 0)
            }
            offsetSteps = (offsetSteps + stepsBeforeReboot).coerceAtLeast(0)
            startOfDaySteps = currentRawSteps
        }

        // Công thức cốt lõi: Số bước hiện tại - Số bước lúc đầu ngày
        val sensorDeltaToday = (currentRawSteps - startOfDaySteps).coerceAtLeast(0)
        val totalTodaySteps = (sensorDeltaToday + offsetSteps).coerceAtLeast(0)

        prefs.edit()
            .putLong(KEY_SAVED_EPOCH_DAY, todayEpoch)
            .putInt(KEY_START_OF_DAY_SENSOR_STEPS, startOfDaySteps)
            .putInt(KEY_LAST_RAW_SENSOR_STEPS, currentRawSteps)
            .putInt(KEY_OFFSET_STEPS_TODAY, offsetSteps)
            .putInt(KEY_TODAY_TOTAL_STEPS, totalTodaySteps)
            .apply()

        _stepCount.value = totalTodaySteps
        return totalTodaySteps
    }

    private fun handleSingleStepDetected() {
        // Nếu máy chưa phát sự kiện TYPE_STEP_COUNTER hoặc chỉ có TYPE_STEP_DETECTOR, cộng ngay 1 bước
        val todayEpoch = LocalDate.now().toEpochDay()
        val savedDay = prefs.getLong(KEY_SAVED_EPOCH_DAY, -1L)
        val currentTotal = if (savedDay == todayEpoch) {
            prefs.getInt(KEY_TODAY_TOTAL_STEPS, _stepCount.value)
        } else {
            0
        }
        val updated = currentTotal + 1
        // Nếu chưa có baseline của TYPE_STEP_COUNTER thì lưu vào offset
        val startOfDaySteps = prefs.getInt(KEY_START_OF_DAY_SENSOR_STEPS, -1)
        val lastRaw = prefs.getInt(KEY_LAST_RAW_SENSOR_STEPS, -1)
        val sensorDelta = if (startOfDaySteps >= 0 && lastRaw >= startOfDaySteps) {
            lastRaw - startOfDaySteps
        } else 0
        val newOffset = (updated - sensorDelta).coerceAtLeast(0)

        prefs.edit()
            .putLong(KEY_SAVED_EPOCH_DAY, todayEpoch)
            .putInt(KEY_OFFSET_STEPS_TODAY, newOffset)
            .putInt(KEY_TODAY_TOTAL_STEPS, updated)
            .apply()

        _stepCount.value = updated
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    fun addManualSteps(steps: Int) {
        val current = loadPersistedTodaySteps()
        setStepCount((current + steps).coerceAtLeast(0))
    }

    fun setStepCount(steps: Int) {
        try {
            val safeSteps = steps.coerceAtLeast(0)
            val todayEpoch = LocalDate.now().toEpochDay()
            val startOfDaySteps = prefs.getInt(KEY_START_OF_DAY_SENSOR_STEPS, -1)
            val lastRaw = prefs.getInt(KEY_LAST_RAW_SENSOR_STEPS, -1)
            val sensorDelta = if (startOfDaySteps >= 0 && lastRaw >= startOfDaySteps) {
                (lastRaw - startOfDaySteps).coerceAtLeast(0)
            } else 0
            val newOffset = (safeSteps - sensorDelta).coerceAtLeast(0)

            prefs.edit()
                .putLong(KEY_SAVED_EPOCH_DAY, todayEpoch)
                .putInt(KEY_OFFSET_STEPS_TODAY, newOffset)
                .putInt(KEY_TODAY_TOTAL_STEPS, safeSteps)
                .apply()

            _stepCount.value = safeSteps
        } catch (_: Exception) {
            _stepCount.value = steps.coerceAtLeast(0)
        }
    }
}
