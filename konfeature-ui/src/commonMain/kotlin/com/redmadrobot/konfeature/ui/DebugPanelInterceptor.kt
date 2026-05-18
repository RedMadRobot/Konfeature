package com.redmadrobot.konfeature.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.redmadrobot.konfeature.source.FeatureValueSource
import com.redmadrobot.konfeature.source.Interceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import okio.Path.Companion.toPath

/**
 * An [Interceptor] implementation that allows overriding feature values at runtime
 * via a debug panel. Overrides are persisted to disk using DataStore.
 *
 * Create an instance and register it with both [Konfeature][com.redmadrobot.konfeature.Konfeature]
 * (via `addInterceptor`) and [KonfeatureUi] (via `init`):
 *
 * ```kotlin
 * val interceptor = DebugPanelInterceptor(
 *     producePath = { context.filesDir.resolve("konfeature_debug.preferences_pb").absolutePath }
 * )
 *
 * val konfeature = konfeature {
 *     addInterceptor(interceptor)
 *     register(myConfig)
 * }
 *
 * KonfeatureUi.init(konfeature, interceptor)
 * ```
 *
 * @param producePath provides an absolute file path for the DataStore storage file.
 *   Platform-specific: on Android use `context.filesDir`, on iOS use `NSDocumentDirectory`.
 * @param scope coroutine scope used for the initial load from disk
 */
public class DebugPanelInterceptor(
    producePath: () -> String,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : Interceptor {

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() },
    )

    private val _valuesFlow = MutableStateFlow(emptyMap<String, Any>())

    internal val valuesFlow = _valuesFlow.asStateFlow()

    override val name: String = "DebugPanelInterceptor"

    init {
        scope.launch { loadValues() }
    }

    override fun intercept(valueSource: FeatureValueSource, key: String, value: Any): Any? {
        return _valuesFlow.value[key]
            ?.let { convertTypeIfNeeded(it, value) }
            ?.takeIf { it != value }
    }

    /**
     * Map debugValue from Int to Long, from Float to Double,
     * and from Long to Double if the original value is Double.
     */
    private fun convertTypeIfNeeded(debugValue: Any, value: Any): Any {
        var result = when (debugValue) {
            is Int -> debugValue.toLong()
            is Float -> debugValue.toDouble()
            else -> debugValue
        }
        if (result is Long && value is Double) {
            result = result.toDouble()
        }
        return result
    }

    internal suspend fun setValue(key: String, value: Any) {
        _valuesFlow.update { it + (key to value) }
        persistValues(_valuesFlow.value)
    }

    internal suspend fun resetValue(key: String) {
        _valuesFlow.update { it - key }
        persistValues(_valuesFlow.value)
    }

    internal suspend fun resetAllValues() {
        _valuesFlow.value = emptyMap()
        persistValues(emptyMap())
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun loadValues() {
        try {
            val prefs = dataStore.data.first()
            val json = prefs[VALUES_KEY] ?: return
            _valuesFlow.value = deserializeMap(json)
        } catch (_: Exception) {
            // If loading fails, start with an empty map — acceptable for a debug tool.
        }
    }

    private suspend fun persistValues(map: Map<String, Any>) {
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

        private fun deserializeMap(json: String): Map<String, Any> {
            return Json.parseToJsonElement(json).jsonObject.mapNotNull { (key, element) ->
                val primitive = element as? JsonPrimitive ?: return@mapNotNull null
                val value: Any = primitive.booleanOrNull
                    ?: primitive.longOrNull
                    ?: primitive.doubleOrNull
                    ?: primitive.content.takeIf { primitive.isString }
                    ?: return@mapNotNull null
                key to value
            }.toMap()
        }
    }
}
