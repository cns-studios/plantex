// app/src/main/java/com/cns/plantex/data/PreferenceDataStore.kt
package com.cns.plantex.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFERENCES_NAME = "plantex_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

class PreferenceDataStore(context: Context) {

    private val dataStore = context.dataStore

    private object PreferenceKeys {
        val WATERING_INTERVAL = intPreferencesKey("watering_interval")
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val DEVICE_ADDRESS = stringPreferencesKey("device_address")
    }

    suspend fun saveWateringInterval(interval: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.WATERING_INTERVAL] = interval
        }
    }

    fun getWateringInterval(): Flow<Int?> {
        return dataStore.data.map { preferences ->
            preferences[PreferenceKeys.WATERING_INTERVAL]
        }
    }

    suspend fun saveDevice(name: String, address: String) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.DEVICE_NAME] = name
            preferences[PreferenceKeys.DEVICE_ADDRESS] = address
        }
    }

    fun getDeviceName(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PreferenceKeys.DEVICE_NAME]
        }
    }

    fun getDeviceAddress(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PreferenceKeys.DEVICE_ADDRESS]
        }
    }
}