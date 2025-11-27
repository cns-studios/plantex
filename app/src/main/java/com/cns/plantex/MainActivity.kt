// app/src/main/java/com/cns/plantex/MainActivity.kt
package com.cns.plantex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cns.plantex.ui.screens.MainScreen
import com.cns.plantex.ui.theme.PlantexTheme
import com.cns.plantex.viewmodel.PlantexViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlantexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: PlantexViewModel = viewModel()
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}