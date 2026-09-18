package com.redmadrobot.konfeature.ui.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import okio.Path.Companion.toPath

internal actual fun createDebugValuesStorage(path: String): DebugValuesStorage {
    return DataStoreDebugValuesStorage(path)
}

/** Persists the overrides blob to a DataStore preferences file at [path]. */
private class DataStoreDebugValuesStorage(path: String) : DebugValuesStorage {

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = { path.toPath() },
    )

    override suspend fun read(): String? = dataStore.data.first()[VALUES_KEY]

    override suspend fun write(raw: String?) {
        dataStore.edit { prefs ->
            if (raw == null) prefs.remove(VALUES_KEY) else prefs[VALUES_KEY] = raw
        }
    }

    private companion object {
        private val VALUES_KEY = stringPreferencesKey("debug_values")
    }
}
