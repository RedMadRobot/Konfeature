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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
 * An [Interceptor] implementation that allows overriding feature values at runtime
 * via a debug panel. Overrides are persisted to disk using DataStore.
 *
 * Create an instance, register it with [Konfeature][com.redmadrobot.konfeature.Konfeature]
 * via `addInterceptor`, and pass both into [KonfeatureScreenData]:
 *
 * ```kotlin
 * val interceptor = KonfeatureInterceptor(
 *     producePath = { context.filesDir.resolve("konfeature_debug.preferences_pb").absolutePath }
 * )
 *
 * val konfeature = konfeature {
 *     addInterceptor(interceptor)
 *     register(myConfig)
 * }
 *
 * val panel = KonfeatureScreenData(konfeature, interceptor)
 *
 * // On app shutdown, release the internal scope:
 * interceptor.close()
 * ```
 *
 * The initial load from disk is asynchronous. Until it completes [intercept] returns `null`
 * so callers see default values rather than stale overrides. Apps that need to read overrides
 * synchronously at startup can suspend on [awaitReady].
 *
 * When [scope] is omitted the interceptor owns an internal scope that must be released via
 * [close] to avoid leaks; when a [scope] is provided the caller is responsible for its lifecycle.
 *
 * @param producePath provides an absolute file path for the DataStore storage file.
 *   Platform-specific: on Android use `context.filesDir`, on iOS use `NSDocumentDirectory`.
 * @param scope coroutine scope used for the initial load from disk. If `null`, an internal
 *   `Dispatchers.Default + SupervisorJob` scope is created and cancelled in [close].
 */
public class KonfeatureInterceptor(
    producePath: () -> String,
    scope: CoroutineScope? = null,
) : Interceptor, AutoCloseable {

    private val ownsScope: Boolean = scope == null

    private val scope: CoroutineScope = scope ?: CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() },
    )

    private val _valuesFlow = MutableStateFlow(emptyMap<String, Any>())
    private val _readyFlow = MutableStateFlow(false)
    private val persistMutex = Mutex()

    internal val valuesFlow: StateFlow<Map<String, Any>> = _valuesFlow.asStateFlow()

    override val name: String = "KonfeatureInterceptor"

    init {
        this.scope.launch { loadValues() }
    }

    /** Suspends until the initial load from disk has completed. */
    public suspend fun awaitReady() {
        _readyFlow.first { it }
    }

    /** Cancels the internal coroutine scope if one was created. No-op when an external scope was supplied. */
    override fun close() {
        if (ownsScope) scope.cancel()
    }

    override fun intercept(valueSource: FeatureValueSource, key: String, value: Any): Any? {
        if (!_readyFlow.value) return null
        return _valuesFlow.value[key]?.takeIf { it != value }
    }

    internal suspend fun setValue(key: String, value: Any) {
        persistMutex.withLock {
            val newMap = _valuesFlow.updateAndGet { it + (key to value) }
            persistValues(newMap)
        }
    }

    internal suspend fun resetValue(key: String) {
        persistMutex.withLock {
            val newMap = _valuesFlow.updateAndGet { it - key }
            persistValues(newMap)
        }
    }

    internal suspend fun resetAllValues() {
        persistMutex.withLock {
            val newMap = _valuesFlow.updateAndGet { emptyMap() }
            persistValues(newMap)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun loadValues() {
        persistMutex.withLock {
            try {
                val prefs = dataStore.data.first()
                val json = prefs[VALUES_KEY]
                if (json != null) {
                    _valuesFlow.value = deserializeMap(json)
                }
            } catch (_: Exception) {
                // If loading fails, start with an empty map — acceptable for a debug tool.
            }
            _readyFlow.value = true
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
