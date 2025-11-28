// app/src/main/java/com/cns/plantex/viewmodel/PlantexViewModel.kt
package com.cns.plantex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlantexState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val connectionProgress: Float = 0f,
    val deviceName: String = "Plantex Device",
    val wateringIntervalMinutes: Int = 120, // 2 hours default
    val remainingTimeSeconds: Long = 7200, // 2 hours in seconds
    val totalTimeSeconds: Long = 7200,
    val isWatering: Boolean = false,
    val lastWateredTime: Long = System.currentTimeMillis(),
    val waterLevel: Int = 75, // percentage
    val moistureLevel: Int = 45, // percentage
    val autoWatering: Boolean = true,
    val bluetoothDevices: List<String> = emptyList()
)

class PlantexViewModel : ViewModel() {

    private val _state = MutableStateFlow(PlantexState())
    val state: StateFlow<PlantexState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
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

            _state.update {
                it.copy(
                    isConnected = true,
                    isConnecting = false,
                    connectionProgress = 1f,
                    deviceName = deviceName
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
        val totalSeconds = minutes * 60L
        _state.update {
            it.copy(
                wateringIntervalMinutes = minutes,
                totalTimeSeconds = totalSeconds,
                remainingTimeSeconds = totalSeconds
            )
        }
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
        waterLevel: Int,
        autoWatering: Boolean
    ) {
        _state.update {
            it.copy(
                deviceName = deviceName,
                waterLevel = waterLevel,
                autoWatering = autoWatering
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}