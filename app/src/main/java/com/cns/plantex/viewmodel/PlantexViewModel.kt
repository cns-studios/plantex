// app/src/main/java/com/cns/plantex/viewmodel/PlantexViewModel.kt
package com.cns.plantex.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cns.plantex.WateringWorker
import com.cns.plantex.data.PreferenceDataStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class PlantexState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val connectionProgress: Float = 0f,
    val deviceName: String = "Plantex Device",
    val deviceAddress: String = "",
    val wateringIntervalMinutes: Int = 120, // 2 hours default
    val remainingTimeSeconds: Long = 7200, // 2 hours in seconds
    val totalTimeSeconds: Long = 7200,
    val isWatering: Boolean = false,
    val lastWateredTime: Long = System.currentTimeMillis(),
    val waterLevel: Int = 75, // percentage
    val moistureLevel: Int = 45, // percentage
    val autoWatering: Boolean = true,
    val bluetoothDevices: List<String> = emptyList(),
    val isBluetoothDialogVisible: Boolean = false
)

class PlantexViewModel(
    private val preferenceDataStore: PreferenceDataStore,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(PlantexState())
    val state: StateFlow<PlantexState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadSettings()
        startTimer()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val savedInterval = preferenceDataStore.getWateringInterval().firstOrNull()
            if (savedInterval != null) {
                setWateringInterval(savedInterval)
            }

            val deviceName = preferenceDataStore.getDeviceName().firstOrNull()
            val deviceAddress = preferenceDataStore.getDeviceAddress().firstOrNull()
            if (deviceName != null && deviceAddress != null) {
                _state.update { it.copy(deviceName = deviceName, deviceAddress = deviceAddress) }
                connect(deviceName)
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (_state.value.isConnected && _state.value.remainingTimeSeconds > 0) {
                    _state.update { currentState ->
                        currentState.copy(
                            remainingTimeSeconds = currentState.remainingTimeSeconds - 1
                        )
                    }
                } else if (_state.value.isConnected && _state.value.remainingTimeSeconds <= 0) {
                    resetTimer()
                }
            }
        }
    }

    fun connect(deviceName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isConnecting = true, connectionProgress = 0f) }

            // Mock Bluetooth connection with progress
            for (i in 1..10) {
                delay(500L)
                _state.update { it.copy(connectionProgress = i / 10f) }
            }

            // For this mock, we'll use a fake address
            val deviceAddress = "00:11:22:33:FF:EE"
            preferenceDataStore.saveDevice(deviceName, deviceAddress)

            _state.update {
                it.copy(
                    isConnected = true,
                    isConnecting = false,
                    connectionProgress = 1f,
                    deviceName = deviceName,
                    deviceAddress = deviceAddress
                )
            }
        }
    }

    fun scanForDevices() {
        viewModelScope.launch {
            _state.update { it.copy(bluetoothDevices = emptyList()) }
            delay(1000L)
            _state.update {
                it.copy(
                    bluetoothDevices = listOf(
                        "Plantex Alpha",
                        "Plantex Beta",
                        "Plantex Gamma"
                    )
                )
            }
        }
    }

    fun disconnect() {
        _state.update {
            it.copy(
                isConnected = false,
                isConnecting = false,
                connectionProgress = 0f
            )
        }
    }

    fun setWateringInterval(minutes: Int) {
        viewModelScope.launch {
            preferenceDataStore.saveWateringInterval(minutes)
            val totalSeconds = minutes * 60L
            _state.update {
                it.copy(
                    wateringIntervalMinutes = minutes,
                    totalTimeSeconds = totalSeconds,
                    remainingTimeSeconds = totalSeconds
                )
            }
            scheduleWateringWorker(minutes.toLong())
        }
    }

    private fun scheduleWateringWorker(intervalMinutes: Long) {
        val workRequest = PeriodicWorkRequestBuilder<WateringWorker>(
            intervalMinutes, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "watering_worker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun waterNow() {
        viewModelScope.launch {
            _state.update { it.copy(isWatering = true) }

            // Simulate watering for 3 seconds
            delay(3000L)

            _state.update {
                it.copy(
                    isWatering = false,
                    lastWateredTime = System.currentTimeMillis(),
                    moistureLevel = minOf(100, it.moistureLevel + 30)
                )
            }
        }
    }

    fun resetTimer() {
        _state.update {
            it.copy(remainingTimeSeconds = it.totalTimeSeconds)
        }
    }

    fun updateDeviceSettings(
        deviceName: String,
        deviceAddress: String,
        waterLevel: Int,
        autoWatering: Boolean
    ) {
        viewModelScope.launch {
            preferenceDataStore.saveDevice(deviceName, deviceAddress)
            _state.update {
                it.copy(
                    deviceName = deviceName,
                    deviceAddress = deviceAddress,
                    waterLevel = waterLevel,
                    autoWatering = autoWatering
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}