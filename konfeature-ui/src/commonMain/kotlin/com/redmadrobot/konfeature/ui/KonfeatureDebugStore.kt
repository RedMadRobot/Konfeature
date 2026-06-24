package com.redmadrobot.konfeature.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.redmadrobot.konfeature.Logger
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
 * Prefer the [create] factory, which constructs the store **and** completes the initial [load] in
 * one `suspend` call, so the returned store already reflects any persisted overrides — callers
 * cannot forget to load it:
 *
 * ```kotlin
 * // Once on startup, from a suitable coroutine scope (DI init, application scope, etc.):
 * val store = KonfeatureDebugStore.create(
 *     path = context.filesDir.resolve("konfeature_debug.preferences_pb").absolutePath,
 *     logger = konfeatureLogger, // optional
 * )
 * val interceptor = KonfeatureDebugInterceptor(store)
 *
 * val konfeature = konfeature {
 *     addInterceptor(interceptor)
 * }
 * ```
 *
 * **Hold a single instance.** Creation is a one-time startup operation; provide the store as a
 * singleton (DI graph or a shared `object`) and reuse it for the interceptor and the debug screen.
 *
 * The current overrides are exposed both as a [values] flow (observed by the debug screen) and via
 * the synchronous, non-blocking [currentValue].
 *
 * @param path absolute file path for the DataStore storage file. Platform-specific: on Android use
 *   `context.filesDir`, on iOS use `NSDocumentDirectory`.
 * @param logger optional logger.
 */
public class KonfeatureDebugStore(
    path: String,
    private val logger: Logger? = null,
) {

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = { path.toPath() },
    )

    private val _values = MutableStateFlow<Map<String, Any>>(emptyMap())

    /** Current overrides. Updated by [load] and by the mutate operations. */
    public val values: StateFlow<Map<String, Any>> = _values.asStateFlow()

    /**
     * Loads overrides from DataStore. Idempotent: a repeated call re-reads the disk.
     */
    @Suppress("TooGenericExceptionCaught")
    public suspend fun load() {
        try {
            val prefs = dataStore.data.first()
            val json = prefs[VALUES_KEY]
            if (json != null) {
                _values.value = deserializeMap(json)
            }
        } catch (e: Exception) {
            // A debug tool can run without persisted overrides; surface the failure but don't crash.
            logger?.warn("Failed to load debug overrides, starting empty: ${e.message}")
        }
    }

    /** Sets an override for [key]. */
    public suspend fun setValue(key: String, value: Any) {
        _values.update { it + (key to value) }
        persist()
        logger?.info("Set debug override '$key' = '$value'")
    }

    /** Removes the override for a single [key]. */
    public suspend fun resetValue(key: String) {
        _values.update { it - key }
        persist()
        logger?.info("Reset debug override '$key'")
    }

    /** Removes all overrides. */
    public suspend fun resetAll() {
        _values.update { emptyMap() }
        persist()
        logger?.info("Reset all debug overrides")
    }

    /** Synchronous read of the current override. */
    public fun currentValue(key: String): Any? = _values.value[key]

    private suspend fun persist() {
        val map = _values.value
        dataStore.edit { prefs ->
            if (map.isEmpty()) {
                prefs.remove(VALUES_KEY)
            } else {
                prefs[VALUES_KEY] = serializeMap(map, logger)
            }
        }
    }

    public companion object {

        private val VALUES_KEY = stringPreferencesKey("debug_values")

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = false
        }

        /**
         * Creates a store for [path] and completes the initial [load] before returning.
         *
         * Call once on startup from any suitable coroutine scope; the returned store already
         * reflects persisted overrides.
         */
        public suspend fun create(path: String, logger: Logger? = null): KonfeatureDebugStore {
            return KonfeatureDebugStore(path, logger).apply { load() }
        }

        private fun serializeMap(map: Map<String, Any>, logger: Logger?): String {
            return buildJsonObject {
                map.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> put(key, value)
                        is Int -> put(key, value)
                        is Long -> put(key, value)
                        is Float -> put(key, value.toDouble())
                        is Double -> put(key, value)
                        is String -> put(key, value)
                        else -> logger?.warn(
                            "Skipping override '$key': unsupported type '${value::class.simpleName}'",
                        )
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
