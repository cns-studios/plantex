// app/src/main/java/com/cns/plantex/viewmodel/PlantexViewModelFactory.kt
package com.cns.plantex.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cns.plantex.data.PreferenceDataStore

class PlantexViewModelFactory(
    private val preferenceDataStore: PreferenceDataStore,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantexViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantexViewModel(preferenceDataStore, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}