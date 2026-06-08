package com.redmadrobot.konfeature.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import okio.Path.Companion.toPath

/**
 * Stores and persists feature value overrides applied by a debug panel.
 *
 * The store owns no coroutine scope and is **not** [AutoCloseable]: every background action runs
 * under a `suspend` call driven by the integrator. Read the initial overrides from disk once on
 * startup via [load]; perform mutations via the `suspend` [setValue] / [resetValue] / [resetAll],
 * each of which returns only after the write to DataStore completes.
 *
 * The current overrides are exposed both as a [values] flow (observed by the debug screen) and via
 * the synchronous, non-blocking [currentValue], which [KonfeatureDebugInterceptor.intercept] calls
 * on every `getValue`.
 *
 * ```kotlin
 * val store = KonfeatureDebugStore(
 *     producePath = { context.filesDir.resolve("konfeature_debug.preferences_pb").absolutePath }
 * )
 * val interceptor = KonfeatureDebugInterceptor(store)
 *
 * val konfeature = konfeature {
 *     addInterceptor(interceptor)
 *     register(myConfig)
 * }
 *
 * // Once on startup, from any suitable scope:
 * applicationScope.launch { store.load() }
 * ```
 *
 * @param producePath provides an absolute file path for the DataStore storage file.
 *   Platform-specific: on Android use `context.filesDir`, on iOS use `NSDocumentDirectory`.
 */
public class KonfeatureDebugStore(
    producePath: () -> String,
) {

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() },
    )

    private val _values = MutableStateFlow<Map<String, Any>>(emptyMap())

    /** Current overrides. Updated by [load] and by the mutate operations. */
    public val values: StateFlow<Map<String, Any>> = _values.asStateFlow()

    /**
     * Loads overrides from DataStore. Idempotent: a repeated call re-reads the disk.
     * Should be called at least once on startup; before the first call [currentValue] returns
     * `null` and the interceptor reports default/source values.
     */
    @Suppress("TooGenericExceptionCaught")
    public suspend fun load() {
        try {
            val prefs = dataStore.data.first()
            val json = prefs[VALUES_KEY]
            if (json != null) {
                _values.value = deserializeMap(json)
            }
        } catch (_: Exception) {
            // If loading fails, start with an empty map — acceptable for a debug tool.
        }
    }

    /** Sets an override for [key]. Returns after the write to DataStore succeeds. */
    public suspend fun setValue(key: String, value: Any) {
        _values.update { it + (key to value) }
        persist()
    }

    /** Removes the override for a single [key]. */
    public suspend fun resetValue(key: String) {
        _values.update { it - key }
        persist()
    }

    /** Removes all overrides. */
    public suspend fun resetAll() {
        _values.update { emptyMap() }
        persist()
    }

    /** Synchronous read of the current override (used from [KonfeatureDebugInterceptor.intercept]). */
    public fun currentValue(key: String): Any? = _values.value[key]

    private suspend fun persist() {
        val map = _values.value
        dataStore.edit { prefs ->
            if (map.isEmpty()) {
                prefs.remove(VALUES_KEY)
            } else {
                prefs[VALUES_KEY] = serializeMap(map)
            }
        }
    }

    private companion object {
        private val VALUES_KEY = stringPreferencesKey("debug_values")

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = false
        }

        private fun serializeMap(map: Map<String, Any>): String {
            return buildJsonObject {
                map.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> put(key, value)
                        is Int -> put(key, value)
                        is Long -> put(key, value)
                        is Float -> put(key, value.toDouble())
                        is Double -> put(key, value)
                        is String -> put(key, value)
                        else -> { /* unsupported type, skip */ }
                    }
                }
            }.toString()
        }

        private fun deserializeMap(raw: String): Map<String, Any> {
            return json.parseToJsonElement(raw).jsonObject.mapNotNull(::parseEntry).toMap()
        }

        private fun parseEntry(entry: Map.Entry<String, JsonElement>): Pair<String, Any>? {
            val primitive = entry.value as? JsonPrimitive
            val value: Any? = primitive?.booleanOrNull
                ?: primitive?.longOrNull
                ?: primitive?.doubleOrNull
                ?: primitive?.content?.takeIf { primitive.isString }
            return value?.let { entry.key to it }
        }
    }
}
