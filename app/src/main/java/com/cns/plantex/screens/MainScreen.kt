// app/src/main/java/com/cns/plantex/ui/screens/MainScreen.kt
package com.cns.plantex.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cns.plantex.ui.components.*
import com.cns.plantex.viewmodel.PlantexViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PlantexViewModel
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showModifyDialog by remember { mutableStateOf(false) }
    var showConnectingDialog by remember { mutableStateOf(false) }
    var showBluetoothDialog by remember { mutableStateOf(false) }

    // Show connecting dialog when connecting
    LaunchedEffect(state.isConnecting) {
        showConnectingDialog = state.isConnecting
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Plantex",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                )
            )

            // Main Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Connection Card
                ConnectionCard(
                    isConnected = state.isConnected,
                    isConnecting = state.isConnecting,
                    connectionProgress = state.connectionProgress,
                    deviceName = state.deviceName,
                    waterLevel = state.waterLevel,
                    moistureLevel = state.moistureLevel,
                    onConnect = { showBluetoothDialog = true },
                    onDisconnect = { viewModel.disconnect() },
                    onModify = { showModifyDialog = true }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Circular Timer
                CircularTimer(
                    remainingSeconds = state.remainingTimeSeconds,
                    totalSeconds = state.totalTimeSeconds,
                    isConnected = state.isConnected,
                    onClick = {
                        if (state.isConnected) {
                            showFrequencyDialog = true
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Additional Info
                AnimatedVisibility(
                    visible = state.isConnected,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Eco,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Tip",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = "Most houseplants prefer watering every 2-3 hours during active growth.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        // Bottom Water Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            WaterButtonWithBackground(
                isWatering = state.isWatering,
                isConnected = state.isConnected,
                onClick = { viewModel.waterNow() }
            )
        }
    }

    // Dialogs
    if (showFrequencyDialog) {
        WateringFrequencyDialog(
            currentInterval = state.wateringIntervalMinutes,
            onDismiss = { showFrequencyDialog = false },
            onConfirm = { minutes ->
                viewModel.setWateringInterval(minutes)
                showFrequencyDialog = false
            }
        )
    }

    if (showModifyDialog) {
        ModifyDeviceDialog(
            currentDeviceName = state.deviceName,
            currentWaterLevel = state.waterLevel,
            currentAutoWatering = state.autoWatering,
            onDismiss = { showModifyDialog = false },
            onConfirm = { name, level, auto ->
                viewModel.updateDeviceSettings(name, level, auto)
                showModifyDialog = false
            }
        )
    }

    if (showConnectingDialog && state.isConnecting) {
        ConnectingDialog(
            progress = state.connectionProgress,
            onCancel = { viewModel.disconnect() }
        )
    }

    if (showBluetoothDialog) {
        BluetoothDeviceSelectionDialog(
            devices = state.bluetoothDevices,
            onDismiss = { showBluetoothDialog = false },
            onScan = { viewModel.scanForDevices() },
            onConnect = { device ->
                scope.launch {
                    showBluetoothDialog = false
                    // Short delay to allow the dialog to dismiss before showing the connection progress
                    kotlinx.coroutines.delay(100)
                    viewModel.connect(device)
                }
            }
        )
    }
}
